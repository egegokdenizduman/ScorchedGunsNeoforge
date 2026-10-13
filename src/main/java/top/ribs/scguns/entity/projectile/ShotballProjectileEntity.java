package top.ribs.scguns.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.core.registries.BuiltInRegistries;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.compat.SableBlockInteraction;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.util.GunEnchantmentHelper;

import java.util.ArrayList;
import java.util.List;

public class ShotballProjectileEntity extends ProjectileEntity {
    private static final int MAX_BOUNCES = 3;
    private static final float FIRST_BOUNCE_VELOCITY_BOOST = 1.15F;
    private static final float FIRST_BOUNCE_DAMAGE_BOOST = 1.1F;
    private static final float BOUNCE_VELOCITY_RETENTION = 0.7F;
    private static final float DAMAGE_REDUCTION_PER_BOUNCE = 0.85F;
    private static final float MIN_BOUNCE_VELOCITY = 0.01F;
    private static final int RIDER_IMMUNITY_TICKS = 3; // Adjust as needed
    private static final int MAX_TRACE_STEPS_PER_TICK = 12;
    private static final double PENETRATION_VELOCITY_RETENTION = 0.85;
    private int immunityTicks;

    private int bouncesLeft;
    private float currentDamageMultiplier = 1.0F;
    /** Penetrable blocks left to smash through; ship (Sable sub-level) blocks cost a fraction of one. */
    private float penetrationLeft;

    public ShotballProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.bouncesLeft = MAX_BOUNCES;
    }

    public ShotballProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.bouncesLeft = MAX_BOUNCES;
        this.immunityTicks = RIDER_IMMUNITY_TICKS;
        this.penetrationLeft = Config.COMMON.gameplay.shotball.woodPenetration.get();
    }

    @Override
    public void tick() {
        if (this.immunityTicks > 0) {
            this.immunityTicks--;
        }
        this.updateHeading();
        this.onProjectileTick();

        if (!this.level().isClientSide()) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            this.handleCustomCollisions(startVec, endVec);
        }

        double nextPosX = this.getX() + this.getDeltaMovement().x();
        double nextPosY = this.getY() + this.getDeltaMovement().y();
        double nextPosZ = this.getZ() + this.getDeltaMovement().z();
        this.setPos(nextPosX, nextPosY, nextPosZ);

        if (this.projectile.isGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
        }

        if (this.tickCount >= this.life) {
            if (this.isAlive()) {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
    }
    private boolean isShooterRelatedEntity(Entity entity) {
        if (this.shooter == null) {
            return false;
        }

        if (this.immunityTicks > 0) {
            if (this.shooter.isPassenger() && this.shooter.getVehicle() == entity) {
                return true;
            }

            if (entity.isPassenger() && entity.getVehicle() == this.shooter) {
                return true;
            }

            Entity shooterVehicle = this.shooter.getVehicle();
            if (shooterVehicle != null) {
                if (shooterVehicle.getVehicle() == entity || entity.getVehicle() == shooterVehicle) {
                    return true;
                }
            }
        }

        return false;
    }
    private void handleCustomCollisions(Vec3 startVec, Vec3 endVec) {
        // A fast shotball can smash through several wooden blocks in one tick, so keep tracing
        // from each broken block until it hits something it cannot go through.
        for (int step = 0; step < MAX_TRACE_STEPS_PER_TICK && this.isAlive(); step++) {
            // rayTraceBlocks also traces Sable sub-levels (ships), Valkyrien Skies ships and shield generators.
            BlockHitResult blockResult = rayTraceBlocks(this.level(),
                    new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);

            List<EntityResult> entityResults = this.findShotballEntitiesOnPath(startVec, endVec);

            double blockDistance = Double.MAX_VALUE;
            double entityDistance = Double.MAX_VALUE;

            if (blockResult.getType() != HitResult.Type.MISS) {
                blockDistance = startVec.distanceToSqr(blockResult.getLocation());
            }

            EntityResult closestEntity = null;
            for (EntityResult entityResult : entityResults) {
                double dist = startVec.distanceToSqr(entityResult.getHitPos());
                if (dist < entityDistance) {
                    entityDistance = dist;
                    closestEntity = entityResult;
                }
            }

            if (blockDistance < entityDistance && blockResult.getType() != HitResult.Type.MISS) {
                if (this.tryPenetrate(blockResult)) {
                    Vec3 direction = endVec.subtract(startVec).normalize();
                    startVec = blockResult.getLocation().add(direction.scale(0.05));
                    continue;
                }
                this.handleBlockCollision(blockResult);
            } else if (closestEntity != null) {
                this.handleEntityCollision(closestEntity);
            }
            return;
        }
    }

    /**
     * Smashes through a block from {@link ModTags.Blocks#SHOTBALL_PENETRABLE} (wood, wool, airship envelopes)
     * if the shotball still has penetration left. Blocks on a Sable sub-level (ship) cost less, so the
     * Hullbreaker tears deeper into hulls.
     */
    private boolean tryPenetrate(BlockHitResult blockResult) {
        if (!Config.COMMON.gameplay.griefing.enableBlockBreaking.get()) {
            return false;
        }

        Level hitLevel = SableBlockInteraction.levelFor(this.level(), blockResult);
        BlockPos hitPos = SableBlockInteraction.blockPosFor(blockResult);
        BlockState state = hitLevel.getBlockState(hitPos);
        if (!state.is(ModTags.Blocks.SHOTBALL_PENETRABLE) || state.getDestroySpeed(hitLevel, hitPos) < 0) {
            return false;
        }
        if (hitLevel.getBlockEntity(hitPos) instanceof Container) {
            return false;
        }

        boolean subLevel = SableBlockInteraction.isSubLevelHit(this.level(), blockResult);
        float cost = 1.0F;
        if (subLevel) {
            double multiplier = Config.COMMON.gameplay.shotball.subLevelPenetrationMultiplier.get();
            cost = multiplier <= 0.0 ? Float.MAX_VALUE : (float) (1.0 / multiplier);
        }
        if (this.penetrationLeft < cost - 1.0E-4F) {
            return false;
        }

        Vec3 hitVec = blockResult.getLocation();
        if (!hitLevel.destroyBlock(hitPos, false, this.shooter)) {
            return false;
        }
        this.penetrationLeft -= cost;

        if (subLevel) {
            Vec3 impulse = this.getDeltaMovement().normalize()
                    .scale(Config.COMMON.gameplay.shotball.subLevelImpulse.get() * 0.25);
            SableBlockInteraction.applyImpulse(this.level(), blockResult, hitVec, impulse);
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(PENETRATION_VELOCITY_RETENTION));
        this.level().playSound(null, hitVec.x, hitVec.y, hitVec.z, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.NEUTRAL,
                0.7F, 1.1F + (this.random.nextFloat() - 0.5F) * 0.3F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    hitVec.x, hitVec.y, hitVec.z, 16, 0.3, 0.3, 0.3, 0.15);
        }
        return true;
    }

    /**
     * Detonates the shotball against a block it could not smash through: a small blast, stronger against ships.
     */
    private void detonate(BlockHitResult blockResult) {
        Vec3 hitVec = blockResult.getLocation();
        Vec3 velocity = this.getDeltaMovement();
        Vec3 direction = velocity.lengthSqr() > 1.0E-8 ? velocity.normalize() : Vec3.ZERO;
        boolean subLevel = SableBlockInteraction.isSubLevelHit(this.level(), blockResult);

        float radius = Config.COMMON.gameplay.shotball.explosionRadius.get().floatValue();
        if (subLevel) {
            radius *= Config.COMMON.gameplay.shotball.subLevelExplosionMultiplier.get().floatValue();
        }

        if (subLevel) {
            Vec3 impulse = direction.scale(Config.COMMON.gameplay.shotball.subLevelImpulse.get());
            SableBlockInteraction.applyImpulse(this.level(), blockResult, hitVec, impulse);
        }

        if (radius > 0.0F) {
            // Explode just in front of the surface so the blast starts in open air.
            Vec3 blastPos = hitVec.subtract(direction.scale(0.3));
            this.setPos(blastPos.x, blastPos.y, blastPos.z);
            float damage = this.getDamage() * Config.COMMON.gameplay.shotball.explosionDamageFraction.get().floatValue();
            createRocketExplosion(this, radius, damage, false);
        } else {
            spawnDeathParticles(this.position());
        }
        this.remove(RemovalReason.KILLED);
    }

    private List<EntityResult> findShotballEntitiesOnPath(Vec3 startVec, Vec3 endVec) {
        List<EntityResult> hitEntities = new ArrayList<>();
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);

        for (Entity entity : entities) {
            if (isShooterRelatedEntity(entity)) {
                continue;
            }

            EntityResult result = this.getHitResult(entity, startVec, endVec);
            if (result == null) {
                continue;
            }
            hitEntities.add(result);
        }
        return hitEntities;
    }

    private void handleBlockCollision(BlockHitResult result) {
        Level hitLevel = SableBlockInteraction.levelFor(this.level(), result);
        BlockPos pos = SableBlockInteraction.blockPosFor(result);
        BlockState state = hitLevel.getBlockState(pos);

        if (state.getBlock() instanceof top.ribs.scguns.interfaces.IDamageable damageable) {
            damageable.onBlockDamaged(hitLevel, state, pos, this, this.getDamage(),
                    (int) Math.ceil(this.getDamage() / 2.0) + 1);
        }

        // The shotball no longer bounces off blocks: whatever it cannot smash through, it blows up on.
        this.detonate(result);
    }

    private void handleEntityCollision(EntityResult entityResult) {
        Entity entity = entityResult.getEntity();
        Vec3 hitVec = entityResult.getHitPos();
        boolean headshot = entityResult.isHeadshot();
        float damage = this.getDamage() * currentDamageMultiplier;
        float newDamage = this.getCriticalDamage(this.getWeapon(), this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        damage *= advantageMultiplier(entity);

        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }

        if (entity instanceof LivingEntity livingTarget) {
            damage = applyProjectileProtection(livingTarget, damage);
            damage = calculateArmorBypassDamage(livingTarget, damage);
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, this.shooter);
        boolean blocked = ProjectileHelper.handleShieldHit(entity, this, damage);

        if (!blocked) {
            if (!(entity.getType().is(ModTags.Entities.GHOST) &&
                    !this.getProjectile().getAdvantage().equals(ModTags.Entities.UNDEAD.location()))) {
                if (damage > 0) {
                    entity.hurt(source, damage);
                }

                if (entity instanceof LivingEntity livingEntity) {
                    ResourceLocation effectLocation = this.projectile.getImpactEffect();
                    if (effectLocation != null) {
                        float effectChance = this.projectile.getImpactEffectChance();
                        if (this.random.nextFloat() < effectChance) {
                            var effect = BuiltInRegistries.MOB_EFFECT.getHolder(effectLocation).orElse(null);
                            if (effect != null) {
                                livingEntity.addEffect(new MobEffectInstance(
                                        effect,
                                        this.projectile.getImpactEffectDuration(),
                                        this.projectile.getImpactEffectAmplifier()
                                ));
                            }
                        }
                    }
                }
            }
        }

        if (entity instanceof LivingEntity livingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.getWeapon(), livingEntity);
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL :
                    headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT :
                            S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter,
                    new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }

        PacketHandler.getPlayChannel().sendToTrackingEntity(() -> entity,
                new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));

        if (bouncesLeft > 0 && canBounce()) {
            bounceOffEntity(entity, hitVec);
            bouncesLeft--;

            if (bouncesLeft == MAX_BOUNCES - 2) {
                currentDamageMultiplier *= FIRST_BOUNCE_DAMAGE_BOOST;
            } else {
                currentDamageMultiplier *= DAMAGE_REDUCTION_PER_BOUNCE;
            }

            this.level().playSound(null, hitVec.x, hitVec.y, hitVec.z, SoundEvents.SLIME_BLOCK_HIT, SoundSource.NEUTRAL,
                    0.6F, 1.0F + (this.random.nextFloat() - 0.5F) * 0.4F);

            spawnBounceParticles(hitVec);
        } else {
            spawnDeathParticles(this.position());
            this.remove(RemovalReason.KILLED);
        }

        entity.invulnerableTime = 0;
    }

    private void bounceOffEntity(Entity entity, Vec3 hitPos) {
        Vec3 velocity = this.getDeltaMovement();
        Vec3 entityCenter = entity.getBoundingBox().getCenter();
        Vec3 bounceDirection = this.position().subtract(entityCenter).normalize();

        Vec3 newVelocity = bounceDirection.scale(velocity.length() * BOUNCE_VELOCITY_RETENTION);

        if (bouncesLeft == MAX_BOUNCES - 1) {
            newVelocity = newVelocity.scale(FIRST_BOUNCE_VELOCITY_BOOST);
        }

        newVelocity = newVelocity.add(
                (this.random.nextDouble() - 0.5) * 0.1,
                (this.random.nextDouble() - 0.5) * 0.1,
                (this.random.nextDouble() - 0.5) * 0.1
        );

        this.setDeltaMovement(newVelocity);
        this.setPos(hitPos.add(bounceDirection.scale(0.3)));
    }

    private boolean canBounce() {
        double currentSpeed = this.getDeltaMovement().length();
        return currentSpeed > MIN_BOUNCE_VELOCITY;
    }

    private void spawnBounceParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 8; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.5;
                double velocityY = this.random.nextDouble() * 0.5;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.5;

                serverLevel.sendParticles(ParticleTypes.CRIT,
                        position.x, position.y, position.z,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.1
                );

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        position.x, position.y, position.z,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.05
                );
            }
        }
    }

    private void spawnDeathParticles(Vec3 position) {
        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            for (int i = 0; i < 5; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.3;
                double velocityY = this.random.nextDouble() * 0.4 + 0.1;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.3;

                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        position.x + (this.random.nextDouble() - 0.5) * 0.2,
                        position.y + (this.random.nextDouble() - 0.5) * 0.15,
                        position.z + (this.random.nextDouble() - 0.5) * 0.2,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.02
                );
            }

            for (int i = 0; i < 3; i++) {
                double velocityX = (this.random.nextDouble() - 0.5) * 0.2;
                double velocityY = this.random.nextDouble() * 0.3 + 0.05;
                double velocityZ = (this.random.nextDouble() - 0.5) * 0.2;

                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        position.x + (this.random.nextDouble() - 0.5) * 0.3,
                        position.y + (this.random.nextDouble() - 0.5) * 0.2,
                        position.z + (this.random.nextDouble() - 0.5) * 0.3,
                        1,
                        velocityX, velocityY, velocityZ,
                        0.01
                );
            }

            this.level().playSound(null, position.x, position.y, position.z,
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL,
                    0.2F, 2.0F + (this.random.nextFloat() - 0.5F) * 0.3F);
        }
    }

    @Override
    protected void onExpired() {
        spawnDeathParticles(this.position());
    }

    @Override
    public float getDamage() {
        return super.getDamage() * currentDamageMultiplier;
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
    }

    @Override
    public void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("BouncesLeft", this.bouncesLeft);
        compound.putFloat("CurrentDamageMultiplier", this.currentDamageMultiplier);
        compound.putFloat("PenetrationLeft", this.penetrationLeft);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.bouncesLeft = compound.getInt("BouncesLeft");
        this.currentDamageMultiplier = compound.getFloat("CurrentDamageMultiplier");
        this.penetrationLeft = compound.getFloat("PenetrationLeft");
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(this.bouncesLeft);
        buffer.writeFloat(this.currentDamageMultiplier);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        super.readSpawnData(buffer);
        this.bouncesLeft = buffer.readInt();
        this.currentDamageMultiplier = buffer.readFloat();
    }
}
