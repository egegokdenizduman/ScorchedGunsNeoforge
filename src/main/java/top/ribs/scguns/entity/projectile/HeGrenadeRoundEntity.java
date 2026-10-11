package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

public class HeGrenadeRoundEntity extends ProjectileEntity {
    private static final float EXPLOSION_RADIUS = 2.5F;

    public HeGrenadeRoundEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public HeGrenadeRoundEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && this.tickCount > 1) {
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        explode();
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        explode();
    }

    @Override
    public void onExpired() {
        explode();
    }

    protected void explode() {
        if (!this.level().isClientSide) {
            createRocketExplosion(this, EXPLOSION_RADIUS, this.getDamage(), true);
        }
        this.discard();
    }
}
