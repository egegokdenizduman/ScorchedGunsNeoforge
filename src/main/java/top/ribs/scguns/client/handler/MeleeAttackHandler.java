package top.ribs.scguns.client.handler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.network.message.S2CMessageMeleeAttack;
import top.ribs.scguns.util.GunModifierHelper;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class MeleeAttackHandler {
    private static final float ENCHANTMENT_DAMAGE_SCALING_FACTOR = 0.70f;
    private static final float BASE_SPEED_DAMAGE_SCALING_FACTOR = 0.0f;
;
    private static final String WALL_COLLISION_COOLDOWN_TAG = "WallCollisionCooldown";
    private static final String MELEE_COOLDOWN_TAG = "MeleeCooldown";
    private static final int KNOCKBACK_GRACE_PERIOD_TICKS = 5;
    private static final String KNOCKBACK_GRACE_TAG = "KnockbackGracePeriod";
    private static boolean isBanzai = false;
    private static final int WALL_COLLISION_COOLDOWN_TICKS = 20;
    private static final double WALL_CHECK_DISTANCE = 1.0;
    private static final double[] WALL_CHECK_ANGLES = {0, 10, -10, 20, -20, 30, -30};
    public static boolean isBanzaiActive() {
        return isBanzai;
    }
    private static final String BANZAI_DAMAGE_COOLDOWN_TAG = "BanzaiDamageCooldown";
    private static final int BANZAI_DAMAGE_COOLDOWN_TICKS = 25;
    private static final double BANZAI_AOE_RADIUS = 1.5;
    private static ItemStack banzaiActiveItem = ItemStack.EMPTY;

    @Nullable
    private static CompoundTag getStackTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : null;
    }

    private static CompoundTag getOrCreateStackTag(ItemStack stack) {
        CompoundTag tag = getStackTag(stack);
        return tag != null ? tag : new CompoundTag();
    }

    private static void setStackTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static void startBanzai(ServerPlayer player) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }
        if (!gunItem.hasBayonet(heldItem)) {
            performMeleeAttack(player);
            return;
        }
        isBanzai = true;
        banzaiActiveItem = heldItem.copy();
    }

    public static void stopBanzai() {
        isBanzai = false;
        banzaiActiveItem = ItemStack.EMPTY;
    }

    public static void performMeleeAttack(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return;
        }
        if (isMeleeOnCooldown(player, heldItem)) {
            return;
        }

        if (heldItem.getItem() instanceof AnimatedGunItem animatedGunItem) {
            CompoundTag tag = getStackTag(heldItem);
            long id = GeoItem.getId(heldItem);
            AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache()
                    .getManagerForId(id)
                    .getAnimationControllers()
                    .get("controller");

            if (tag != null && tag.getBoolean("scguns:IsReloading")) {
                Gun gun = gunItem.getModifiedGun(heldItem);
                if (gun.getReloads().getReloadType() == ReloadType.MAG_FED) {
                    tag.remove("scguns:IsReloading");
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                    setStackTag(heldItem, tag);
                    if (animationController != null) {
                        animationController.forceAnimationReset();
                    }
                } else if (gun.getReloads().getReloadType() == ReloadType.MANUAL) {
                    if (animationController != null &&
                            (animatedGunItem.isAnimationPlaying(animationController, "reload_loop") ||
                                    animatedGunItem.isAnimationPlaying(animationController, "reload_start"))) {
                        tag.putBoolean("scguns:ReloadComplete", true);
                        animationController.tryTriggerAnimation("reload_stop");
                        tag.remove("scguns:IsReloading");
                        ModSyncedDataKeys.RELOADING.setValue(player, false);
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                        setStackTag(heldItem, tag);
                    }
                }
            }
        }
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        if (heldItem.getItem() instanceof AnimatedGunItem) {
            AnimationController<GeoAnimatable> controller = ((AnimatedGunItem)heldItem.getItem())
                    .getAnimatableInstanceCache()
                    .getManagerForId(GeoItem.getId(heldItem))
                    .getAnimationControllers()
                    .get("controller");

            if (controller != null && ((AnimatedGunItem)heldItem.getItem()).isAnimationPlaying(controller, "inspect")) {
                controller.tryTriggerAnimation("idle");
            }
        }

        PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageMeleeAttack(heldItem));
        LivingEntity target = findTargetWithinReach(player, heldItem);
        if (target != null && target != player) {
            performMeleeAttackOnTarget(player, target, false);
            damageGunAndAttachments(heldItem, player);
        } else {
            HitResult hitResult = rayTraceBlocks(player, heldItem);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState blockState = player.level().getBlockState(pos);
                ClientboundLevelParticlesPacket particlePacket = getClientboundLevelParticlesPacket(blockHitResult, blockState);
                player.connection.send(particlePacket);
            }
        }
        setMeleeCooldown(player, heldItem, gunItem);
    }

    @NotNull
    private static ClientboundLevelParticlesPacket getClientboundLevelParticlesPacket(BlockHitResult blockHitResult, BlockState blockState) {
        Vec3 hitVec = blockHitResult.getLocation();
        BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
        return new ClientboundLevelParticlesPacket(
                particleData,
                true,
                hitVec.x,
                hitVec.y,
                hitVec.z,
                0.0F,
                0.0F,
                0.0F,
                0.1F,
                10
        );
    }
    private static HitResult rayTraceBlocks(Player player, ItemStack heldItem) {
        GunItem gunItem = (GunItem)heldItem.getItem();
        float reach = gunItem.getModifiedGun(heldItem).getGeneral().getMeleeReach();

        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle();
        Vec3 reachVector = eyePosition.add(lookVector.scale(reach));
        return player.level().clip(new ClipContext(
                eyePosition,
                reachVector,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
    }
    private static LivingEntity findTargetWithinReach(Player player, ItemStack heldItem) {
        GunItem gunItem = (GunItem)heldItem.getItem();
        float reach = gunItem.getModifiedGun(heldItem).getGeneral().getMeleeReach();

        AABB boundingBox = player.getBoundingBox().inflate(reach, reach, reach);
        return player.level().getEntitiesOfClass(LivingEntity.class, boundingBox,
                        entity -> entity != player && entity.isAlive())
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }


    public static boolean isMeleeOnCooldown(Player player, ItemStack heldItem) {
        CompoundTag tag = getOrCreateStackTag(heldItem);
        long currentTime = player.level().getGameTime();
        return tag.contains(MELEE_COOLDOWN_TAG) && currentTime < tag.getLong(MELEE_COOLDOWN_TAG);
    }

    public static void setMeleeCooldown(Player player, ItemStack heldItem, GunItem gunItem) {
        CompoundTag tag = getOrCreateStackTag(heldItem);
        long currentTime = player.level().getGameTime();
        int cooldownTicks = gunItem.getModifiedGun(heldItem).getGeneral().getMeleeCooldownTicks();
        tag.putLong(MELEE_COOLDOWN_TAG, currentTime + cooldownTicks);
        setStackTag(heldItem, tag);
    }
    private static boolean performMeleeAttackOnTarget(ServerPlayer player, LivingEntity target, boolean isBanzaiAttack) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(heldItem.getItem() instanceof GunItem gunItem)) {
            return false;
        }

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        float additionalDamage = GunModifierHelper.getAdditionalDamage(heldItem, true);

        DamageSource damageSource = player.serverLevel().damageSources().playerAttack(player);

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        float meleeDamage = modifiedGun.getGeneral().getMeleeDamage();


        float attackDamage = baseDamage + additionalDamage + meleeDamage;

        if (isBanzaiAttack) {
            float speedDamageMultiplier = getBanzaiDamageMultiplier(player, heldItem);
            attackDamage *= speedDamageMultiplier;
            attackDamage = (float) (Math.round(attackDamage * 100.0) / 100.0);
        }

        if (isBanzaiAttack) {
            boolean damaged = false;
            List<LivingEntity> targets = findTargetsInArea(player, 2.5);
            for (LivingEntity aoeTarget : targets) {
                if (aoeTarget.hurt(damageSource, attackDamage)) {
                    damaged = true;
                    spawnSuccessfulHitParticles(player, aoeTarget);

                    player.level().playSound(
                            null,
                            aoeTarget.getX(),
                            aoeTarget.getY(),
                            aoeTarget.getZ(),
                            SoundEvents.PLAYER_ATTACK_WEAK,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );

                    applyKnockback(player, aoeTarget, heldItem);
                    triggerBanzaiImpactIfNecessary(heldItem);
                }
            }
            return damaged;
        } else {
            LivingEntity raycastTarget = raycastForMeleeAttack(player, heldItem);
            if (raycastTarget != null && raycastTarget.hurt(damageSource, attackDamage)) {
                spawnSuccessfulHitParticles(player, raycastTarget);

                applyKnockback(player, raycastTarget, heldItem);
                triggerBanzaiImpactIfNecessary(heldItem);
                return true;
            }
        }
        return false;
    }
    private static void spawnSuccessfulHitParticles(ServerPlayer player, LivingEntity target) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        ClientboundLevelParticlesPacket sweepPacket = new ClientboundLevelParticlesPacket(
                ParticleTypes.SWEEP_ATTACK,
                true,
                targetPos.x,
                targetPos.y,
                targetPos.z,
                0.0F,
                0.0F,
                0.0F,
                0.0F,
                1
        );
        player.connection.send(sweepPacket);

        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;

            ClientboundLevelParticlesPacket critPacket = new ClientboundLevelParticlesPacket(
                    ParticleTypes.CRIT,
                    true,
                    targetPos.x + offsetX,
                    targetPos.y + offsetY,
                    targetPos.z + offsetZ,
                    0.0F,
                    0.1F,
                    0.0F,
                    0.1F,
                    1
            );
            player.connection.send(critPacket);
        }
    }
    private static void triggerBanzaiImpactIfNecessary(ItemStack heldItem) {
        if (((GunItem) heldItem.getItem()).hasBayonet(heldItem)) {
            GunRenderingHandler.get().triggerBanzaiImpact();
        }
    }


    public static void performNormalMeleeAttack(ServerPlayer player) {
        performMeleeAttack(player);
    }

    public static void handleBanzaiMode(ServerPlayer player) {
        if (!isBanzai) return;

        ItemStack currentHeldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!ItemStack.matches(currentHeldItem, banzaiActiveItem)) {
            stopBanzai();
            return;
        }

        CompoundTag playerData = player.getPersistentData();
        long currentTime = player.level().getGameTime();

        boolean inGracePeriod = playerData.contains(KNOCKBACK_GRACE_TAG) &&
                currentTime < playerData.getLong(KNOCKBACK_GRACE_TAG);

        if (!player.isSprinting() && !inGracePeriod) {
            stopBanzai();
            return;
        }

        if (checkForWallCollision(player)) {
            knockPlayerBack(player);
            sendWallImpactParticles(player);
            triggerBanzaiImpactIfNecessary(currentHeldItem);
            playerData.putLong(KNOCKBACK_GRACE_TAG, currentTime + KNOCKBACK_GRACE_PERIOD_TICKS);
            return;
        }

        if (playerData.contains(BANZAI_DAMAGE_COOLDOWN_TAG) &&
                currentTime < playerData.getLong(BANZAI_DAMAGE_COOLDOWN_TAG)) {
            return;
        }

        List<LivingEntity> targets = findTargetsInArea(player, BANZAI_AOE_RADIUS);
        if (!targets.isEmpty()) {
            playerData.putLong(BANZAI_DAMAGE_COOLDOWN_TAG, currentTime + BANZAI_DAMAGE_COOLDOWN_TICKS);
            boolean damagedAnyTarget = false;
            for (LivingEntity target : targets) {
                if (target != player) {
                    damagedAnyTarget |= performMeleeAttackOnTarget(player, target, true);
                }
            }
            if (damagedAnyTarget) {
                damageGunAndAttachments(currentHeldItem, player);
            }
        }
    }
    private static boolean checkForWallCollision(ServerPlayer player) {
        CompoundTag playerData = player.getPersistentData();
        long currentTime = player.level().getGameTime();
        if (playerData.contains(WALL_COLLISION_COOLDOWN_TAG) &&
                currentTime < playerData.getLong(WALL_COLLISION_COOLDOWN_TAG)) {
            return false;
        }
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle();
        Vec3 playerMotion = player.getDeltaMovement();
        if (lookVector.dot(playerMotion) <= 0) {
            return false;
        }
        double[] heightOffsets = {0, 0.5, -0.5};
        for (double heightOffset : heightOffsets) {
            Vec3 checkPosition = eyePosition.add(0, heightOffset, 0);
            for (double angle : WALL_CHECK_ANGLES) {
                Vec3 rotatedVector = rotateVector(lookVector, angle);
                Vec3 reachVector = checkPosition.add(rotatedVector.scale(WALL_CHECK_DISTANCE));

                BlockHitResult hitResult = player.level().clip(new ClipContext(
                        checkPosition,
                        reachVector,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                ));

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    playerData.putLong(WALL_COLLISION_COOLDOWN_TAG,
                            currentTime + WALL_COLLISION_COOLDOWN_TICKS);
                    return true;
                }
            }
        }
        return false;
    }

    private static Vec3 rotateVector(Vec3 lookVector, double angle) {
        double angleRadians = Math.toRadians(angle);
        double x = lookVector.x * Math.cos(angleRadians) - lookVector.z * Math.sin(angleRadians);
        double z = lookVector.x * Math.sin(angleRadians) + lookVector.z * Math.cos(angleRadians);
        return new Vec3(x, lookVector.y, z);
    }

    private static void knockPlayerBack(ServerPlayer player) {
        Vec3 knockbackDirection = player.getLookAngle().scale(-0.5);
        player.push(knockbackDirection.x, 0.3, knockbackDirection.z);
        player.hurtMarked = true;
    }
    private static void sendWallImpactParticles(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle();
        Vec3 reachVector = eyePosition.add(lookVector.scale(WALL_CHECK_DISTANCE));
        ClipContext context = new ClipContext(
                eyePosition,
                reachVector,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );
        BlockHitResult hitResult = player.level().clip(context);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = player.level().getBlockState(pos);
            ClientboundLevelParticlesPacket particlePacket = getClientboundLevelParticlesPacket(hitResult, blockState);
            player.connection.send(particlePacket);
            player.level().playSound(
                    null,
                    hitResult.getLocation().x,
                    hitResult.getLocation().y,
                    hitResult.getLocation().z,
                    SoundEvents.SHIELD_BLOCK,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }

    private static float getBanzaiDamageMultiplier(ServerPlayer player, ItemStack heldItem) {
        double speed = player.getDeltaMovement().length();
        return 1.0f + (float) speed * BASE_SPEED_DAMAGE_SCALING_FACTOR;
    }


    private static void applyEnchantmentEffects(ResourceKey<Enchantment> enchantment, int level, LivingEntity target, Player player) {
        if (level <= 0) {
            return;
        }

        if (enchantment == Enchantments.FIRE_ASPECT) {
            target.igniteForSeconds(level * 4);
            spawnClientMeleeParticle(player, target, ParticleTypes.FLAME);
        } else if (enchantment == Enchantments.KNOCKBACK) {
            Vec3 direction = target.position().subtract(player.position()).normalize();
            target.knockback(level * 0.5F, -direction.x(), -direction.z());
        } else if (enchantment == Enchantments.SMITE) {
            spawnClientMeleeParticle(player, target, ParticleTypes.ENCHANTED_HIT);
        } else if (enchantment == Enchantments.BANE_OF_ARTHROPODS) {
            spawnClientMeleeParticle(player, target, ParticleTypes.ENCHANTED_HIT);
        } else if (enchantment == Enchantments.SHARPNESS) {
            spawnClientMeleeParticle(player, target, ParticleTypes.ENCHANTED_HIT);
        }
    }

    private static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        int[] level = {0};
        EnchantmentHelper.runIterationOnItem(stack, (holder, enchantmentLevel) -> {
            if (holder.is(enchantment)) {
                level[0] = Math.max(level[0], enchantmentLevel);
            }
        });
        return level[0];
    }

    private static void spawnClientMeleeParticle(Player player, LivingEntity target, SimpleParticleType particle) {
        if (FMLEnvironment.dist.isClient()) {
            ClientMeleeAttackHandler.spawnParticleEffect(player, target, particle);
        }
    }

    private static void applyKnockback(Player player, LivingEntity target, ItemStack stack) {
        int knockbackLevel = getEnchantmentLevel(stack, Enchantments.KNOCKBACK);
        Vec3 direction = target.position().subtract(player.position()).normalize();
        target.knockback(0.4F + (knockbackLevel * 0.5F), -direction.x(), -direction.z());
    }
    private static LivingEntity raycastForMeleeAttack(Player player, ItemStack heldItem) {
        GunItem gunItem = (GunItem)heldItem.getItem();
        float reach = gunItem.getModifiedGun(heldItem).getGeneral().getMeleeReach();

        Vec3 startVec = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endVec = startVec.add(lookVec.scale(reach));
        AABB boundingBox = new AABB(startVec, endVec);

        return player.level().getEntitiesOfClass(LivingEntity.class, boundingBox,
                        entity -> entity != player && entity.isAlive())
                .stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }
    private static List<LivingEntity> findTargetsInArea(Player player, double radius) {
        Vec3 position = player.position();
        AABB boundingBox = new AABB(position.subtract(radius, radius, radius), position.add(radius, radius, radius));

        return player.level().getEntitiesOfClass(LivingEntity.class, boundingBox, entity -> entity != player && entity.isAlive());
    }
    private static void damageGunAndAttachments(ItemStack stack, Player player) {
        Level level = player.level();
        GunEventBus.damageGun(stack, level, player);
        GunEventBus.damageAttachments(stack, level, player);
    }
}
