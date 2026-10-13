package top.ribs.scguns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Three-frame, three-tick full-bright flash where a bullet strikes a block.
 */
@OnlyIn(Dist.CLIENT)
public class ImpactFlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected ImpactFlashParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.lifetime = 3;
        this.gravity = 0F;
        this.hasPhysics = false;
        this.quadSize = 0.16F + this.random.nextFloat() * 0.06F;
        this.roll = this.random.nextInt(4) * Mth.HALF_PI;
        this.oRoll = this.roll;
        this.setSprite(sprites.get(0, 2));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (++this.age >= this.lifetime) {
            this.remove();
            return;
        }
        // one sprite frame per tick: ignite, full flash, broken ring
        this.setSprite(this.sprites.get(this.age, 2));
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new ImpactFlashParticle(level, x, y, z, this.sprites);
        }
    }
}
