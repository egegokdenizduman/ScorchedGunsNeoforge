package top.ribs.scguns.mixin.common.compat.create;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.item.MoldItem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Create builds basin recipe remainders from the input slots after extracting the ingredients, so a single (unstackable)
 * mold is gone before its crafting remainder can be computed. This keeps molds in the basin: they are remembered before
 * the recipe applies and the damaged mold (or nothing, if it broke) is put back into the same input slot afterwards.
 */
@Pseudo
@Mixin(targets = "com.simibubi.create.content.processing.basin.BasinRecipe", remap = false)
public abstract class BasinRecipeMoldMixin {
    private static final ThreadLocal<List<ItemStack>> SCGUNS_MOLDS = new ThreadLocal<>();

    @Inject(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At("HEAD"), remap = false)
    private static void scguns$rememberMolds(@Coerce Object basin, Recipe<?> recipe, boolean test, CallbackInfoReturnable<Boolean> cir) {
        SCGUNS_MOLDS.remove();
        if (test) {
            return;
        }
        IItemHandler inventory = scguns$getInput(basin);
        if (inventory == null) {
            return;
        }
        List<ItemStack> molds = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            molds.add(stack.getItem() instanceof MoldItem ? stack.copy() : ItemStack.EMPTY);
        }
        SCGUNS_MOLDS.set(molds);
    }

    @Inject(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At("RETURN"), remap = false)
    private static void scguns$returnMolds(@Coerce Object basin, Recipe<?> recipe, boolean test, CallbackInfoReturnable<Boolean> cir) {
        List<ItemStack> molds = SCGUNS_MOLDS.get();
        SCGUNS_MOLDS.remove();
        if (test || molds == null || !Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        if (!(scguns$getInput(basin) instanceof IItemHandlerModifiable inventory)) {
            return;
        }
        for (int i = 0; i < molds.size() && i < inventory.getSlots(); i++) {
            ItemStack before = molds.get(i);
            if (before.isEmpty() || !inventory.getStackInSlot(i).isEmpty()) {
                continue;
            }
            // The mold was used by the recipe: return it with one use of wear (Unbreaking applies)
            inventory.setStackInSlot(i, before.getItem().getCraftingRemainingItem(before));
        }
    }

    private static IItemHandler scguns$getInput(Object basin) {
        try {
            Method method = basin.getClass().getMethod("getInputInventory");
            Object inventory = method.invoke(basin);
            return inventory instanceof IItemHandler handler ? handler : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
