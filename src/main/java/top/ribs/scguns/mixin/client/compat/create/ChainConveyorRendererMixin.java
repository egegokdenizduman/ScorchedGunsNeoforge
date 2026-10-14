package top.ribs.scguns.mixin.client.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.ribs.scguns.client.render.AmmoBoxPackageModels;

/**
 * Packages on chain conveyors (non-Flywheel path); the key is cached on the package and also picks its rigging.
 * Swap the item-id model key for the ammo box's fill-state key (see {@link AmmoBoxPackageModels#modelKey}).
 */
@Mixin(value = ChainConveyorRenderer.class, remap = false)
public abstract class ChainConveyorRendererMixin {
    @WrapOperation(method = "renderBox", remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation scguns$fillStateModel(DefaultedRegistry<?> registry, Object item, Operation<ResourceLocation> original,
                                                   @Local(argsOnly = true) ChainConveyorPackage box) {
        return AmmoBoxPackageModels.modelKey(box.item, original.call(registry, item));
    }
}
