package top.ribs.scguns.entity.projectile;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.EventHooks;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.cache.HotBarrelCache;
import top.ribs.scguns.common.*;
import top.ribs.scguns.common.Gun.Projectile;
import top.ribs.scguns.Config;
import top.ribs.scguns.compat.SableBlockInteraction;
import top.ribs.scguns.compat.ShieldGeneratorCompat;
import top.ribs.scguns.effect.RocketExplosion;
import top.ribs.scguns.config.ProjectileAdvantageConfig;
import top.ribs.scguns.init.*;
import top.ribs.scguns.event.GunProjectileHitEvent;
import top.ribs.scguns.interfaces.IDamageable;
import top.ribs.scguns.interfaces.IExplosionDamageable;
import top.ribs.scguns.interfaces.IHeadshotBox;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedDiamondSteelGunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageBlood;
import top.ribs.scguns.network.message.S2CMessageProjectileHitBlock;
import top.ribs.scguns.network.message.S2CMessageProjectileHitEntity;
import top.ribs.scguns.network.message.S2CMessageRemoveProjectile;
import top.ribs.scguns.util.BufferUtil;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;
import top.ribs.scguns.util.ReflectionUtil;
import top.ribs.scguns.util.math.ExtendedEntityRayTraceResult;
import top.ribs.scguns.world.ProjectileExplosion;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ProjectileEntity extends Entity implements IEntityWithComplexSpawn {
    static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();
    public static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null && Config.COMMON.gameplay.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;

    private long worldDay;
    protected int shooterId;
    protected LivingEntity shooter;
    protected Gun modifiedGun;
    protected Gun.General general;
    protected Gun.Projectile projectile;
    private ItemStack weapon = ItemStack.EMPTY;
    private ItemStack item = ItemStack.EMPTY;
    protected float additionalDamage = 0.0F;
    protected float attributeAdditionalDamage = 0.0F;
    protected double attributeDamageMultiplier = 0.0;
    protected EntityDimensions entitySize;
    protected double modifiedGravity;
    protected int life;
    private int soundTime = 0;
    private float chargeProgress;
    protected float armorBypassAmount = 2.0F;
    private double damageFalloffOriginX;
    private double damageFalloffOriginY;
    private double damageFalloffOriginZ;
    @Nullable
    private static Method valkyrienSkiesClipIncludeShipsMethod;
    private static boolean valkyrienSkiesClipIncludeShipsUnavailable;
    @Nullable
    private static Class<?> sableClipContextExtensionClass;
    @Nullable
    private static Method sableSetIgnoreMainLevelMethod;
    private static boolean sableClipUnavailable;
    @Nullable
    private Level activeBlockHitLevel;
    @Nullable
    private BlockPos activeBlockHitPos;

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        this(entityType, worldIn);
        this.shooterId = shooter.getId();
        this.shooter = shooter;
        this.modifiedGun = modifiedGun;
        this.general = modifiedGun.getGeneral();
        this.projectile = Gun.getLoadedProjectile(weapon, modifiedGun);
        if (shooter instanceof ServerPlayer player) {
            this.chargeProgress = player.getPersistentData().getFloat("ChargeProgress");
        } else if (shooter instanceof Player player) {
            this.chargeProgress = ChargeHandler.getChargeProgress(player, weapon);
        } else {
            this.chargeProgress = 0f;
        }
        if (shooter instanceof Player player) {
            ChargeHandler.clearLastChargeProgress(player.getUUID());
        }
        float puncturingBypass = GunEnchantmentHelper.getPuncturingArmorBypass(weapon);
        this.setArmorBypassAmount(this.projectile.getArmorPen() + puncturingBypass);
        AttributeInstance additionalDamageAttr = shooter.getAttribute(SCAttributes.ADDITIONAL_BULLET_DAMAGE);
        this.attributeAdditionalDamage = additionalDamageAttr != null ? (float) additionalDamageAttr.getValue() : 0.0F;

        AttributeInstance damageMultAttr = shooter.getAttribute(SCAttributes.BULLET_DAMAGE_MULTIPLIER);
        this.attributeDamageMultiplier = damageMultAttr != null ? damageMultAttr.getValue() : 1.0;

        this.entitySize = EntityDimensions.scalable(this.projectile.getSize(), this.projectile.getSize());
        this.modifiedGravity = this.projectile.isGravity() ? GunModifierHelper.getModifiedProjectileGravity(weapon, -0.04) : 0.0;
        this.life = GunModifierHelper.getModifiedProjectileLife(weapon, this.projectile.getLife());
        this.worldDay = worldIn.getDayTime() / 24000L;

        /* Get speed and set motion */
        Vec3 dir = this.getDirection(shooter, weapon, item, modifiedGun);
        double speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(weapon);
        double speed = GunModifierHelper.getModifiedProjectileSpeed(weapon, this.projectile.getSpeed() * speedModifier);
        if (modifiedGun.getGeneral().getFireMode() == FireMode.PULSE) {
            float chargeSpeedMultiplier = calculateChargeSpeedMultiplier(this.chargeProgress);
            speed *= chargeSpeedMultiplier;
        }

        AttributeInstance speedAttr = shooter.getAttribute(SCAttributes.PROJECTILE_SPEED);
        speed *= speedAttr != null ? speedAttr.getValue() : 1.0;
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();

        /* Spawn the projectile half way between the previous and current position */
        double posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
        double posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
        double posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
        this.setPos(posX, posY, posZ);
        this.damageFalloffOriginX = posX;
        this.damageFalloffOriginY = posY;
        this.damageFalloffOriginZ = posZ;

        Item ammo = this.projectile.getItem();
        if (ammo != null) {
            int customModelData = -1;
            CompoundTag weaponTag = getStackTag(weapon);
            if (weaponTag != null) {
                if (weaponTag.contains("Model", Tag.TAG_COMPOUND)) {
                    ItemStack model = ItemStack.parseOptional(worldIn.registryAccess(), weaponTag.getCompound("Model"));
                    CustomModelData modelData = model.get(DataComponents.CUSTOM_MODEL_DATA);
                    if (modelData != null) {
                        customModelData = modelData.value();
                    } else {
                        CompoundTag modelTag = getStackTag(model);
                        if (modelTag != null && modelTag.contains("CustomModelData")) {
                            customModelData = modelTag.getInt("CustomModelData");
                        }
                    }
                }
            }
            ItemStack ammoStack = new ItemStack(ammo);
            if (customModelData != -1) {
                ammoStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));
            }
            this.item = ammoStack;
        }
    }
    @Nullable
    private static CompoundTag getStackTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : null;
    }

    private float calculateChargeSpeedMultiplier(float chargeProgress) {
        chargeProgress = Mth.clamp(chargeProgress, 0.0f, 1.0f);

        float minChargeSpeedMultiplier = 0.4f;
        float maxChargeSpeedMultiplier = 1.0f;

        return minChargeSpeedMultiplier + (maxChargeSpeedMultiplier - minChargeSpeedMultiplier) * chargeProgress;
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }
    public void setArmorBypassAmount(float amount) {
        this.armorBypassAmount = amount;
    }
    protected float calculateArmorBypassDamage(LivingEntity target, float damage) {
        int armorValue = target.getArmorValue();

        float baseReduction = Math.min(0.75f, armorValue * 0.004f);

        if (armorBypassAmount <= 0) {
            return damage * (1.0f - baseReduction);
        }

        float bypassPercent = armorBypassAmount / 10.0f;
        float effectiveArmor = armorValue * (1.0f - bypassPercent);
        float finalReduction = Math.min(0.75f, effectiveArmor * 0.004f);

        return damage * (1.0f - finalReduction);
    }
    public float getDamage() {
        float damage = getaFloat();
        damage = GunModifierHelper.getModifiedDamage(this.weapon, this.modifiedGun, damage);
        damage = GunEnchantmentHelper.getAcceleratorDamage(this.weapon, damage);
        damage = GunEnchantmentHelper.getHeavyShotDamage(this.weapon, damage);

        if (this.shooter instanceof Player player) {
            damage = GunEnchantmentHelper.getHotBarrelDamage(player, this.weapon, damage);
        }

        damage = GunEnchantmentHelper.getChargeDamage(this.weapon, damage, this.chargeProgress);

        if (Config.GunScalingConfig.getInstance().isScalingEnabled()) {
            double scaledDamage = Config.GunScalingConfig.getInstance().getBaseDamage() +
                    (Config.GunScalingConfig.getInstance().getDamageIncreaseRate() * this.worldDay);
            damage *= (float) Math.min(scaledDamage, Config.GunScalingConfig.getInstance().getMaxDamage());
        }

        damage *= Config.COMMON.gameplay.globalDamageMultiplier.get().floatValue();

        if (this.getPersistentData().contains("AIDamageScale")) {
            damage *= this.getPersistentData().getFloat("AIDamageScale");
        }

        return Math.max(0F, damage);
    }
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.entitySize;
    }

    private Vec3 getDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {

        float baseSpread = modifiedGun.getGeneral().getSpread();

        float gunSpread;
        if (shooter instanceof Player player) {
            gunSpread = GunModifierHelper.getModifiedSpread(player, weapon, baseSpread);
        } else {
            gunSpread = GunModifierHelper.getModifiedSpread(weapon, baseSpread);
        }

        if (modifiedGun.getGeneral().getFireMode() == FireMode.PULSE) {
            float chargeSpreadMultiplier = calculateChargeSpreadMultiplier(this.chargeProgress);
            gunSpread *= chargeSpreadMultiplier;
        }

        if (gunSpread == 0F) {
            return getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        }

        if (shooter instanceof Player player) {
            if (!modifiedGun.getGeneral().isAlwaysSpread()) {
                float spreadTrackerMultiplier = SpreadTracker.get(player).getSpread(item);
                gunSpread *= spreadTrackerMultiplier;
            }
            if (ModSyncedDataKeys.AIMING.getValue(player)) {
                gunSpread *= 0.7F;
            }
        }

        AttributeInstance spreadAttr = shooter.getAttribute(SCAttributes.SPREAD_MULTIPLIER);
        float attrMultiplier = spreadAttr != null ? (float) spreadAttr.getValue() : 1.0F;
        gunSpread *= attrMultiplier;
        float spreadRadians = gunSpread * 0.017453292F;
        float angleY = random.nextFloat() * 2 * (float)Math.PI;
        float angleX = random.nextFloat() * spreadRadians;

        Vec3 forward = getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        Vec3 right;
        if (Math.abs(forward.y) < 0.999) {
            right = new Vec3(0, 1, 0).cross(forward).normalize();
        } else {
            right = new Vec3(1, 0, 0);
        }

        Vec3 up = forward.cross(right).normalize();
        Vec3 spreadVector = forward;
        spreadVector = rotateVector(spreadVector, right, angleX);
        spreadVector = rotateVector(spreadVector, forward, angleY);

        return spreadVector.normalize();
    }

    private float calculateChargeSpreadMultiplier(float chargeProgress) {
        chargeProgress = Mth.clamp(chargeProgress, 0.0f, 1.0f);

        float minChargeSpreadMultiplier = 2.0f;
        float maxChargeSpreadMultiplier = 0.3f;

        return minChargeSpreadMultiplier - (minChargeSpreadMultiplier - maxChargeSpreadMultiplier) * (chargeProgress * chargeProgress);
    }

    private Vec3 rotateVector(Vec3 vector, Vec3 axis, float angle) {
        float sin = Mth.sin(angle);
        float cos = Mth.cos(angle);
        float dot = (float) vector.dot(axis);

        return new Vec3(
                vector.x * cos + (axis.y * vector.z - axis.z * vector.y) * sin + axis.x * dot * (1 - cos),
                vector.y * cos + (axis.z * vector.x - axis.x * vector.z) * sin + axis.y * dot * (1 - cos),
                vector.z * cos + (axis.x * vector.y - axis.y * vector.x) * sin + axis.z * dot * (1 - cos)
        );
    }
    public float getaFloat() {
        float initialDamage = (this.projectile.getDamage() + this.additionalDamage + this.attributeAdditionalDamage);
        initialDamage *= (float) this.attributeDamageMultiplier;
        if (this.projectile.isDamageReduceOverLife()) {
            float modifier = ((float) this.projectile.getLife() - (float) (this.tickCount - 1)) / (float) this.projectile.getLife();
            initialDamage *= modifier;
        }
        if (this.projectile.hasDamageFalloff()) {
            initialDamage *= this.getDamageFalloffMultiplier();
        }
        return initialDamage / this.general.getProjectileAmount();
    }

    private float getDamageFalloffMultiplier() {
        float start = this.projectile.getDamageFalloffStart();
        float end = this.projectile.getDamageFalloffEnd();
        if (end <= start) {
            return 1.0F;
        }

        double dx = this.getX() - this.damageFalloffOriginX;
        double dy = this.getY() - this.damageFalloffOriginY;
        double dz = this.getZ() - this.damageFalloffOriginZ;
        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance <= start) {
            return 1.0F;
        }

        float minMultiplier = Mth.clamp(this.projectile.getDamageFalloffMinMultiplier(), 0.0F, 1.0F);
        if (distance >= end) {
            return minMultiplier;
        }

        float progress = (distance - start) / (end - start);
        return Mth.lerp(progress, 1.0F, minMultiplier);
    }

    public void setWeapon(ItemStack weapon) {
        this.weapon = weapon.copy();
    }

    public ItemStack getWeapon() {
        return this.weapon;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setAdditionalDamage(float additionalDamage) {
        this.additionalDamage = additionalDamage;
    }

    public double getModifiedGravity() {
        return this.modifiedGravity;
    }

    public void tick() {
        super.tick();
        this.updateHeading();
        this.onProjectileTick();

        if (!this.level().isClientSide()) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            BlockHitResult fluidResult = this.level().clip(new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));

            AABB range = new AABB(startVec.x-5, startVec.y-5, startVec.z-5, startVec.x+5, startVec.y+5, startVec.z+5);
            List<Player> players = this.level().getEntitiesOfClass(Player.class, range);

            ResourceLocation flybySound = modifiedGun.getSounds().getFlybySound();

            if (!players.isEmpty() && flybySound != null && modifiedGun.getGeneral().getProjectileAmount() == 1 && this.tickCount > 3 && soundTime < this.tickCount - 3) {
                this.level().playSound(null, startVec.x,startVec.y,startVec.z, Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get(flybySound)), SoundSource.NEUTRAL, (float) 0.5F + this.level().getRandom().nextFloat() * 0.4F, 0.8F + this.level().getRandom().nextFloat() * 0.4F);
                this.soundTime = this.tickCount;
            }

            if (fluidResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = fluidResult.getBlockPos();
                BlockState blockState = this.level().getBlockState(blockPos);
                FluidState fluidState = blockState.getFluidState();

                if (fluidState.is(FluidTags.WATER)) {
                    this.onWaterImpact(fluidResult.getLocation());
                } else if (fluidState.is(FluidTags.LAVA)) {
                    this.onLavaImpact(fluidResult.getLocation());
                }
            }

            HitResult result = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);
            if (result.getType() != HitResult.Type.MISS) {
                endVec = result.getLocation();
            }

            List<EntityResult> hitEntities = null;
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COLLATERAL, this.weapon);
            if (level == 0) {
                EntityResult entityResult = this.findEntityOnPath(startVec, endVec);
                if (entityResult != null) {
                    hitEntities = Collections.singletonList(entityResult);
                }
            } else {
                hitEntities = this.findEntitiesOnPath(startVec, endVec);
            }

            if (hitEntities != null && !hitEntities.isEmpty()) {
                for (EntityResult entityResult : hitEntities) {
                    result = new ExtendedEntityRayTraceResult(entityResult);
                    if (((EntityHitResult) result).getEntity() instanceof Player player) {
                        if (this.shooter instanceof Player && !((Player) this.shooter).canHarmPlayer(player)) {
                            result = null;
                        }
                    }
                    if (result != null) {
                        this.onHit(result, startVec, endVec);
                    }
                }
            } else {
                this.onHit(result, startVec, endVec);
            }
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

    /**
     * A simple method to perform logic on each tick of the projectile. This method is appropriate
     * for spawning particles. Override {@link #tick()} to make changes to physics
     */
    protected void onProjectileTick() {
    }

    /**
     * Called when the projectile has run out of its life. In other words, the projectile managed
     * to not hit any blocks and instead aged. The grenade uses this to explode in the air.
     */
    protected void onExpired() {
    }

    @Nullable
    protected EntityResult findEntityOnPath(Vec3 startVec, Vec3 endVec) {
        Vec3 hitVec = null;
        Entity hitEntity = null;
        boolean headshot = false;
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : entities) {
            if (!entity.equals(this.shooter)) {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if (result == null)
                    continue;
                Vec3 hitPos = result.getHitPos();
                double distanceToHit = startVec.distanceTo(hitPos);
                if (distanceToHit < closestDistance) {
                    hitVec = hitPos;
                    hitEntity = entity;
                    closestDistance = distanceToHit;
                    headshot = result.isHeadshot();
                }
            }
        }
        return hitEntity != null ? new EntityResult(hitEntity, hitVec, headshot) : null;
    }


    protected List<EntityResult> findEntitiesOnPath(Vec3 startVec, Vec3 endVec) {
        List<EntityResult> hitEntities = new ArrayList<>();
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);

        for (Entity entity : entities) {

            EntityResult result = this.getHitResult(entity, startVec, endVec);
            if (result == null) {
                continue;
            }
            hitEntities.add(result);
        }
        return hitEntities;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public EntityResult getHitResult(Entity entity, Vec3 startVec, Vec3 endVec) {
        double expandHeight = entity instanceof Player && !entity.isCrouching() ? 0.0625 : 0.0;
        AABB boundingBox = entity.getBoundingBox();
        if (Config.COMMON.gameplay.improvedHitboxes.get() && entity instanceof ServerPlayer targetPlayer && this.shooter instanceof ServerPlayer shooterPlayer) {
            boundingBox = BoundingBoxManager.getBoundingBox(targetPlayer, getLatencyHistoryIndex(shooterPlayer));
        }
        boundingBox = boundingBox.expandTowards(0, expandHeight, 0);

        Vec3 hitPos = boundingBox.clip(startVec, endVec).orElse(null);
        Vec3 grownHitPos = boundingBox.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get()).clip(startVec, endVec).orElse(null);
        if (hitPos == null && grownHitPos != null) {
            HitResult raytraceresult = rayTraceBlocks(this.level(), new ClipContext(startVec, grownHitPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);
            if (raytraceresult.getType() == HitResult.Type.BLOCK) {
                return null;
            }
            hitPos = grownHitPos;
        }

        /* Check for headshot */
        boolean headshot = false;
        if (Config.COMMON.gameplay.enableHeadShots.get() && entity instanceof LivingEntity) {
            IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());
            if (headshotBox != null) {
                AABB box = headshotBox.getHeadshotBox((LivingEntity) entity);
                if (box != null) {
                    box = box.move(boundingBox.getCenter().x, boundingBox.minY, boundingBox.getCenter().z);
                    Optional<Vec3> headshotHitPos = box.clip(startVec, endVec);
                    if (!headshotHitPos.isPresent()) {
                        box = box.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get());
                        headshotHitPos = box.clip(startVec, endVec);
                    }
                    if (headshotHitPos.isPresent() && (hitPos == null || headshotHitPos.get().distanceTo(hitPos) < 0.5)) {
                        hitPos = headshotHitPos.get();
                        headshot = true;
                    }
                }
            }
        }

        if (hitPos == null) {
            return null;
        }

        return new EntityResult(entity, hitPos, headshot);
    }

    private static int getLatencyHistoryIndex(ServerPlayer player) {
        if (player.connection == null) {
            return 0;
        }
        return Mth.clamp(Mth.floor(player.connection.latency() / 50.0F + 0.5F), 0, 19);
    }

    public void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
        if (NeoForge.EVENT_BUS.post(new GunProjectileHitEvent(result, this)).isCanceled()) {
            return;
        }

        if (ShieldGeneratorCompat.isShieldHit(result)) {
            this.remove(RemovalReason.KILLED);
            return;
        }

        if (result instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getType() == HitResult.Type.MISS) {
                return;
            }

            Vec3 hitVec = result.getLocation();
            this.beginBlockHit(blockHitResult);
            BlockPos pos = this.blockHitPos(blockHitResult.getBlockPos());
            Level hitLevel = this.blockHitLevel();
            BlockState state = hitLevel.getBlockState(pos);
            Block block = state.getBlock();

            try {
                if (Config.COMMON.gameplay.griefing.enableGlassBreaking.get() && state.is(ModTags.Blocks.FRAGILE)) {
                    float destroySpeed = state.getDestroySpeed(hitLevel, pos);
                    if (destroySpeed >= 0) {
                        float chance = Config.COMMON.gameplay.griefing.fragileBaseBreakChance.get().floatValue() / (destroySpeed + 1);
                        if (this.random.nextFloat() < chance) {
                            hitLevel.destroyBlock(pos, Config.COMMON.gameplay.griefing.fragileBlockDrops.get());
                        }
                    }
                }

                if (!state.canBeReplaced()) {
                    this.remove(RemovalReason.KILLED);
                }

                if (block instanceof IDamageable) {
                    ((IDamageable) block).onBlockDamaged(hitLevel, state, pos, this, this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
                }

                this.onHitBlock(state, pos, blockHitResult.getDirection(), hitVec.x, hitVec.y, hitVec.z);

                if (block instanceof TargetBlock targetBlock) {
                    int power = ReflectionUtil.updateTargetBlock(targetBlock, hitLevel, state, blockHitResult, this);
                    if (this.shooter instanceof ServerPlayer serverPlayer) {
                        serverPlayer.awardStat(Stats.TARGET_HIT);
                        CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, this, blockHitResult.getLocation(), power);
                    }
                }

                if (block instanceof BellBlock bell) {
                    bell.attemptToRing(hitLevel, pos, blockHitResult.getDirection());
                }
            } finally {
                this.endBlockHit();
            }

            return;
        }

        if (result instanceof ExtendedEntityRayTraceResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity.getId() == this.shooterId) {
                return;
            }

            if (this.shooter instanceof Player player) {
                if (entity.hasIndirectPassenger(player)) {
                    return;
                }
                HotBarrelCache.getHotBarrelLevel(player, this.weapon);
                boolean shouldFire = GunEnchantmentHelper.shouldSetOnFire(player, this.weapon);

                if (shouldFire) {
                    entity.igniteForSeconds(5);
                }
            } else {
                if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL, this.weapon) > 0) {
                    entity.igniteForSeconds(5);
                }
            }

            this.onHitEntity(entity, result.getLocation(), startVec, endVec, entityHitResult.isHeadshot());

            int collateralLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COLLATERAL, weapon);
            ResourceLocation advantage = this.getProjectile().getAdvantage();

            if (!(entity.getType().is(ModTags.Entities.GHOST) &&
                    advantage.equals(ModTags.Entities.UNDEAD.location())) ||
                    collateralLevel == 0) {
                this.remove(RemovalReason.KILLED);
            }

            entity.invulnerableTime = 0;
        }
    }

    protected void onLavaImpact(Vec3 impactPos) {
        if (!this.level().isClientSide()) {
            boolean enableParticles = isLavaImpactParticlesEnabled();

            if (enableParticles) {
                ServerLevel serverLevel = (ServerLevel) this.level();
                for (int i = 0; i < 5; i++) {
                    double ySpeed = 0.2 + (random.nextDouble() * 0.3);
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            impactPos.x, impactPos.y, impactPos.z,
                            1,
                            0.02, 0, 0.02,
                            ySpeed
                    );
                }

                for (int i = 0; i < 3; i++) {
                    double xSpeed = (random.nextDouble() - 0.5) * 0.1;
                    double ySpeed = 0.2 + (random.nextDouble() * 0.2);
                    double zSpeed = (random.nextDouble() - 0.5) * 0.1;
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            impactPos.x, impactPos.y, impactPos.z,
                            1,
                            xSpeed, ySpeed, zSpeed,
                            0.05
                    );
                }

                serverLevel.sendParticles(ParticleTypes.LAVA,
                        impactPos.x, impactPos.y + 0.05, impactPos.z,
                        3,
                        0.1, 0.1, 0.1,
                        0.2
                );
            }
        }
        this.level().playSound(null, impactPos.x, impactPos.y, impactPos.z,
                SoundEvents.LAVA_POP, SoundSource.NEUTRAL,
                0.6F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
    }

    protected void onWaterImpact(Vec3 impactPos) {
        if (!this.level().isClientSide()) {
            boolean enableParticles = isWaterImpactParticlesEnabled();

            if (enableParticles) {
                ServerLevel serverLevel = (ServerLevel) this.level();
                boolean isSubmerged = this.isInWater();
                int splashParticles = isSubmerged ? 10 : 40;
                int bubbleParticles = isSubmerged ? 10 : 30;
                int snowflakeParticles = isSubmerged ? 5 : 15;
                int fallingWaterParticles = isSubmerged ? 5 : 20;

                for (int i = 0; i < fallingWaterParticles; i++) {
                    double ySpeed = 0.5 + (random.nextDouble() * 0.5);
                    serverLevel.sendParticles(ParticleTypes.FALLING_WATER,
                            impactPos.x, impactPos.y, impactPos.z,
                            1, 0.05, 0, 0.05, ySpeed);
                }

                for (int i = 0; i < snowflakeParticles; i++) {
                    double xSpeed = (random.nextDouble() - 0.5) * 0.2;
                    double ySpeed = 0.3 + (random.nextDouble() * 0.3);
                    double zSpeed = (random.nextDouble() - 0.5) * 0.2;
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            impactPos.x, impactPos.y, impactPos.z,
                            1, xSpeed, ySpeed, zSpeed, 0.1);
                }

                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        impactPos.x, impactPos.y + 0.1, impactPos.z,
                        splashParticles, 0.2, 0.2, 0.2, 0.4);

                serverLevel.sendParticles(ParticleTypes.BUBBLE_POP,
                        impactPos.x, impactPos.y, impactPos.z,
                        bubbleParticles, 0.5, 0.3, 0.5, 0.2);
            }
        }

        this.level().playSound(null, impactPos.x, impactPos.y, impactPos.z,
                SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL,
                1.2F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
    }

    protected static boolean isWaterImpactParticlesEnabled() {
        try {
            return Config.CLIENT.particle.enableWaterImpactParticles.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    protected static boolean isLavaImpactParticlesEnabled() {
        try {
            return Config.CLIENT.particle.enableLavaImpactParticles.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public float advantageMultiplier(Entity entity) {
        ResourceLocation advantage = this.getProjectile().getAdvantage();
        if (advantage.equals(ModTags.Entities.NONE.location())) {
            return 1.0f;
        }

        ProjectileAdvantageConfig.AdvantageData advantageData =
                ProjectileAdvantageConfig.getAdvantageData(advantage.toString());

        if (advantageData == null) {
            return 1.0f;
        }
        boolean hasMatchingTag = false;
        for (String targetTag : advantageData.targetTags()) {
            ResourceLocation tagLocation = ResourceLocation.parse(targetTag);
            TagKey<EntityType<?>> entityTag = TagKey.create(Registries.ENTITY_TYPE, tagLocation);
            if (entity.getType().is(entityTag)) {
                hasMatchingTag = true;
                break;
            }
        }

        if (!hasMatchingTag && advantage.equals(ModTags.Entities.UNDEAD.location()) && entity instanceof LivingEntity livingEntity) {
            hasMatchingTag = livingEntity.isInvertedHealAndHarm();
        }

        if (hasMatchingTag) {
            if (advantageData.causesFire() && advantageData.fireDuration() > 0) {
                entity.igniteForSeconds(advantageData.fireDuration());
            }

            return advantageData.multiplier();
        }

        return 1.0f;
    }

    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.weapon, this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        damage *= advantageMultiplier(entity);
        boolean wasAlive = entity instanceof LivingEntity && entity.isAlive();
        if (this.shooter instanceof Player player) {
            damage = GunEnchantmentHelper.getWaterProofDamage(this.weapon, player, damage);
        }
        if (headshot) {
            damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
        }
        if (entity instanceof LivingEntity livingTarget) {
            damage = GunEnchantmentHelper.getPuncturingDamageReduction(this.weapon, livingTarget, damage);
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
                            Optional<Holder.Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.getHolder(effectLocation);
                            if (effect.isPresent()) {
                                livingEntity.addEffect(new MobEffectInstance(
                                        effect.get(),
                                        this.projectile.getImpactEffectDuration(),
                                        this.projectile.getImpactEffectAmplifier()
                                ));
                            }
                        }
                    }
                }
            }
        }

        if (entity instanceof LivingEntity) {
            GunEnchantmentHelper.applyElementalPopEffect(this.weapon, (LivingEntity) entity);
        }

        if (this.shooter instanceof Player) {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }
        if (wasAlive && entity instanceof LivingEntity livingEntity && !livingEntity.isAlive()) {
            checkForDiamondSteelBonus(livingEntity, hitVec);
        }
        PacketHandler.getPlayChannel().sendToTrackingEntity(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity.getType()));

    }
    void checkForDiamondSteelBonus(LivingEntity killedEntity, Vec3 position) {
        if (!this.level().isClientSide && this.getShooter() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();

            if (weapon.getItem() instanceof AnimatedDiamondSteelGunItem) {
                int baseXP = killedEntity.getExperienceReward((ServerLevel) this.level(), player);
                int bonusXP = Math.round(baseXP * 0.2f);

                if (bonusXP > 0) {
                    ExperienceOrb xpOrb = new ExperienceOrb(this.level(), position.x, position.y, position.z, bonusXP);
                    this.level().addFreshEntity(xpOrb);
                }
            }
        }
    }
    public float applyProjectileProtection(LivingEntity target, float damage) {
        int protectionLevel = getEntityEnchantmentLevel(target, Enchantments.PROJECTILE_PROTECTION);

        if (protectionLevel > 0) {
            float reduction = protectionLevel * 0.10f;
            reduction = Math.min(reduction, 0.8f);
            damage *= (1.0f - reduction);
        }

        return damage;
    }

    private static int getEntityEnchantmentLevel(LivingEntity target, ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        return target.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(enchantment)
                .map(holder -> EnchantmentHelper.getEnchantmentLevel(holder, target))
                .orElse(0);
    }

    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        Level hitLevel = this.blockHitLevel();
        BlockPos hitPos = this.blockHitPos(pos);
        PacketHandler.getPlayChannel().sendToTrackingChunk(() -> hitLevel.getChunkAt(hitPos), new S2CMessageProjectileHitBlock(x, y, z, hitPos, face));
        Block block = state.getBlock();

        if (primeTNT(hitLevel, state, hitPos)) {
            return;
        }
        if (block instanceof DoorBlock) {
            boolean isOpen = state.getValue(DoorBlock.OPEN);
            if (!isOpen) {
                hitLevel.setBlock(hitPos, state.setValue(DoorBlock.OPEN, true), 10);
                hitLevel.playSound(null, hitPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        if (!state.canBeReplaced()) {
            this.remove(RemovalReason.KILLED);
        }
        if (block instanceof IDamageable) {
            ((IDamageable) block).onBlockDamaged(hitLevel, state, hitPos, this, this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
        }
    }

    boolean primeTNT(BlockState state, BlockPos pos) {
        return primeTNT(this.level(), state, pos);
    }

    boolean primeTNT(Level level, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block == Blocks.TNT) {
            if (!level.isClientSide()) {
                TntBlock.explode(level, pos);
                level.removeBlock(pos, false);
            }
            return true;
        }
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.projectile = new Gun.Projectile();
        this.projectile.deserializeNBT(compound.getCompound("Projectile"));
        this.general = new Gun.General();
        this.general.deserializeNBT(compound.getCompound("General"));
        this.modifiedGravity = compound.getDouble("ModifiedGravity");
        this.life = compound.getInt("MaxLife");
        this.damageFalloffOriginX = compound.contains("DamageFalloffOriginX", Tag.TAG_ANY_NUMERIC) ? compound.getDouble("DamageFalloffOriginX") : this.getX();
        this.damageFalloffOriginY = compound.contains("DamageFalloffOriginY", Tag.TAG_ANY_NUMERIC) ? compound.getDouble("DamageFalloffOriginY") : this.getY();
        this.damageFalloffOriginZ = compound.contains("DamageFalloffOriginZ", Tag.TAG_ANY_NUMERIC) ? compound.getDouble("DamageFalloffOriginZ") : this.getZ();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Projectile", this.projectile.serializeNBT());
        compound.put("General", this.general.serializeNBT());
        compound.putDouble("ModifiedGravity", this.modifiedGravity);
        compound.putInt("MaxLife", this.life);
        compound.putDouble("DamageFalloffOriginX", this.damageFalloffOriginX);
        compound.putDouble("DamageFalloffOriginY", this.damageFalloffOriginY);
        compound.putDouble("DamageFalloffOriginZ", this.damageFalloffOriginZ);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeNbt(this.projectile.serializeNBT());
        buffer.writeNbt(this.general.serializeNBT());
        buffer.writeInt(this.shooterId);
        BufferUtil.writeItemStackToBufIgnoreTag(buffer, this.item);
        buffer.writeDouble(this.modifiedGravity);
        buffer.writeVarInt(this.life);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        this.projectile = new Gun.Projectile();
        this.projectile.deserializeNBT(buffer.readNbt());
        this.general = new Gun.General();
        this.general.deserializeNBT(buffer.readNbt());
        this.shooterId = buffer.readInt();
        this.item = BufferUtil.readItemStackFromBufIgnoreTag(buffer);
        this.modifiedGravity = buffer.readDouble();
        this.life = buffer.readVarInt();
        this.entitySize = EntityDimensions.scalable(this.projectile.getSize(), this.projectile.getSize());
    }

    public void updateHeading() {
        double horizontalDistance = this.getDeltaMovement().horizontalDistance();
        this.setYRot((float) (Mth.atan2(this.getDeltaMovement().x(), this.getDeltaMovement().z()) * (180D / Math.PI)));
        this.setXRot((float) (Mth.atan2(this.getDeltaMovement().y(), horizontalDistance) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public Projectile getProjectile() {
        return this.projectile;
    }

    static Vec3 getVectorFromRotation(float pitch, float yaw) {
        float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    /**
     * Gets the entity who spawned the projectile
     */
    public LivingEntity getShooter() {
        return this.shooter;
    }

    /**
     * Gets the id of the entity who spawned the projectile
     */
    public int getShooterId() {
        return this.shooterId;
    }




    float getCriticalDamage(ItemStack weapon, RandomSource rand, float damage) {
        float chance = GunModifierHelper.getCriticalChance(weapon);
        if (rand.nextFloat() < chance) {
            float multiplier = this.projectile != null ? this.projectile.getCritDamageMultiplier() : Config.COMMON.gameplay.criticalDamageMultiplier.get().floatValue();
            return damage * multiplier;
        }
        return damage;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    public void onRemovedFromWorld() {
        if (!this.level().isClientSide) {
            PacketHandler.getPlayChannel().sendToNearbyPlayers(this::getDeathTargetPoint, new S2CMessageRemoveProjectile(this.getId()));
        }
    }

    LevelLocation getDeathTargetPoint() {
        return LevelLocation.create((ServerLevel) this.level(), this.getX(), this.getY(), this.getZ(), 256);
    }

    /**
     * A custom implementation of ray tracing that allows you to pass a predicate to ignore certain
     * blocks when checking for collisions.
     *
     * @param world           the world to perform the ray trace
     * @param context         the ray trace context
     * @param ignorePredicate the block state predicate
     * @return a result of the raytrace
     */
    static BlockHitResult rayTraceBlocks(Level world, ClipContext context, Predicate<BlockState> ignorePredicate) {
        BlockHitResult normalResult = performRayTrace(context, (rayTraceContext, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if (ignorePredicate.test(blockState)) return null;
            FluidState fluidState = world.getFluidState(blockPos);
            Vec3 startVec = rayTraceContext.getFrom();
            Vec3 endVec = rayTraceContext.getTo();
            VoxelShape blockShape = rayTraceContext.getBlockShape(blockState, world, blockPos);
            BlockHitResult blockResult = world.clipWithInteractionOverride(startVec, endVec, blockPos, blockShape, blockState);
            VoxelShape fluidShape = rayTraceContext.getFluidShape(fluidState, world, blockPos);
            BlockHitResult fluidResult = fluidShape.clip(startVec, endVec, blockPos);
            double blockDistance = blockResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(blockResult.getLocation());
            double fluidDistance = fluidResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(fluidResult.getLocation());
            return blockDistance <= fluidDistance ? blockResult : fluidResult;
        }, (rayTraceContext) -> {
            Vec3 Vector3d = rayTraceContext.getFrom().subtract(rayTraceContext.getTo());
            return BlockHitResult.miss(rayTraceContext.getTo(), Direction.getNearest(Vector3d.x, Vector3d.y, Vector3d.z), BlockPos.containing(rayTraceContext.getTo()));
        });
        return nearestBlockHit(context.getFrom(), normalResult, clipIncludeShipsIfAvailable(world, context), clipSableSubLevelsIfAvailable(world, context, ignorePredicate), ShieldGeneratorCompat.clip(world, context));
    }

    @Nullable
    private static BlockHitResult clipIncludeShipsIfAvailable(Level world, ClipContext context) {
        if (!ScorchedGuns.valkyrienSkiesLoaded || valkyrienSkiesClipIncludeShipsUnavailable) {
            return null;
        }
        try {
            Method method = valkyrienSkiesClipIncludeShipsMethod;
            if (method == null) {
                Class<?> raycastUtils = Class.forName("org.valkyrienskies.mod.common.world.RaycastUtilsKt");
                method = raycastUtils.getMethod("clipIncludeShips", Level.class, ClipContext.class);
                valkyrienSkiesClipIncludeShipsMethod = method;
            }
            Object result = method.invoke(null, world, context);
            return result instanceof BlockHitResult blockHitResult ? blockHitResult : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            valkyrienSkiesClipIncludeShipsUnavailable = true;
            return null;
        }
    }

    @Nullable
    private static BlockHitResult clipSableSubLevelsIfAvailable(Level world, ClipContext context, Predicate<BlockState> ignorePredicate) {
        return SableBlockInteraction.clipSubLevels(world, context, ignorePredicate);
    }

    private static BlockHitResult nearestBlockHit(Vec3 start, BlockHitResult fallback, @Nullable BlockHitResult... candidates) {
        BlockHitResult nearest = fallback;
        double nearestDistance = fallback == null || fallback.getType() == HitResult.Type.MISS ? Double.MAX_VALUE : start.distanceToSqr(fallback.getLocation());
        for (BlockHitResult candidate : candidates) {
            if (candidate == null || candidate.getType() == HitResult.Type.MISS) {
                continue;
            }
            double distance = start.distanceToSqr(candidate.getLocation());
            if (nearest == null || nearest.getType() == HitResult.Type.MISS || distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    protected void beginBlockHit(BlockHitResult hitResult) {
        this.activeBlockHitLevel = SableBlockInteraction.levelFor(this.level(), hitResult);
        this.activeBlockHitPos = SableBlockInteraction.blockPosFor(hitResult);
    }

    protected void endBlockHit() {
        this.activeBlockHitLevel = null;
        this.activeBlockHitPos = null;
    }

    protected Level blockHitLevel() {
        return this.activeBlockHitLevel != null ? this.activeBlockHitLevel : this.level();
    }

    protected BlockPos blockHitPos(BlockPos fallback) {
        return this.activeBlockHitPos != null ? this.activeBlockHitPos : fallback;
    }

    private static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> p_217300_2_) {
        Vec3 startVec = context.getFrom();
        Vec3 endVec = context.getTo();
        if (startVec.equals(endVec)) {
            return p_217300_2_.apply(context);
        } else {
            double startX = Mth.lerp(-0.0000001, endVec.x, startVec.x);
            double startY = Mth.lerp(-0.0000001, endVec.y, startVec.y);
            double startZ = Mth.lerp(-0.0000001, endVec.z, startVec.z);
            double endX = Mth.lerp(-0.0000001, startVec.x, endVec.x);
            double endY = Mth.lerp(-0.0000001, startVec.y, endVec.y);
            double endZ = Mth.lerp(-0.0000001, startVec.z, endVec.z);
            int blockX = Mth.floor(endX);
            int blockY = Mth.floor(endY);
            int blockZ = Mth.floor(endZ);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
            T t = hitFunction.apply(context, mutablePos);
            if (t != null) {
                return t;
            }

            double deltaX = startX - endX;
            double deltaY = startY - endY;
            double deltaZ = startZ - endZ;
            int signX = Mth.sign(deltaX);
            int signY = Mth.sign(deltaY);
            int signZ = Mth.sign(deltaZ);
            double d9 = signX == 0 ? Double.MAX_VALUE : (double) signX / deltaX;
            double d10 = signY == 0 ? Double.MAX_VALUE : (double) signY / deltaY;
            double d11 = signZ == 0 ? Double.MAX_VALUE : (double) signZ / deltaZ;
            double d12 = d9 * (signX > 0 ? 1.0D - Mth.frac(endX) : Mth.frac(endX));
            double d13 = d10 * (signY > 0 ? 1.0D - Mth.frac(endY) : Mth.frac(endY));
            double d14 = d11 * (signZ > 0 ? 1.0D - Mth.frac(endZ) : Mth.frac(endZ));

            while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
                if (d12 < d13) {
                    if (d12 < d14) {
                        blockX += signX;
                        d12 += d9;
                    } else {
                        blockZ += signZ;
                        d14 += d11;
                    }
                } else if (d13 < d14) {
                    blockY += signY;
                    d13 += d10;
                } else {
                    blockZ += signZ;
                    d14 += d11;
                }

                T t1 = hitFunction.apply(context, mutablePos.set(blockX, blockY, blockZ));
                if (t1 != null) {
                    return t1;
                }
            }

            return p_217300_2_.apply(context);
        }
    }

    /**
     * Creates a projectile explosion for the specified entity.
     *
     * @param entity    The entity to explode
     * @param radius    The amount of radius the entity should deal
     * @param forceNone If true, forces the explosion mode to be NONE instead of config value
     */
    public static void createExplosion(Entity entity, float radius, boolean forceNone) {
        Level world = entity.level();
        if (world.isClientSide())
            return;

        DamageSource source = entity instanceof ProjectileEntity projectile ? entity.damageSources().explosion(entity, projectile.getShooter()) : null;
        Explosion.BlockInteraction mode = Config.COMMON.gameplay.griefing.enableBlockRemovalOnExplosions.get() && !forceNone ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius, false, mode) {
            @Override
            protected float getEntityDamageAmount(Entity entity, double distance) {
                return 0;
            }
        };

        if (EventHooks.onExplosionStart(world, explosion))
            return;

        explosion.explode();
        explosion.finalizeExplosion(true);

        explosion.getToBlow().forEach(pos ->
        {
            if (world.getBlockState(pos).getBlock() instanceof IExplosionDamageable) {
                ((IExplosionDamageable) world.getBlockState(pos).getBlock()).onProjectileExploded(world, world.getBlockState(pos), pos, entity);
            }
        });

        // Clears the affected blocks if mode is none
        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for (ServerPlayer player : ((ServerLevel) world).players()) {
            if (player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < 4096) {
                player.connection.send(new ClientboundExplodePacket(
                        entity.getX(), entity.getY(), entity.getZ(), radius,
                        explosion.getToBlow(), explosion.getHitPlayers().get(player),
                        explosion.getBlockInteraction(),
                        explosion.getSmallExplosionParticles(),
                        explosion.getLargeExplosionParticles(),
                        explosion.getExplosionSound()
                ));
            }
        }
    }

    public static void createRocketExplosion(Entity entity, float radius, float damage, boolean forceNone) {
        Level world = entity.level();
        if (world.isClientSide())
            return;

        DamageSource source = entity instanceof ProjectileEntity projectile ?
                entity.damageSources().explosion(entity, projectile.getShooter()) : null;
        Explosion.BlockInteraction mode = Config.COMMON.gameplay.griefing.enableBlockRemovalOnExplosions.get() && !forceNone ?
                Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;

        Explosion explosion = new RocketExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius, damage, false, mode);

        if (EventHooks.onExplosionStart(world, explosion))
            return;

        explosion.explode();
        explosion.finalizeExplosion(true);

        explosion.getToBlow().forEach(pos -> {
            if (world.getBlockState(pos).getBlock() instanceof IExplosionDamageable) {
                ((IExplosionDamageable) world.getBlockState(pos).getBlock()).onProjectileExploded(world, world.getBlockState(pos), pos, entity);
            }
        });

        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

    }


    public static void createFireExplosion(Entity entity, float radius, boolean forceNone) {
        Level world = entity.level();
        if (world.isClientSide())
            return;

        DamageSource source = entity instanceof ProjectileEntity projectile ? entity.damageSources().explosion(entity, projectile.getShooter()) : null;
        Explosion.BlockInteraction mode = Explosion.BlockInteraction.KEEP;
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius, true, mode) {
            @Override
            protected float getEntityDamageAmount(Entity entity, double distance) {
                return 0;
            }
        };

        if (EventHooks.onExplosionStart(world, explosion))
            return;
        explosion.explode();
        explosion.finalizeExplosion(true);

    }

    public static void createChokeExplosion(Entity entity, float radius) {
        Level world = entity.level();
        if (world.isClientSide()) {
            return;
        }

        BlockPos centerPos = entity.blockPosition();
        int radiusInt = (int) Math.ceil(radius);
        int radiusSquared = radiusInt * radiusInt;
        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    BlockPos pos = centerPos.offset(x, y, z);
                    if (centerPos.distSqr(pos) <= radiusSquared) {
                        if (world.getBlockState(pos).getBlock() == Blocks.FIRE) {
                            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            world.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                        }
                    }
                }
            }
        }
        AABB effectArea = new AABB(centerPos).inflate(radius);
        List<LivingEntity> affectedEntities = world.getEntitiesOfClass(LivingEntity.class, effectArea);
        for (LivingEntity affectedEntity : affectedEntities) {
            if (affectedEntity.isOnFire()) {
                affectedEntity.clearFire();
            }
        }
        if (!world.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) world;
            for (int i = 0; i < 200; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 2.0 * radius;
                double offsetY = (serverLevel.random.nextDouble() - 0.5) * 0.2;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 2.0 * radius;
                double posX = centerPos.getX() + offsetX;
                double posY = centerPos.getY() + offsetY;
                double posZ = centerPos.getZ() + offsetZ;
                double speedX = (serverLevel.random.nextDouble() - 0.5) * 0.1;
                double speedY = (serverLevel.random.nextDouble() - 0.5) * 0.1;
                double speedZ = (serverLevel.random.nextDouble() - 0.5) * 0.1;
                serverLevel.sendParticles(ParticleTypes.WHITE_ASH, posX, posY, posZ, 1, speedX, speedY, speedZ, 0.1);
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, posX, posY, posZ, 1, speedX, speedY, speedZ, 0.1);
            }
        }
    }


    public Entity getOwner() {
        return this.shooter;
    }

    /**
     * Author: MrCrayfish
     */
    public static class EntityResult {
        private final Entity entity;
        private final Vec3 hitVec;
        private final boolean headshot;

        public EntityResult(Entity entity, Vec3 hitVec, boolean headshot) {
            this.entity = entity;
            this.hitVec = hitVec;
            this.headshot = headshot;
        }

        /**
         * Gets the entity that was hit by the projectile
         */
        public Entity getEntity() {
            return this.entity;
        }

        /**
         * Gets the position the projectile hit
         */
        public Vec3 getHitPos() {
            return this.hitVec;
        }

        /**
         * Gets if this was a headshot
         */
        public boolean isHeadshot() {
            return this.headshot;
        }
    }

    public static class ProjectileHelper {
        public static final float DEFAULT_SHIELD_DISABLE_CHANCE = 0.30f;

        public static boolean handleShieldHit(Entity target, Entity projectile, float damage, float shieldDisableChance) {
            if (!(target instanceof Player player)) {
                return false;
            }

            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            boolean isBlockingMainHand = player.isBlocking() && mainHandItem.getItem() instanceof ShieldItem;
            boolean isBlockingOffHand = player.isBlocking() && offHandItem.getItem() instanceof ShieldItem;

            if (!isBlockingMainHand && !isBlockingOffHand) {
                return false;
            }

            ItemStack shield = isBlockingMainHand ? mainHandItem : offHandItem;
            InteractionHand hand = isBlockingMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

            if (projectile.level().getRandom().nextFloat() < shieldDisableChance) {
                player.getCooldowns().addCooldown(shield.getItem(), 100);
                player.stopUsingItem();
                player.level().broadcastEntityEvent(player, (byte) 30);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                return false;
            }

            player.hurt(player.damageSources().generic(), 0.5f);
            shield.hurtAndBreak(12, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);

            return true;
        }

        public static boolean handleShieldHit(Entity target, Entity projectile, float damage) {
            return handleShieldHit(target, projectile, damage, DEFAULT_SHIELD_DISABLE_CHANCE);
        }
    }
}
