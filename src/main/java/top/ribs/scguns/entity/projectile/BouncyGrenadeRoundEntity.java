package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

public class BouncyGrenadeRoundEntity extends ProjectileEntity {
    private static final float EXPLOSION_RADIUS = 2.0F;
    private static final int MAX_BOUNCES = 3;
    private static final double BOUNCE_VELOCITY_RETENTION = 0.75D;
    private static final double MIN_BOUNCE_VELOCITY = 0.05D;
    private int bouncesLeft = MAX_BOUNCES;

    public BouncyGrenadeRoundEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public BouncyGrenadeRoundEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    public void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
        if (result instanceof BlockHitResult blockHitResult && blockHitResult.getType() != HitResult.Type.MISS) {
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = this.level().getBlockState(pos);
            if (bouncesLeft > 0 && canBounce() && !state.canBeReplaced()) {
                bounce(blockHitResult.getDirection(), result.getLocation());
                bouncesLeft--;
                return;
            }
        }
        super.onHit(result, startVec, endVec);
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide && this.tickCount > 1 && this.tickCount % 3 == 0) {
            this.level().addParticle(ParticleTypes.CRIT, true, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
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

    private void bounce(Direction face, Vec3 hitPos) {
        Vec3 velocity = this.getDeltaMovement();
        Vec3 bounced = switch (face.getAxis()) {
            case X -> new Vec3(-velocity.x, velocity.y, velocity.z);
            case Y -> new Vec3(velocity.x, -velocity.y, velocity.z);
            case Z -> new Vec3(velocity.x, velocity.y, -velocity.z);
        };
        bounced = bounced.scale(BOUNCE_VELOCITY_RETENTION).add(
                (this.random.nextDouble() - 0.5D) * 0.05D,
                (this.random.nextDouble() - 0.5D) * 0.05D,
                (this.random.nextDouble() - 0.5D) * 0.05D);
        this.setDeltaMovement(bounced);
        this.setPos(hitPos.add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.2D)));
        this.level().playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.SLIME_BLOCK_HIT, SoundSource.NEUTRAL, 0.8F, 0.8F + this.random.nextFloat() * 0.4F);
        if (!this.level().isClientSide) {
            for (int i = 0; i < 6; i++) {
                ((net.minecraft.server.level.ServerLevel) this.level()).sendParticles(ParticleTypes.CRIT, hitPos.x, hitPos.y, hitPos.z, 1,
                        (this.random.nextDouble() - 0.5D) * 0.4D,
                        this.random.nextDouble() * 0.4D,
                        (this.random.nextDouble() - 0.5D) * 0.4D,
                        0.05D);
            }
        }
    }

    private boolean canBounce() {
        return this.getDeltaMovement().length() > MIN_BOUNCE_VELOCITY;
    }

    private void explode() {
        if (!this.level().isClientSide) {
            createRocketExplosion(this, EXPLOSION_RADIUS, this.getDamage(), true);
        }
        this.discard();
    }
}
