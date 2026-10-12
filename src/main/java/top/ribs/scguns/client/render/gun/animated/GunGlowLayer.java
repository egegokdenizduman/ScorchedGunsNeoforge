package top.ribs.scguns.client.render.gun.animated;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import top.ribs.scguns.item.animated.AnimatedGunItem;

/**
 * Re-renders the gun once more in an emissive ("eyes") pass. {@link AnimatedGunRenderer} decides per bone what goes
 * into that pass: bones named glow / glow_2 / glow_3 use the base texture, every other bone uses the gun's
 * "_glowmask" texture when one exists.
 */
public class GunGlowLayer extends GeoRenderLayer<AnimatedGunItem> {
    private final AnimatedGunRenderer gunRenderer;

    public GunGlowLayer(AnimatedGunRenderer renderer) {
        super(renderer);
        this.gunRenderer = renderer;
    }

    @Override
    public void render(PoseStack poseStack, AnimatedGunItem animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!this.gunRenderer.hasGlowContent(animatable, bakedModel)) {
            return;
        }
        RenderType glowType = RenderType.eyes(this.gunRenderer.getTextureLocation(animatable));
        this.gunRenderer.setGlowPass(true);
        try {
            this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glowType,
                    bufferSource.getBuffer(glowType), partialTick, LightTexture.FULL_BRIGHT, packedOverlay, 0xFFFFFFFF);
        } finally {
            this.gunRenderer.setGlowPass(false);
        }
    }
}
