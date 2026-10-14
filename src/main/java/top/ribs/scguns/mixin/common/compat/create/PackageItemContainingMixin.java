package top.ribs.scguns.mixin.common.compat.create;

import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.item.ammo_boxes.AmmoBoxConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * Every box a packager makes comes from {@link PackageItem#containing(ItemStackHandler)}, wrapped in a random
 * cardboard style. When everything inside is one ammo type (or all empty casings), use that ammo box instead.
 * The packager adds the address and order data afterwards, so only the contents need carrying over.
 */
@Mixin(value = PackageItem.class, remap = false)
public abstract class PackageItemContainingMixin {
    @Inject(method = "containing(Lnet/neoforged/neoforge/items/ItemStackHandler;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), cancellable = true, remap = false)
    private static void scguns$useAmmoBox(ItemStackHandler stacks, CallbackInfoReturnable<ItemStack> cir) {
        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < stacks.getSlots(); i++) {
            contents.add(stacks.getStackInSlot(i));
        }
        AmmoBoxConversion.boxForAll(contents).ifPresent(item -> {
            ItemStack box = new ItemStack(item);
            box.applyComponents(cir.getReturnValue().getComponentsPatch());
            cir.setReturnValue(box);
        });
    }
}
