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
