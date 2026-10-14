package top.ribs.scguns.item.ammo_boxes;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.AmmoBoxItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Turns a plain, empty Create package into an ammo box: right-click it with any ammo of a type, either in
 * the inventory (either way round, like a bundle) or while it is placed in the world. The clicked ammo goes
 * into the new box. Any empty casing makes a casing pouch.
 */
@EventBusSubscriber(modid = Reference.MOD_ID)
public final class AmmoBoxConversion {
    /** Lookup order; ammo in several tags (e.g. hog rounds) becomes the first box that takes it. */
    private static final List<Supplier<Item>> BOXES = List.of(
            ModItems.PISTOL_AMMO_BOX, ModItems.RIFLE_AMMO_BOX, ModItems.SHOTGUN_AMMO_BOX,
            ModItems.MAGNUM_AMMO_BOX, ModItems.ENERGY_AMMO_BOX, ModItems.ROCKET_AMMO_BOX,
            ModItems.SPECIAL_AMMO_BOX, ModItems.EMPTY_CASING_POUCH);

    private AmmoBoxConversion() {
    }

    /** The box that takes this ammo, if any. Never the creative box. */
    public static Optional<AmmoBoxItem> boxFor(ItemStack ammo) {
        return boxForAll(List.of(ammo));
    }

    /** The first box that takes every one of these (non-empty) stacks, if any. */
    public static Optional<AmmoBoxItem> boxForAll(List<ItemStack> stacks) {
        List<ItemStack> items = stacks.stream().filter(s -> !s.isEmpty()).toList();
        if (items.isEmpty()) {
            return Optional.empty();
        }
        for (Supplier<Item> supplier : BOXES) {
            if (supplier.get() instanceof AmmoBoxItem box && items.stream().allMatch(box::canHold)) {
                return Optional.of(box);
            }
        }
        return Optional.empty();
    }

    /** A Create package that is not one of ours and holds nothing. */
    public static boolean isPlainEmptyPackage(ItemStack stack) {
        return stack.getCount() == 1 && PackageItem.isPackage(stack) && !(stack.getItem() instanceof AmmoBoxItem)
                && stack.getOrDefault(AllDataComponents.PACKAGE_CONTENTS, ItemContainerContents.EMPTY).nonEmptyStream().findAny().isEmpty();
    }

    /**
     * Builds the ammo box for {@code plainBox} filled with as much of {@code ammo} as fits.
     * Does not modify either stack; the caller shrinks {@code ammo} by the inserted count.
     */
    private static Optional<Converted> convert(ItemStack plainBox, ItemStack ammo) {
        if (!isPlainEmptyPackage(plainBox)) {
            return Optional.empty();
        }
        return boxFor(ammo).map(item -> {
            ItemStack box = new ItemStack(item);
            if (plainBox.has(AllDataComponents.PACKAGE_ADDRESS)) {
                box.set(AllDataComponents.PACKAGE_ADDRESS, plainBox.get(AllDataComponents.PACKAGE_ADDRESS));
            }
            int inserted = AmmoBoxItem.add(box, ammo);
            return new Converted(box, inserted);
        });
    }

    private record Converted(ItemStack box, int inserted) {
    }

    @SubscribeEvent
    public static void onStackedOnOther(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() != ClickAction.SECONDARY) {
            return;
        }
        Slot slot = event.getSlot();
        Player player = event.getPlayer();
        if (!slot.allowModification(player)) {
            return;
        }
        ItemStack carried = event.getCarriedItem();
        ItemStack inSlot = event.getStackedOnItem();

        // Ammo on the cursor, clicked onto a package in a slot.
        Optional<Converted> intoSlot = convert(inSlot, carried);
        if (intoSlot.isPresent()) {
            carried.shrink(intoSlot.get().inserted());
            slot.set(intoSlot.get().box());
            playConvertSound(player);
            event.setCanceled(true);
            return;
        }

        // Package on the cursor, clicked onto ammo in a slot.
        Optional<Converted> onCursor = convert(carried, inSlot);
        if (onCursor.isPresent()) {
            slot.safeTake(inSlot.getCount(), onCursor.get().inserted(), player);
            event.getCarriedSlotAccess().set(onCursor.get().box());
            playConvertSound(player);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof PackageEntity packageEntity)) {
            return;
        }
        ItemStack held = event.getItemStack();
        Optional<Converted> converted = convert(packageEntity.getBox(), held);
        if (converted.isEmpty()) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        if (event.getLevel().isClientSide) {
            return;
        }
        // The box is only sent to clients when the entity spawns, so swap in a new entity.
        PackageEntity replacement = PackageEntity.fromItemStack(event.getLevel(), packageEntity.position(), converted.get().box());
        replacement.setYRot(packageEntity.getYRot());
        replacement.setYHeadRot(packageEntity.getYRot());
        packageEntity.discard();
        event.getLevel().addFreshEntity(replacement);
        if (!event.getEntity().getAbilities().instabuild) {
            held.shrink(converted.get().inserted());
        }
        playConvertSound(event.getEntity());
    }

    private static void playConvertSound(Player player) {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }
}
