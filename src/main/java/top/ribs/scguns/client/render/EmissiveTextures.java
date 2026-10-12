package top.ribs.scguns.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Looks up "_glowmask" companions of textures. A glowmask holds only the emissive pixels of its base texture and is
 * drawn as an extra pass with {@link RenderType#eyes}: vanilla draws it unlit and additive, and Iris routes it through
 * the shader pack's "spidereyes" program, which shader packs such as Complementary / Euphoria Patches render as emissive.
 */
public final class EmissiveTextures {
    private static final Map<ResourceLocation, Optional<ResourceLocation>> CACHE = new ConcurrentHashMap<>();

    private EmissiveTextures() {
    }

    @Nullable
    public static ResourceLocation getGlowmask(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, EmissiveTextures::find).orElse(null);
    }

    @Nullable
    public static RenderType getGlowRenderType(ResourceLocation texture) {
        ResourceLocation glowmask = getGlowmask(texture);
        return glowmask != null ? RenderType.eyes(glowmask) : null;
    }

    /**
     * Adds an emissive copy of a flat textured quad (unit square in the current pose) through {@link RenderType#eyes},
     * so muzzle flashes and similar effects glow under shader packs.
     */
    public static void emissiveQuad(com.mojang.blaze3d.vertex.PoseStack.Pose pose, net.minecraft.client.renderer.MultiBufferSource buffer,
                                    ResourceLocation texture, float minU, float maxU, float alpha) {
        com.mojang.blaze3d.vertex.VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(texture));
        org.joml.Matrix4f matrix = pose.pose();
        float[][] corners = {{0, 0, maxU, 1}, {1, 0, minU, 1}, {1, 1, minU, 0}, {0, 1, maxU, 0}};
        for (float[] c : corners) {
            consumer.addVertex(matrix, c[0], c[1], 0).setColor(alpha, alpha, alpha, 1.0F).setUv(c[2], c[3])
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                    .setLight(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private static Optional<ResourceLocation> find(ResourceLocation texture) {
        String path = texture.getPath();
        if (!path.endsWith(".png")) {
            return Optional.empty();
        }
        ResourceLocation glowmask = ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), path.substring(0, path.length() - 4) + "_glowmask.png");
        return Minecraft.getInstance().getResourceManager().getResource(glowmask).isPresent() ? Optional.of(glowmask) : Optional.empty();
    }
}
