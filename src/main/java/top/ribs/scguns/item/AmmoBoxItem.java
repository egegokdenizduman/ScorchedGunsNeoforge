package top.ribs.scguns.item;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.box.PackageStyles.PackageStyle;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Config;
import top.ribs.scguns.item.ammo_boxes.AmmoBoxTooltip;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Ammo boxes are Create packages: placing, throwing, opening (shift-right-click), packagers, chains and
 * frogports all use Create's own package behaviour. The ammo is stored in Create's package contents
 * component, so whatever Create does with a package keeps the ammo intact.
 *
 * Capacity is counted in stacks ({@link Config.Gameplay#ammoBoxStackCapacity}, at most the package's 9
 * slots). The item bar, the fill-level model predicate and the tooltip all read {@link #getFullness(ItemStack)}.
 *
 * Clicks (same as a vanilla bundle):
 * - right-click the box onto a slot with matching ammo: pulls as much as fits;
 * - right-click ammo onto the box: puts as much as fits into it;
 * - right-click the box onto an empty slot / with an empty cursor: takes out the most recently stored stack.
 */
public abstract class AmmoBoxItem extends PackageItem {
    /** Weight units of one full stack, used by the tooltip to show partial stacks. */
    public static final int UNITS_PER_STACK = 64;
    /** Same size as Create's rare packages, so the 12x10 package model and chain rigging fit. */
    public static final PackageStyle STYLE = new PackageStyle("scguns_ammo_box", 12, 10, 21f, true);
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);
    private static final int BAR_COLOR_FULL = Mth.color(1.0F, 0.33F, 0.33F);

    private String descriptionId;

    public AmmoBoxItem(Item.Properties properties) {
        super(properties, STYLE);
        // Packagers pick a random box from these lists for ordinary items; ammo boxes are only chosen by
        // PackageItemContainingMixin when the contents are all one ammo type.
        PackageStyles.STANDARD_BOXES.remove(this);
        PackageStyles.RARE_BOXES.remove(this);
    }

    public abstract ResourceLocation getAmmoTag();

    public boolean canHold(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof PackageItem) && stack.is(ItemTags.create(getAmmoTag()));
    }

    @Override
    public @NotNull String getDescriptionId() {
        // PackageItem names every package "Package"; ammo boxes keep their own names.
        if (descriptionId == null) {
            descriptionId = Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
        }
        return descriptionId;
    }

    // ------------------------------------------------------------------ capacity

    /** Stacks this box holds. */
    public static int getStackCapacity(ItemStack box) {
        if (!(box.getItem() instanceof AmmoBoxItem)) {
            return 0;
        }
        return Mth.clamp(Config.COMMON.gameplay.ammoBoxStackCapacity.get(), 1, SLOTS);
    }

    /** Capacity in weight units ({@link #UNITS_PER_STACK} per stack). */
    public static int getMaxItemCount(ItemStack box) {
        return getStackCapacity(box) * UNITS_PER_STACK;
    }

    /** Weight of a single item: 1 for things that stack to 64, 4 for things that stack to 16, ... */
    public static int getWeight(ItemStack stack) {
        return Math.max(1, UNITS_PER_STACK / Math.max(1, stack.getMaxStackSize()));
    }

    /** Used capacity in weight units. */
    public static int getUsedUnits(ItemStack box) {
        if (box.getItem() instanceof CreativeAmmoBoxItem) {
            return 0;
        }
        return getStoredAmmo(box).mapToInt(s -> s.getCount() * getWeight(s)).sum();
    }

    /** 0..1 fill level, shared by the item bar, the model predicate and the tooltip. */
    public static float getFullness(ItemStack box) {
        int max = getMaxItemCount(box);
        if (max <= 0 || box.getItem() instanceof CreativeAmmoBoxItem) {
            return 0F;
        }
        return Mth.clamp((float) getUsedUnits(box) / max, 0F, 1F);
    }

    public static boolean isFull(ItemStack box) {
        return getUsedUnits(box) >= getMaxItemCount(box);
    }

    /** How many of {@code toInsert} would fit into the box right now (merging into stacks, then free slots). */
    public static int getInsertableCount(ItemStack box, ItemStack toInsert) {
        if (!(box.getItem() instanceof AmmoBoxItem item) || item instanceof CreativeAmmoBoxItem || !item.canHold(toInsert)) {
            return 0;
        }
        List<ItemStack> contents = getStoredAmmo(box).toList();
        int max = toInsert.getMaxStackSize();
        int space = 0;
        for (ItemStack existing : contents) {
            if (ItemStack.isSameItemSameComponents(existing, toInsert)) {
                space += Math.max(0, max - existing.getCount());
            }
        }
        space += Math.max(0, getStackCapacity(box) - contents.size()) * max;
        return Math.min(toInsert.getCount(), space);
    }

    // ------------------------------------------------------------------ clicks

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        if (stack.getCount() != 1 || action != ClickAction.SECONDARY) {
            return false;
        }
        ItemStack inSlot = slot.getItem();
        if (inSlot.isEmpty()) {
            removeLast(stack).ifPresent(removed -> {
                this.playRemoveOneSound(player);
                ItemStack rest = slot.safeInsert(removed);
                if (!rest.isEmpty()) {
                    add(stack, rest);
                }
            });
        } else {
            int insertable = getInsertableCount(stack, inSlot);
            if (insertable > 0) {
                ItemStack taken = slot.safeTake(inSlot.getCount(), insertable, player);
                if (add(stack, taken) > 0) {
                    this.playInsertSound(player);
                }
            }
        }
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack otherStack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        if (stack.getCount() != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        if (otherStack.isEmpty()) {
            removeLast(stack).ifPresent(removed -> {
                this.playRemoveOneSound(player);
                slotAccess.set(removed);
            });
        } else {
            int inserted = add(stack, otherStack);
            if (inserted > 0) {
                this.playInsertSound(player);
                otherStack.shrink(inserted);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ bar / tooltip

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return getUsedUnits(stack) > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.min(1 + Mth.floor(12F * getFullness(stack)), 13);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return isFull(stack) ? BAR_COLOR_FULL : BAR_COLOR;
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        List<ItemStack> contents = getStoredAmmo(stack).toList();
        return Optional.of(new AmmoBoxTooltip(contents, getUsedUnits(stack), getMaxItemCount(stack)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        // Not super: Create's package tooltip would list the contents a second time.
        if (stack.has(AllDataComponents.PACKAGE_ADDRESS)) {
            tooltip.add(Component.literal("→ " + getAddress(stack)).withStyle(ChatFormatting.GOLD));
        }
        int rounds = getStoredAmmo(stack).mapToInt(ItemStack::getCount).sum();
        int used = getUsedUnits(stack);
        int stacks = getStackCapacity(stack);
        String usedStacks = String.format(java.util.Locale.ROOT, "%.1f", used / (float) UNITS_PER_STACK);
        tooltip.add(Component.translatable("item.scguns.ammo_box.rounds", rounds).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.scguns.ammo_box.capacity", usedStacks, stacks)
                .withStyle(isFull(stack) ? ChatFormatting.RED : ChatFormatting.GRAY));
        if (rounds == 0) {
            tooltip.add(Component.translatable("item.scguns.ammo_box.hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    protected void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    // ------------------------------------------------------------------ storage

    /**
     * Inserts as much of {@code insertedStack} as fits. Does not modify {@code insertedStack}.
     *
     * @return the number of items actually stored
     */
    public static int add(ItemStack box, ItemStack insertedStack) {
        int toInsert = getInsertableCount(box, insertedStack);
        if (toInsert <= 0) {
            return 0;
        }
        List<ItemStack> contents = new ArrayList<>(getStoredAmmo(box).map(ItemStack::copy).toList());
        int remaining = toInsert;
        for (ItemStack existing : contents) {
            if (remaining <= 0) {
                break;
            }
            if (ItemStack.isSameItemSameComponents(existing, insertedStack)) {
                int space = Math.min(existing.getMaxStackSize() - existing.getCount(), remaining);
                if (space > 0) {
                    existing.grow(space);
                    remaining -= space;
                }
            }
        }
        while (remaining > 0) {
            int count = Math.min(insertedStack.getMaxStackSize(), remaining);
            contents.add(insertedStack.copyWithCount(count));
            remaining -= count;
        }
        setContents(box, contents, null);
        return toInsert;
    }

    public static int getTotalItemCount(ItemStack stack) {
        return getStoredAmmo(stack).mapToInt(ItemStack::getCount).sum();
    }

    public static int getAmmoCount(ItemStack stack, Item ammoItem) {
        if (!(stack.getItem() instanceof AmmoBoxItem)) {
            return 0;
        }
        if (stack.getItem() instanceof CreativeAmmoBoxItem creative) {
            TagKey<Item> ammoTag = ItemTags.create(creative.getAmmoTag());
            return ammoItem.builtInRegistryHolder().is(ammoTag) ? Integer.MAX_VALUE : 0;
        }
        return getStoredAmmo(stack)
                .filter(contained -> contained.getItem() == ammoItem)
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public static boolean containsAmmo(ItemStack stack, Item ammoItem) {
        return getAmmoCount(stack, ammoItem) > 0;
    }

    public static int shrinkAmmo(ItemStack stack, Item ammoItem, int amount, HolderLookup.Provider provider) {
        if (amount <= 0 || !(stack.getItem() instanceof AmmoBoxItem)) {
            return 0;
        }
        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
            return containsAmmo(stack, ammoItem) ? amount : 0;
        }

        int remaining = amount;
        List<ItemStack> contents = new ArrayList<>(getStoredAmmo(stack).map(ItemStack::copy).toList());
        // Take from the last (most recently added, usually partial) stacks first so full stacks stay together.
        for (int i = contents.size() - 1; i >= 0 && remaining > 0; i--) {
            ItemStack contained = contents.get(i);
            if (contained.getItem() == ammoItem) {
                int consumed = Math.min(remaining, contained.getCount());
                contained.shrink(consumed);
                remaining -= consumed;
            }
        }

        int consumed = amount - remaining;
        if (consumed > 0) {
            setContents(stack, contents, provider);
        }
        return consumed;
    }

    /**
     * Writes the contents into Create's package contents, packed into the first slots so the last
     * non-empty slot is always the most recently stored stack.
     */
    public static void setContents(ItemStack stack, List<ItemStack> contents, HolderLookup.Provider provider) {
        if (!(stack.getItem() instanceof AmmoBoxItem) || stack.getItem() instanceof CreativeAmmoBoxItem) {
            return;
        }
        List<ItemStack> packed = contents.stream().filter(s -> !s.isEmpty()).limit(SLOTS).toList();
        if (packed.isEmpty()) {
            stack.remove(AllDataComponents.PACKAGE_CONTENTS);
        } else {
            stack.set(AllDataComponents.PACKAGE_CONTENTS, ItemContainerContents.fromItems(packed));
        }
    }

    /** Removes the most recently stored stack (vanilla bundle behaviour). */
    private static Optional<ItemStack> removeLast(ItemStack stack) {
        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
            return Optional.empty();
        }
        List<ItemStack> contents = new ArrayList<>(getStoredAmmo(stack).map(ItemStack::copy).toList());
        if (contents.isEmpty()) {
            return Optional.empty();
        }
        ItemStack removed = contents.remove(contents.size() - 1);
        setContents(stack, contents, null);
        return Optional.of(removed);
    }

    /** Non-empty stacks in the box, in slot order. A creative box yields one endless stack of every ammo. */
    public static Stream<ItemStack> getStoredAmmo(ItemStack stack) {
        if (stack.getItem() instanceof CreativeAmmoBoxItem creative) {
            TagKey<Item> ammoTag = ItemTags.create(creative.getAmmoTag());
            return BuiltInRegistries.ITEM.stream()
                    .filter(item -> item.builtInRegistryHolder().is(ammoTag))
                    .map(item -> new ItemStack(item, Integer.MAX_VALUE));
        }
        return stack.getOrDefault(AllDataComponents.PACKAGE_CONTENTS, ItemContainerContents.EMPTY)
                .stream()
                .filter(s -> !s.isEmpty());
    }
}
