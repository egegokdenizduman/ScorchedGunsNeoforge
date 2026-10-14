package top.ribs.scguns.common.network;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import top.ribs.scguns.Config;
import top.ribs.scguns.client.handler.BeamHandler;
import top.ribs.scguns.common.*;
import top.ribs.scguns.common.container.AttachmentContainer;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.init.*;
import top.ribs.scguns.interfaces.IProjectileFactory;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.*;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;
import top.ribs.scguns.util.math.ExtendedEntityRayTraceResult;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: MrCrayfish
 */
public class ServerPlayHandler {

    private static final Map<UUID, BeamHandler.BeamInfo> activeBeams = new HashMap<>();
    private static final Map<Integer, Integer> serverEntityShotCount = new HashMap<>();
    /**
     * Fires the weapon the player is currently holding.
     * This is only intended for use on the logical server.
     *
     * @param player the player for who's weapon to fire
     */

    public static void handleShoot(C2SMessageShoot message, ServerPlayer player) {
        if (player.isSpectator() || player.getUseItem().getItem() == Items.SHIELD)
            return;

        Level world = player.level();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (!(heldItem.getItem() instanceof GunItem item)) {
            return;
        }

        if (!Gun.hasAmmo(heldItem) && !player.isCreative()) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.8F);
            return;
        }

        Gun modifiedGun = item.getModifiedGun(heldItem);
        if (modifiedGun == null) return;
        Gun.Projectile activeProjectile = Gun.getLoadedProjectile(heldItem, modifiedGun);
        if (NeoForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)).isCanceled())
            return;
        player.setYRot(Mth.wrapDegrees(message.getRotationYaw()));
        player.setXRot(Mth.clamp(message.getRotationPitch(), -90F, 90F));
        ShootTracker tracker = ShootTracker.getShootTracker(player);
        if (tracker.hasCooldown(item) && tracker.getRemaining(item) > Config.SERVER.cooldownThreshold.get()) {
            return;
        }
        tracker.putCooldown(player, heldItem, item, modifiedGun);
        if(ModSyncedDataKeys.RELOADING.getValue(player)) {
            ModSyncedDataKeys.RELOADING.setValue(player, false);
        }
        if (!modifiedGun.getGeneral().isAlwaysSpread() && modifiedGun.getGeneral().getSpread() > 0.0F) {
            SpreadTracker.get(player).update(player, item);
        }
        if (FireMode.BEAM.equals(modifiedGun.getGeneral().getFireMode()) ||
                FireMode.SEMI_BEAM.equals(modifiedGun.getGeneral().getFireMode())) {
            handleBeamWeapon(player, heldItem, modifiedGun);
        }
        else if (activeProjectile.firesArrows()) {
            int count = modifiedGun.getGeneral().getProjectileAmount();
            ItemStack arrowStack = Gun.getLoadedAmmoStack(heldItem, modifiedGun, 1, world.registryAccess());
            Gun.setLastFiredAmmoStack(heldItem, arrowStack, world.registryAccess());
            // Multishot works like on a crossbow: extra arrows fan out and can't be picked up
            int multishot = EnchantmentHelper.processProjectileCount(player.serverLevel(), heldItem, player, 1);
            float fan = EnchantmentHelper.processProjectileSpread(player.serverLevel(), heldItem, player, 0.0F);
            float step = multishot == 1 ? 0.0F : 2.0F * fan / (multishot - 1);
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < multishot; j++) {
                    float angle = (j + 1) / 2 * step * (j % 2 == 1 ? 1.0F : -1.0F);
                    AbstractArrow arrow = getArrow(player, world, activeProjectile, arrowStack, heldItem, angle);
                    arrow.pickup = player.isCreative() || j > 0 ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;

                    world.addFreshEntity(arrow);
                }
            }
        }else {
            fireProjectiles(world, player, heldItem, item, modifiedGun, activeProjectile);
        }

        if (!player.isCreative()) {
            CompoundTag tag = getOrCreateCustomData(heldItem);
            if (!tag.getBoolean("IgnoreAmmo")) {
                int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RECLAIMED, heldItem);
                if (level == 0 || player.level().random.nextInt(4 - Mth.clamp(level, 1, 2)) != 0) {
                    int currentAmmo = tag.getInt("AmmoCount");
                    int newAmmo = Math.max(0, currentAmmo - 1);
                    tag.putInt("AmmoCount", newAmmo);
                    setCustomData(heldItem, tag);
                    if (newAmmo == 0) {
                        Gun.clearLoadedProjectileItem(heldItem);
                    }
                }
            }
        }
        if (Config.COMMON.gameplay.spawnCasings.get()) {
            if (activeProjectile.casingType != null && !player.getAbilities().instabuild && !activeProjectile.ejectDuringReload()) {
                ItemStack casingStack = new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.getOptional(activeProjectile.casingType).orElse(null)));
                GunEventBus.dropCasing(player, heldItem, casingStack);
            }
        }
        if (!player.isCreative()) {
            if (Config.COMMON.gameplay.enableGunDamage.get()) {
                GunEventBus.damageGun(heldItem, world, player);
            }
            if (Config.COMMON.gameplay.enableAttachmentDamage.get()) {
                GunEventBus.damageAttachments(heldItem, world, player);
            }

        }
        ResourceLocation fireSound = getFireSound(heldItem, modifiedGun);
        if (fireSound != null) {
            playFireSound(player, world, heldItem, modifiedGun, fireSound);
        }
        GunEffectsHandler.handleGunEffects(player, heldItem, modifiedGun);
        NeoForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
        player.awardStat(Stats.ITEM_USED.get(item));
    }
    public static class RatKingAndQueenModel {
        public static class GunFireEventRatHandler {
            private static int shotCount = 0;

            public static int getShotCount() {
                return shotCount;
            }

            public static void incrementShotCount() {
                shotCount++;
            }

            public static boolean shouldUseAlternateAnimation() {
                return shotCount % 2 == 1;
            }
        }
    }
    @NotNull
    private static AbstractArrow getArrow(ServerPlayer player, Level world, Gun.Projectile projectile, ItemStack ammo, ItemStack weapon, float angle) {
        // Let the arrow item build its own entity: tipped arrows keep their potion, spectral arrows glow, etc.
        // Passing the gun as the weapon lets vanilla apply its bow/crossbow enchantments (Power, Punch, Flame, Piercing).
        ItemStack arrowStack = ammo.getItem() instanceof ArrowItem ? ammo.copyWithCount(1) : Items.ARROW.getDefaultInstance();
        AbstractArrow arrow = ((ArrowItem) arrowStack.getItem()).createArrow(world, arrowStack, player, weapon);

        float speed = (float) projectile.getSpeed() * 0.35f;
        float pitch = player.getXRot();
        float yaw = player.getYRot();
        float f = -Mth.sin(yaw * ((float)Math.PI / 180F)) * Mth.cos(pitch * ((float)Math.PI / 180F));
        float f1 = -Mth.sin(pitch * ((float)Math.PI / 180F));
        float f2 = Mth.cos(yaw * ((float)Math.PI / 180F)) * Mth.cos(pitch * ((float)Math.PI / 180F));
        Vec3 motion = new Vec3(f, f1, f2);
        if (angle != 0.0F) {
            Vec3 up = player.getUpVector(1.0F);
            Vector3f rotated = motion.toVector3f().rotate(new Quaternionf().setAngleAxis(angle * ((float)Math.PI / 180F), up.x, up.y, up.z));
            motion = new Vec3(rotated);
        }
        Vec3 spawnPos = player.getEyePosition().add(
                motion.x * 0.5,
                -0.1,
                motion.z * 0.5
        );
        arrow.setPos(spawnPos);
        arrow.setDeltaMovement(motion.x * speed, motion.y * speed, motion.z * speed);

        float horizontalDistance = Mth.sqrt((float) (motion.x * motion.x + motion.z * motion.z));
        arrow.setYRot((float) (Mth.atan2(motion.x, motion.z) * (180F / Math.PI)));
        arrow.setXRot((float) (Mth.atan2(motion.y, horizontalDistance) * (180F / Math.PI)));
        arrow.yRotO = arrow.getYRot();
        arrow.xRotO = arrow.getXRot();

        arrow.setBaseDamage(projectile.getDamage() * 0.15);
        return arrow;
    }

    private static void fireProjectiles(Level world, ServerPlayer player, ItemStack heldItem, GunItem item, Gun modifiedGun, Gun.Projectile projectileProps) {
        int count = modifiedGun.getGeneral().getProjectileAmount();
        ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];

        for (int i = 0; i < count; i++) {
            IProjectileFactory factory = ProjectileManager.getInstance().getFactory(BuiltInRegistries.ITEM.getKey(projectileProps.getItem()));
            ProjectileEntity projectileEntity = factory.create(world, player, heldItem, item, modifiedGun);
            projectileEntity.setWeapon(heldItem);
            projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(heldItem));
            world.addFreshEntity(projectileEntity);
            spawnedProjectiles[i] = projectileEntity;
            projectileEntity.tick();
        }

        if (!projectileProps.shouldHideProjectile()) {
            sendProjectileTrail(player, spawnedProjectiles, projectileProps);
        }
    }
    private static void sendProjectileTrail(ServerPlayer player, ProjectileEntity[] projectiles, Gun.Projectile projectileProps) {
        if (projectileProps.shouldHideTrail()) {
            return;
        }

        double spawnX = player.getX();
        double spawnY = player.getY() + 1.0;
        double spawnZ = player.getZ();
        double radius = Config.COMMON.network.projectileTrackingRange.get();
        ParticleOptions data = GunEnchantmentHelper.getParticle(player.getMainHandItem());

        S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(
                projectiles,
                projectileProps,
                player.getId(),
                data);

        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(player.serverLevel(), spawnX, spawnY, spawnZ, radius),
                messageBulletTrail);
    }
    private static void playFireSound(ServerPlayer player, Level world, ItemStack heldItem, Gun modifiedGun, ResourceLocation fireSound) {
        double posX = player.getX();
        double posY = player.getY() + player.getEyeHeight();
        double posZ = player.getZ();
        float volume = GunModifierHelper.getFireSoundVolume(heldItem);
        float pitch = 0.9F + world.random.nextFloat() * 0.2F;
        double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
        boolean muzzle = modifiedGun.getDisplay().getFlash() != null;

        S2CMessageGunSound messageSound = new S2CMessageGunSound(
                fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ,
                volume, pitch, player.getId(), muzzle, false);

        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(player.serverLevel(), posX, posY, posZ, radius),
                messageSound);
        DistantGunSoundRouter.send(
                player.serverLevel(),
                new Vec3(posX, posY, posZ),
                SoundSource.PLAYERS,
                volume,
                pitch,
                player.getId(),
                GunModifierHelper.isSilencedFire(heldItem),
                radius
        );
    }
    private static void handleBeamWeapon(ServerPlayer player, ItemStack heldItem, Gun modifiedGun) {
        UUID playerId = player.getUUID();
        Level world = player.level();
        Vec3 beamOriginOffset = new Vec3(0.0, player.getEyeHeight(), 0.0);
        Vec3 beamOrigin = player.position().add(beamOriginOffset);
        Vec3 lookVec = player.getLookAngle();
        double maxDistance = modifiedGun.getGeneral().getBeamMaxDistance();
        Vec3 endVec = beamOrigin.add(lookVec.scale(maxDistance));

        HitResult finalHitResult = BeamHandlerCommon.BeamMiningManager.getBeamHitResult(
                world, beamOrigin, endVec, player, maxDistance);

        Vec3 hitPos = finalHitResult.getLocation();
        List<BlockHitResult> glassPenetrations = new ArrayList<>();
        double damageMultiplier = 1.0;

        if (finalHitResult instanceof BeamHandlerCommon.BeamMiningManager.ExtendedBlockHitResult extendedBlock) {
            glassPenetrations = extendedBlock.getGlassPenetrations();
            damageMultiplier = extendedBlock.getDamageMultiplier();
        } else if (finalHitResult instanceof BeamHandlerCommon.BeamMiningManager.ExtendedEntityHitResult extendedEntity) {
            damageMultiplier = extendedEntity.getDamageMultiplier();
        }

        long currentTime = System.currentTimeMillis();
        boolean isBeamFireMode = modifiedGun.getGeneral().getFireMode() == FireMode.BEAM;
        BeamHandler.BeamInfo beamInfo = activeBeams.computeIfAbsent(playerId,
                k -> new BeamHandler.BeamInfo(beamOrigin, hitPos, currentTime, isBeamFireMode));
        beamInfo.startPos = beamOrigin;
        beamInfo.endPos = hitPos;

        double radius = 64.0;
        S2CMessageBeamUpdate beamUpdate = new S2CMessageBeamUpdate(playerId, beamOrigin, hitPos);
        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(player.serverLevel(), beamOrigin.x, beamOrigin.y, beamOrigin.z, radius),
                beamUpdate
        );

        if (!glassPenetrations.isEmpty()) {
            S2CMessageBeamPenetration penetrationMessage = new S2CMessageBeamPenetration(playerId, glassPenetrations);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create(player.serverLevel(), beamOrigin.x, beamOrigin.y, beamOrigin.z, radius),
                    penetrationMessage
            );
        }
        int damageDelayMs = Math.max(1, modifiedGun.getGeneral().getBeamDamageDelay());
        if (finalHitResult.getType() == HitResult.Type.BLOCK) {
            assert finalHitResult instanceof BlockHitResult;
            BlockHitResult blockHit = (BlockHitResult) finalHitResult;
            if (!glassPenetrations.contains(blockHit)) {
                BeamHandlerCommon.BeamMiningManager.updateBlockMining(world, blockHit, player, modifiedGun);
            }
        }

        if (currentTime - beamInfo.lastDamageTime >= damageDelayMs) {
            handleBeamEffects(player, finalHitResult, modifiedGun, damageMultiplier);
            beamInfo.lastDamageTime = currentTime;
        }

        if (modifiedGun.getGeneral().getFireMode() == FireMode.BEAM &&
                currentTime - beamInfo.startTime >= modifiedGun.getGeneral().getBeamAmmoConsumptionDelay()) {
            consumeAmmo(player, heldItem);
            beamInfo.startTime = currentTime;
        }
    }
    public static void handleStopBeam(ServerPlayer player) {
        UUID playerId = player.getUUID();
        double radius = 64.0;
        S2CMessageStopBeam stopBeamMessage = new S2CMessageStopBeam(playerId);
        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(player.serverLevel(), player.getX(), player.getY(), player.getZ(), radius),
                stopBeamMessage
        );
    }
    public static EntityHitResult rayTraceEntities(Level world, Entity shooter, Vec3 startVec, Vec3 endVec) {
        endVec.subtract(startVec).normalize();
        double maxDistance = startVec.distanceTo(endVec);
        AABB searchArea = new AABB(startVec, endVec).inflate(1.0D);

        Entity closestEntity = null;
        Vec3 hitVec = null;
        double minDistance = maxDistance;

        List<Entity> entities = world.getEntities(shooter, searchArea, entity -> !entity.isSpectator() && entity.isPickable() && entity.isAlive());

        for (Entity entity : entities) {
            AABB entityBB = entity.getBoundingBox().inflate(0.3D);
            Optional<Vec3> optionalHit = entityBB.clip(startVec, endVec);

            if (optionalHit.isPresent()) {
                double distance = startVec.distanceTo(optionalHit.get());

                if (distance < minDistance) {
                    minDistance = distance;
                    closestEntity = entity;
                    hitVec = optionalHit.get();
                }
            }
        }

        if (closestEntity != null) {
            return new EntityHitResult(closestEntity, hitVec);
        }

        return null;
    }
    // Modify the handleBeamEffects method in ServerPlayHandler.java

    private static void handleBeamEffects(ServerPlayer player, HitResult hitResult, Gun modifiedGun, double damageMultiplier) {
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity hitEntity = entityHitResult.getEntity();
            if (!hitEntity.isAttackable()) {
                return;
            }
            if (hitEntity instanceof Player hitPlayer && !player.canHarmPlayer(hitPlayer)) {
                return;
            }
            ItemStack weapon = player.getMainHandItem();

            if (weapon.getItem() instanceof GunItem gunItem && gunItem.equals(ModItems.FLAYED_GOD.get())) {
                if (hitEntity instanceof LivingEntity livingEntity) {
                    RandomSource random = player.level().random;
                    if (random.nextFloat() < 0.75f) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, true));
                    }
                    if (random.nextFloat() < 0.5f) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true));
                    }
                    if (random.nextFloat() < 0.5f) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0, false, true));
                    }
                }
            }
            float damage = modifiedGun.getProjectile().getDamage();
            damage = GunModifierHelper.getModifiedDamage(weapon, modifiedGun, damage);
            damage = GunEnchantmentHelper.getAcceleratorDamage(weapon, damage);
            damage = GunEnchantmentHelper.getHeavyShotDamage(weapon, damage);
            damage = GunEnchantmentHelper.getHotBarrelDamage(player, weapon, damage);
            damage *= damageMultiplier;

            // ADD THIS LINE - Apply global damage multiplier to beam weapons
            damage *= Config.COMMON.gameplay.globalDamageMultiplier.get().floatValue();

            if (hitResult instanceof ExtendedEntityRayTraceResult extendedResult && extendedResult.isHeadshot()) {
                damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
            }
            DamageSource damageSource = ModDamageTypes.Sources.projectile(player.server.registryAccess(), null, player);
            if (hitEntity instanceof LivingEntity livingEntity) {
                damage += EnchantmentHelper.modifyDamage(player.serverLevel(), weapon, livingEntity, damageSource, 0.0F);
            }

            boolean damaged = hitEntity.hurt(damageSource, damage);

            if (damaged) {
                hitEntity.invulnerableTime = 0;
                if (hitEntity instanceof LivingEntity livingEntity) {
                    GunEnchantmentHelper.applyElementalPopEffect(weapon, livingEntity);
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(player.serverLevel(), livingEntity, damageSource, weapon);
                    if (GunEnchantmentHelper.shouldSetOnFire(player, weapon)) {
                        hitEntity.igniteForSeconds(5);
                    }
                }
            }
            PacketHandler.getPlayChannel().sendToPlayer(() -> player,
                    new S2CMessageBeamImpact(hitResult.getLocation(), player.getUUID()));
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            PacketHandler.getPlayChannel().sendToPlayer(() -> player,
                    new S2CMessageBeamImpact(hitResult.getLocation(), player.getUUID()));
        }
    }

    private static void consumeAmmo(ServerPlayer player, ItemStack heldItem) {
        if (!player.isCreative()) {
            CompoundTag tag = getOrCreateCustomData(heldItem);
            if (!tag.getBoolean("IgnoreAmmo")) {
                int currentAmmo = tag.getInt("AmmoCount");
                if (currentAmmo > 0) { // Prevent negative ammo
                    tag.putInt("AmmoCount", currentAmmo - 1);
                    setCustomData(heldItem, tag);
                }
            }
        }
    }


    public static void handlePreFireSound(ServerPlayer player) {
        Level world = player.level();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.getItem() instanceof GunItem item && (Gun.hasAmmo(heldItem) || player.isCreative()))
        {
            Gun modifiedGun = item.getModifiedGun(heldItem);
            ResourceLocation fireSound = getPreFireSound(heldItem, modifiedGun);
            if(fireSound != null)
            {
                double posX = player.getX();
                double posY = player.getY() + player.getEyeHeight();
                double posZ = player.getZ();
                float volume = GunModifierHelper.getFireSoundVolume(heldItem);
                float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
                S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, player.getId(), false, false);
                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.serverLevel(), posX, posY, posZ, radius), messageSound);
            }
        }
    }
    private static ResourceLocation getFireSound(ItemStack stack, Gun modifiedGun)
    {
        ResourceLocation fireSound = null;
        if(GunModifierHelper.isSilencedFire(stack))
        {
            fireSound = modifiedGun.getSounds().getSilencedFire();
        }
        else if(stack.isEnchanted())
        {
            fireSound = modifiedGun.getSounds().getEnchantedFire();
        }
        if(fireSound != null)
        {
            return fireSound;
        }
        return modifiedGun.getSounds().getFire();
    }

    private static ResourceLocation getPreFireSound(ItemStack stack, Gun modifiedGun)
    {
        return modifiedGun.getSounds().getPreFire();
    }

    /**
     * @param player
     */
    public static void handleUnload(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(stack);
            CompoundTag tag = getCustomData(stack);
            if (player.getInventory().items.stream().anyMatch(i -> i.getItem() instanceof CreativeAmmoBoxItem) ||
                    hasCreativeAmmoBoxInCurios(player)) {
                return;
            }

            if (gun.getReloads().getReloadType() != ReloadType.SINGLE_ITEM) {
                if (tag != null && tag.contains("AmmoCount", Tag.TAG_INT)) {
                    int count = tag.getInt("AmmoCount");
                    ItemStack loadedAmmo = Gun.getLoadedAmmoStack(stack, gun, 1, player.registryAccess());
                    tag.putInt("AmmoCount", 0);
                    setCustomData(stack, tag);
                    Gun.clearLoadedProjectileItem(stack);

                    if (loadedAmmo.isEmpty()) return;

                    int maxStackSize = loadedAmmo.getMaxStackSize();
                    int stacks = count / maxStackSize;
                    for (int i = 0; i < stacks; i++) {
                        spawnAmmo(player, loadedAmmo.copyWithCount(maxStackSize));
                    }

                    int remaining = count % maxStackSize;
                    if (remaining > 0) {
                        spawnAmmo(player, loadedAmmo.copyWithCount(remaining));
                    }
                }
            }
        }
    }

    /**
     * Puts one ammo box from the main hand on the belt, like equipping armour: an empty belt slot first, otherwise
     * the two slots take turns and the worn box goes back into the hand.
     */
    public static void handleEquipAmmoBoxes(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof AmmoBoxItem)) {
            return;
        }
        CuriosApi.getCuriosInventory(player).flatMap(inventory -> inventory.getStacksHandler("belt")).ifPresent(handler -> {
            int slots = handler.getSlots();
            int target = -1;
            for (int slot = 0; slot < slots && target < 0; slot++) {
                if (handler.getStacks().getStackInSlot(slot).isEmpty() && canWearOnBelt(player, slot, held)) {
                    target = slot;
                }
            }
            CompoundTag data = player.getPersistentData();
            if (target < 0) {
                int next = data.getInt(NEXT_BELT_SLOT);
                for (int i = 0; i < slots && target < 0; i++) {
                    int slot = (next + i) % slots;
                    if (canWearOnBelt(player, slot, held)) {
                        target = slot;
                    }
                }
            }
            if (target < 0) {
                return;
            }
            data.putInt(NEXT_BELT_SLOT, (target + 1) % slots);

            ItemStack worn = handler.getStacks().getStackInSlot(target);
            handler.getStacks().setStackInSlot(target, held.split(1));
            if (!worn.isEmpty()) {
                if (held.isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, worn);
                } else if (!player.getInventory().add(worn)) {
                    player.drop(worn, false);
                }
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        });
    }

    private static final String NEXT_BELT_SLOT = "scguns:next_ammo_belt_slot";

    private static boolean canWearOnBelt(ServerPlayer player, int slot, ItemStack stack) {
        return CuriosApi.isStackValid(new SlotContext("belt", player, slot, false, true), stack);
    }

    public static boolean hasCreativeAmmoBoxInCurios(ServerPlayer player) {
        AtomicBoolean found = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                if (curios.getStackInSlot(i).getItem() instanceof CreativeAmmoBoxItem) {
                    found.set(true);
                    return;
                }
            }
        });
        return found.get();
    }
    /**
     * @param player
     */
    public static void handleExtraAmmo(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem gunItem) {
            boolean hasCreativeBox = player.getInventory().items.stream().anyMatch(i -> i.getItem() instanceof CreativeAmmoBoxItem) ||
                    hasCreativeAmmoBoxInCurios(player);

            Gun gun = gunItem.getModifiedGun(stack);
            CompoundTag tag = getCustomData(stack);
            if (tag != null && tag.contains("AmmoCount", Tag.TAG_INT)) {
                int currentAmmo = tag.getInt("AmmoCount");
                int modifiedCapacity = GunModifierHelper.getModifiedAmmoCapacity(stack, gun);

                if (currentAmmo > modifiedCapacity) {
                    tag.putInt("AmmoCount", modifiedCapacity);
                    setCustomData(stack, tag);
                    if (!hasCreativeBox) {
                        ItemStack residue = Gun.getLoadedAmmoStack(stack, gun, currentAmmo - modifiedCapacity, player.registryAccess());
                        if (!residue.isEmpty()) {
                            spawnAmmo(player, residue);
                        }
                    }
                }
            }
        }
    }
    /**
     * @param player
     * @param stack
     */
    private static void spawnAmmo(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
        if (stack.getCount() > 0) {
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack.copy()));
        }
    }

    /**
     * @param player
     */
    public static void handleAttachments(ServerPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            player.openMenu(new SimpleMenuProvider((windowId, playerInventory, player1) -> new AttachmentContainer(windowId, playerInventory, heldItem), Component.translatable("container.scguns.attachments")));
        }
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : null;
    }

    private static CompoundTag getOrCreateCustomData(ItemStack stack) {
        CompoundTag tag = getCustomData(stack);
        return tag != null ? tag : new CompoundTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
