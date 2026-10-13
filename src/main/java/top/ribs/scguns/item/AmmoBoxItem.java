package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.ammo_boxes.AmmoBoxTooltip;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bundle-style ammo container.
 *
 * Capacity is measured in stacks, like a vanilla bundle: every stored item weighs
 * {@code 64 / maxStackSize} units and a box holds {@code stacks * 64} units. A box with
 * 5 stacks therefore holds 320 rounds of a 64-stacking round, or 80 rockets that stack
 * to 16. The item bar, the fill-level model predicate and the tooltip all read the
 * same {@link #getFullness(ItemStack)} so they always agree.
 *
 * Clicks (same as a vanilla bundle):
 * - right-click the box onto a slot with matching ammo: pulls as much as fits;
 * - right-click ammo onto the box: puts as much as fits into it;
 * - right-click the box onto an empty slot / with an empty cursor: takes out the most
 *   recently stored stack.
 */
public abstract class AmmoBoxItem extends Item {
    public static final String TAG_ITEMS = "Items";
    /** Weight units of one full stack. */
    public static final int UNITS_PER_STACK = 64;
    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);
    private static final int BAR_COLOR_FULL = Mth.color(1.0F, 0.33F, 0.33F);

    public AmmoBoxItem(Item.Properties properties) {
        super(properties);
    }

    protected abstract ResourceLocation getAmmoTag();

    /** How many full stacks the box holds before the capacity multiplier is applied. */
    protected abstract int getBaseStackCapacity();

    public boolean canHold(ItemStack stack) {
        return !stack.isEmpty() && !(stack.getItem() instanceof AmmoBoxItem) && stack.is(ItemTags.create(getAmmoTag()));
    }

    // ------------------------------------------------------------------ capacity

    /** Stacks this box holds, including the config multiplier. */
    public static int getStackCapacity(ItemStack box) {
        if (!(box.getItem() instanceof AmmoBoxItem item)) {
            return 0;
        }
        int base = item.getBaseStackCapacity();
        if (base >= Integer.MAX_VALUE / UNITS_PER_STACK) {
            return base;
        }
        double multiplier = Config.COMMON.gameplay.ammoBoxCapacityMultiplier.get();
        return Math.max(1, (int) Math.round(base * multiplier));
    }

    /** Capacity in weight units ({@link #UNITS_PER_STACK} per stack). */
    public static int getMaxItemCount(ItemStack box) {
        int stacks = getStackCapacity(box);
        if (stacks >= Integer.MAX_VALUE / UNITS_PER_STACK) {
            return Integer.MAX_VALUE;
        }
        return stacks * UNITS_PER_STACK;
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
        return getContents(box).mapToInt(s -> s.getCount() * getWeight(s)).sum();
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

    /** How many of {@code toInsert} would fit into the box right now. */
    public static int getInsertableCount(ItemStack box, ItemStack toInsert) {
        if (!(box.getItem() instanceof AmmoBoxItem item) || item instanceof CreativeAmmoBoxItem || !item.canHold(toInsert)) {
            return 0;
        }
        int free = getMaxItemCount(box) - getUsedUnits(box);
        if (free <= 0) {
            return 0;
        }
        return Math.min(toInsert.getCount(), free / getWeight(toInsert));
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
        List<ItemStack> contents = getContents(stack).filter(s -> !s.isEmpty()).toList();
        return Optional.of(new AmmoBoxTooltip(contents, getUsedUnits(stack), getMaxItemCount(stack)));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        int rounds = getContents(stack).mapToInt(ItemStack::getCount).sum();
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

    @Override
    public void onDestroyed(@NotNull ItemEntity itemEntity) {
        ItemUtils.onContainerDestroyed(itemEntity, getContents(itemEntity.getItem()).toList());
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
    public static int add(ItemStack pouchStack, ItemStack insertedStack) {
        int toInsert = getInsertableCount(pouchStack, insertedStack);
        if (toInsert <= 0) {
            return 0;
        }
        List<ItemStack> contents = getContents(pouchStack).filter(s -> !s.isEmpty()).collect(Collectors.toCollection(ArrayList::new));
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
        setContents(pouchStack, contents, Gun.builtInRegistryProvider());
        return toInsert;
    }

    public static int getTotalItemCount(ItemStack stack) {
        return getContents(stack).mapToInt(ItemStack::getCount).sum();
    }

    public static int getAmmoCount(ItemStack stack, Item ammoItem) {
        if (!(stack.getItem() instanceof AmmoBoxItem)) {
            return 0;
        }
        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
            TagKey<Item> ammoTag = ItemTags.create(((CreativeAmmoBoxItem) stack.getItem()).getAmmoTag());
            return ammoItem.builtInRegistryHolder().is(ammoTag) ? Integer.MAX_VALUE : 0;
        }
        return getContents(stack)
                .filter(contained -> !contained.isEmpty() && contained.getItem() == ammoItem)
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
        List<ItemStack> contents = getContents(stack).collect(Collectors.toCollection(ArrayList::new));
        // Take from the last (most recently added, usually partial) stacks first so full stacks stay together.
        for (int i = contents.size() - 1; i >= 0 && remaining > 0; i--) {
            ItemStack contained = contents.get(i);
            if (!contained.isEmpty() && contained.getItem() == ammoItem) {
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

    public static void setContents(ItemStack stack, List<ItemStack> contents, HolderLookup.Provider provider) {
        if (!(stack.getItem() instanceof AmmoBoxItem) || stack.getItem() instanceof CreativeAmmoBoxItem) {
            return;
        }

        CompoundTag compoundTag = getCustomData(stack);
        ListTag listTag = new ListTag();
        for (ItemStack contained : contents) {
            if (!contained.isEmpty()) {
                listTag.add(saveStack(contained, provider));
            }
        }

        if (listTag.isEmpty()) {
            compoundTag.remove(TAG_ITEMS);
        } else {
            compoundTag.put(TAG_ITEMS, listTag);
        }
        setCustomData(stack, compoundTag);
    }

    /** Removes the most recently stored stack (vanilla bundle behaviour). */
    private static Optional<ItemStack> removeLast(ItemStack stack) {
        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
            return Optional.empty();
        }
        List<ItemStack> contents = getContents(stack).filter(s -> !s.isEmpty()).collect(Collectors.toCollection(ArrayList::new));
        if (contents.isEmpty()) {
            return Optional.empty();
        }
        ItemStack removed = contents.remove(contents.size() - 1);
        setContents(stack, contents, Gun.builtInRegistryProvider());
        return Optional.of(removed);
    }

    public static Stream<ItemStack> getContents(ItemStack stack) {
        if (stack.getItem() instanceof CreativeAmmoBoxItem) {
            TagKey<Item> ammoTag = ItemTags.create(((CreativeAmmoBoxItem) stack.getItem()).getAmmoTag());
            return BuiltInRegistries.ITEM.stream()
                    .filter(item -> item.builtInRegistryHolder().is(ammoTag))
                    .map(item -> new ItemStack(item, Integer.MAX_VALUE));
        }
        CompoundTag compoundTag = getCustomData(stack);
        ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
        return listTag.stream()
                .map(CompoundTag.class::cast)
                .map(tag -> ItemStack.parseOptional(Gun.builtInRegistryProvider(), tag));
    }

    private static CompoundTag saveStack(ItemStack stack, HolderLookup.Provider provider) {
        return (CompoundTag) stack.save(provider, new CompoundTag());
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
