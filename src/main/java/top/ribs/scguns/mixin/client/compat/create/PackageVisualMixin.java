package top.ribs.scguns.mixin.client.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.box.PackageVisual;
import com.simibubi.create.content.logistics.box.PackageEntity;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.ribs.scguns.client.render.AmmoBoxPackageModels;

/**
 * Flywheel path for placed and thrown packages: the model is chosen once, when the visual is created.
 * Swap the item-id model key for the ammo box's fill-state key (see {@link AmmoBoxPackageModels#modelKey}).
 */
@Mixin(value = PackageVisual.class, remap = false)
public abstract class PackageVisualMixin {
    @WrapOperation(method = "<init>", remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation scguns$fillStateModel(DefaultedRegistry<?> registry, Object item, Operation<ResourceLocation> original,
                                                   @Local(argsOnly = true) PackageEntity entity) {
        return AmmoBoxPackageModels.modelKey(entity.box, original.call(registry, item));
    }
}
