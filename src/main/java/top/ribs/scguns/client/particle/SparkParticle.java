package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.ribs.scguns.particles.SparkData;

/**
 * Small, fast, full-bright spark thrown off a muzzle or a bullet impact. Falls under gravity,
 * bounces off blocks and burns down through 4 sprite frames. Tinted by {@link SparkData#color()}.
 */
@OnlyIn(Dist.CLIENT)
public class SparkParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, int color, SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.sprites = sprites;
        this.lifetime = 5 + this.random.nextInt(6);
        this.gravity = 0.75F;
        this.friction = 0.86F;
        this.hasPhysics = true;
        this.quadSize = 0.035F + this.random.nextFloat() * 0.025F;
        // slight per-spark variation towards white-hot
        float hot = this.random.nextFloat() * 0.25F;
        this.rCol = Math.min(1F, ((color >> 16) & 0xFF) / 255F + hot);
        this.gCol = Math.min(1F, ((color >> 8) & 0xFF) / 255F + hot);
        this.bCol = Math.min(1F, (color & 0xFF) / 255F + hot);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SparkData> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SparkData data, @NotNull ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new SparkParticle(level, x, y, z, xd, yd, zd, data.color(), this.sprites);
        }
    }
}
