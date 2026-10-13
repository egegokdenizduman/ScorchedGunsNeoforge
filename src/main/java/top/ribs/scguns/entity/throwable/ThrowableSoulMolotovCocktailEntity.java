package top.ribs.scguns.entity.throwable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModEntities;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.world.ProjectileExplosion;

/**
 * Soul molotov (the original fork's "Hellfire Bomb"): shatters like a molotov but spreads short-lived soul fire.
 */
public class ThrowableSoulMolotovCocktailEntity extends ThrowableGrenadeEntity
{
    private static final float RADIUS = 2.5F;

    public ThrowableSoulMolotovCocktailEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public ThrowableSoulMolotovCocktailEntity(Level world, LivingEntity entity, int timeLeft)
    {
        super(ModEntities.THROWABLE_SOUL_MOLOTOV_COCKTAIL.get(), world, entity);
        this.setShouldBounce(false);
        this.setItem(new ItemStack(ModItems.SOUL_MOLOTOV_COCKTAIL.get()));
        this.setMaxLife(20 * 3);
    }

    @Override
    public void particleTick()
    {
        if (this.level().isClientSide)
        {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void onDeath()
    {
        double y = this.getY() + this.getType().getDimensions().height() * 0.5;
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 2, 1);
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 2, 0.8F);
        this.level().playSound(null, this.getX(), y, this.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.5F, 1);
        createSoulFireExplosion(this, RADIUS);
    }

    private static void createSoulFireExplosion(Entity entity, float radius)
    {
        Level world = entity.level();
        if (world.isClientSide())
            return;

        // Same harmless "pop" as the regular molotov; the fire is placed by hand below so it is soul fire.
        DamageSource source = entity.damageSources().explosion(entity, entity instanceof ThrowableItemEntity throwable ? throwable.getOwner() : null);
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), 2.0F, false, Explosion.BlockInteraction.KEEP)
        {
            @Override
            protected float getEntityDamageAmount(Entity entity, double distance)
            {
                return 0;
            }
        };
        if (EventHooks.onExplosionStart(world, explosion))
            return;
        explosion.explode();
        explosion.finalizeExplosion(true);

        BlockPos center = entity.blockPosition();
        int r = (int) Math.ceil(radius);
        double radiusSq = radius * radius;
        for (int x = -r; x <= r; x++)
        {
            for (int z = -r; z <= r; z++)
            {
                if (x * x + z * z > radiusSq)
                    continue;
                // Outer ring is patchier, like a splash of burning liquid.
                if (x * x + z * z > 2 && world.random.nextInt(3) == 0)
                    continue;
                for (int dy = 1; dy >= -2; dy--)
                {
                    BlockPos pos = center.offset(x, dy, z);
                    BlockPos below = pos.below();
                    BlockState belowState = world.getBlockState(below);
                    if (world.getBlockState(pos).isAir() && belowState.isFaceSturdy(world, below, Direction.UP))
                    {
                        world.setBlock(pos, ModBlocks.FAKE_SOUL_FIRE.get().defaultBlockState(), 3);
                        break;
                    }
                }
            }
        }

        if (world instanceof ServerLevel serverLevel)
        {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, entity.getX(), entity.getY() + 0.25, entity.getZ(), 30, 1.0, 0.3, 1.0, 0.04);
            serverLevel.sendParticles(ParticleTypes.SOUL, entity.getX(), entity.getY() + 0.25, entity.getZ(), 10, 1.0, 0.3, 1.0, 0.03);
        }
    }
}
