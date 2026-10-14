package top.ribs.scguns.mixin.client.compat.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.box.PackageRenderer;
import com.simibubi.create.content.logistics.box.PackageEntity;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.ribs.scguns.client.render.AmmoBoxPackageModels;

/**
 * Placed and thrown packages (non-Flywheel path) pick their model from AllPartialModels.PACKAGES by item id.
 * Swap the item-id model key for the ammo box's fill-state key (see {@link AmmoBoxPackageModels#modelKey}).
 */
@Mixin(value = PackageRenderer.class, remap = false)
public abstract class PackageRendererMixin {
    @WrapOperation(method = "render(Lcom/simibubi/create/content/logistics/box/PackageEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation scguns$fillStateModel(DefaultedRegistry<?> registry, Object item, Operation<ResourceLocation> original,
                                                   @Local(argsOnly = true) PackageEntity entity) {
        return AmmoBoxPackageModels.modelKey(entity.box, original.call(registry, item));
    }
}
