package top.ribs.scguns.mixin.common.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import top.ribs.scguns.item.BrokenMoldItem;

/**
 * Spouting liquid experience on a broken Mending mold may be handled by Create: Enchantment Industry's own spout
 * repair, which never goes through the mold's fluid handler. Whatever repaired it, a broken mold coming out of a spout
 * with any durability restored becomes its working mold right away.
 */
@Pseudo
@Mixin(targets = "com.simibubi.create.content.fluids.spout.SpoutBlockEntity", remap = false)
public abstract class SpoutFillMoldMixin {
    @WrapOperation(method = "*", remap = false,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lcom/simibubi/create/content/fluids/spout/FillingBySpout;fillItem(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/item/ItemStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack scguns$restoreRepairedMold(Level level, int amount, ItemStack stack, FluidStack fluid, Operation<ItemStack> original) {
        return BrokenMoldItem.restoreIfRepaired(original.call(level, amount, stack, fluid));
    }
}
