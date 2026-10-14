package top.ribs.scguns.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.cache.HotBarrelCache;
import top.ribs.scguns.client.handler.MeleeAttackHandler;
import top.ribs.scguns.common.*;
import top.ribs.scguns.common.network.ServerPlayHandler;
import top.ribs.scguns.init.*;
import top.ribs.scguns.interfaces.IAirGun;
import top.ribs.scguns.interfaces.IEnergyGun;
import top.ribs.scguns.compat.CreateBacktank;
import top.ribs.scguns.item.*;
import top.ribs.scguns.item.ammo_boxes.EmptyCasingPouchItem;
import top.ribs.scguns.item.animated.*;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageReload;
import top.ribs.scguns.network.message.S2CMessageHotBarrelSync;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;


@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class GunEventBus {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerLevel world : server.getAllLevels()) {
                BeamHandlerCommon.BeamMiningManager.tickMiningProgress(world);
            }
        }
    }
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        for (ServerLevel world : server.getAllLevels()) {
          TemporaryLightManager.emergencyCleanup(world);
        }
    }

    @SubscribeEvent
    public static void preShoot(GunFireEvent.Pre event) {
        Player player = event.getEntity();
        Level level = event.getEntity().level();
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = getCustomData(heldItem);

        if (MeleeAttackHandler.isBanzaiActive()) {
            MeleeAttackHandler.stopBanzai();
        }

        if (level.isClientSide() && heldItem.getItem() instanceof AnimatedGunItem animatedGunItem) {
            try {
                long id = GeoItem.getId(heldItem);
                AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache()
                        .getManagerForId(id)
                        .getAnimationControllers()
                        .get("controller");

                if (animationController != null) {
                    if (heldItem.isDamageableItem() &&
                            heldItem.getDamageValue() >= (heldItem.getMaxDamage() - 1)) {
                        player.displayClientMessage(Component.translatable("message.scguns.gun_broken")
                                .withStyle(ChatFormatting.RED), true);
                        event.setCanceled(true);
                        return;
                    }

                    if (animatedGunItem.isAnimationPlaying(animationController, "reload_stop")) {
                        event.setCanceled(true);
                        return;
                    }

                    if (tag.getBoolean("scguns:IsReloading")) {
                        if (animatedGunItem.isAnimationPlaying(animationController, "reload_loop") ||
                                animatedGunItem.isAnimationPlaying(animationController, "reload_start")) {
                            tag.putBoolean("scguns:ReloadComplete", true);
                            animationController.tryTriggerAnimation("reload_stop");
                            tag.remove("scguns:IsReloading");
                            setCustomData(heldItem, tag);
                            ModSyncedDataKeys.RELOADING.setValue(player, false);
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
                        }
                        event.setCanceled(true);
                        return;
                    }
                }
            } catch (Exception e) {
                ScorchedGuns.LOGGER.error("Error in preShoot animation handling: " + e.getMessage());
            }
        }
        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);
            GripType gripType = gun.determineGripType(heldItem);

            if (player.isUsingItem() && player.getOffhandItem().getItem() == Items.SHIELD
                    && (gripType == GripType.ONE_HANDED || gripType == GripType.ONE_HANDED_2)) {
                event.setCanceled(true);
                return;
            }
            if (tag.contains("DrawnTick") &&
                    tag.getInt("DrawnTick") < 15) {
                event.setCanceled(true);
                return;
            }

            int energyUse = gun.getGeneral().getEnergyUse();
            if (!player.isCreative()) {
                if (heldItem.getItem() instanceof IEnergyGun) {
                    IEnergyStorage energyStorage = heldItem.getCapability(Capabilities.EnergyStorage.ITEM);
                    if (energyStorage == null) {
                        throw new IllegalStateException();
                    }
                    if (energyStorage.getEnergyStored() >= energyUse) {
                        energyStorage.extractEnergy(energyUse, false);
                    } else {
                        player.displayClientMessage(Component.translatable("message.energy_gun.no_energy")
                                .withStyle(ChatFormatting.RED), true);
                        event.setCanceled(true);
                        return;
                    }
                }

                if (heldItem.getItem() instanceof IAirGun) {
                    int airCostPerShot = getAirCostPerShot(gun);
                    boolean consumedAir = false;
                    if (ScorchedGuns.createLoaded) {
                        List<ItemStack> backtanks = CreateBacktankBridge.getAllWithAir(player);
                        if (!backtanks.isEmpty()) {
                            CreateBacktankBridge.consumeAir(player, backtanks.get(0), airCostPerShot);
                            consumedAir = true;
                        }
                    }
                    if (!consumedAir) {
                        player.displayClientMessage(Component.translatable("message.airgun.no_air")
                                .withStyle(ChatFormatting.RED), true);
                        event.setCanceled(true);
                        return;
                    }
                } else if (CreateBacktank.isCrusaderBoosted(player, heldItem)) {
                    // Optional boost: the faster shot draws air, an empty or missing backtank just fires normally
                    CreateBacktank.consumeAir(player, CreateBacktank.getAllWithAir(player).get(0), getAirCostPerShot(gun));
                }
            }

            if ((heldItem.getItem() instanceof NonUnderwaterGunItem ||
                    heldItem.getItem() instanceof NonUnderwaterAnimatedGunItem) &&
                    player.isUnderWater()) {
                event.setCanceled(true);
                return;
            }

            if (heldItem.isDamageableItem() && tag != null) {
                if (heldItem.getDamageValue() == (heldItem.getMaxDamage() - 1)) {
                    level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK,
                            SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.displayClientMessage(Component.translatable("message.scguns.gun_broken")
                            .withStyle(ChatFormatting.RED), true);
                    event.getEntity().getCooldowns().addCooldown(event.getStack().getItem(),
                            gun.getGeneral().getRate());
                    event.setCanceled(true);
                    return;
                }
                int maxDamage = heldItem.getMaxDamage();
                int currentDamage = heldItem.getDamageValue();
                int gunRustLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.GUN_RUST, heldItem);

                double jamChance = 0.0;

                if (gunRustLevel > 0) {
                    jamChance = 0.10 + (gunRustLevel * 0.05);
                }

                if (currentDamage >= maxDamage * 0.8) {
                    jamChance += 0.025;
                }

                if (jamChance > 0 && Math.random() < jamChance) {
                    event.getEntity().playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
                    player.displayClientMessage(Component.translatable("message.scguns.gun_jammed")
                            .withStyle(ChatFormatting.YELLOW), true);

                    int coolDown = gun.getGeneral().getRate() * 10;
                    if (coolDown > 30) {
                        coolDown = 30;
                    }
                    event.getEntity().getCooldowns().addCooldown(event.getStack().getItem(), coolDown);
                    event.setCanceled(true);
                    return;
                }
                if (tag.getInt("AmmoCount") >= 1) {
                    broken(heldItem, level, player);
                }
            }
        }
    }
    @SubscribeEvent
    public static void postShoot(GunFireEvent.Post event) {
        Player player = event.getEntity();
        Level level = event.getEntity().level();
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = getOrCreateCustomData(heldItem);

        if (heldItem.getItem() instanceof AnimatedGunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);
            if (gun.getGeneral().isRevolver()) {
                ((AnimatedGunItem)heldItem.getItem()).getRotationHandler().incrementCylinderRotation(30.0f);
            }
            if (heldItem.getItem().toString().contains("cogloader")) {
                ((AnimatedGunItem)heldItem.getItem()).getRotationHandler().incrementMagazineRotation(15.0f);
            }
            if (heldItem.getItem().toString().contains("scrapper")) {
                float maxAmmo = Gun.getMaxAmmo(heldItem);
                float currentAmmo = Gun.getAmmoCount(heldItem);
                float slidePosition = Math.min((maxAmmo - currentAmmo) / maxAmmo, 1.0f);
                tag.putFloat("MagazinePosition", slidePosition);
                setCustomData(heldItem, tag);
            }
            if (level.isClientSide()) {
                long id = GeoItem.getId(heldItem);
                var manager = gunItem.getAnimatableInstanceCache().getManagerForId(id);
                AnimationController<GeoAnimatable> controller = manager != null
                        ? manager.getAnimationControllers().get("controller")
                        : null;

                if (controller != null) {
                    controller.forceAnimationReset();
                    boolean isCarbine = gunItem.isInCarbineMode(heldItem);

                    if (gunItem instanceof AnimatedDualWieldGunItem) {
                        ServerPlayHandler.RatKingAndQueenModel.GunFireEventRatHandler.incrementShotCount();
                        boolean useAlternate = ServerPlayHandler.RatKingAndQueenModel.GunFireEventRatHandler.shouldUseAlternateAnimation();

                        if (ModSyncedDataKeys.AIMING.getValue(player)) {
                            controller.tryTriggerAnimation(useAlternate ? "aim_shoot1" : "aim_shoot");
                        } else {
                            controller.tryTriggerAnimation(useAlternate ? "shoot1" : "shoot");
                        }
                    } else {
                        if (ModSyncedDataKeys.AIMING.getValue(player)) {
                            controller.tryTriggerAnimation(isCarbine ? "carbine_aim_shoot" : "aim_shoot");
                        } else {
                            controller.tryTriggerAnimation(isCarbine ? "carbine_shoot" : "shoot");
                        }
                    }
                }
            }
        }
        if (heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);
            int hotBarrelLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL, heldItem);

            if (gun.getGeneral().hasPlayerKnockBack()) {
                applyGunKnockback(player, gun);
            }

            if (hotBarrelLevel > 0) {
                int hotBarrelFillRate = gun.getGeneral().getHotBarrelRate();
                HotBarrelCache.increaseHotBarrel(player, heldItem, hotBarrelFillRate);
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    int newLevel = HotBarrelCache.getHotBarrelLevel(player, heldItem);
                    PacketHandler.getPlayChannel().sendToPlayer(
                            () -> serverPlayer,
                            new S2CMessageHotBarrelSync(newLevel, heldItem.getItem().getDescriptionId())
                    );
                }
            }
            if (gun.getGeneral().isEnableGunLight()) {
                Vec3 lookVec = player.getLookAngle();
                BlockPos lightPos = player.blockPosition()
                        .offset((int)(lookVec.x * 2), 2, (int)(lookVec.z * 2));
                boolean isBeamWeapon = gun.getGeneral().getFireMode() == FireMode.BEAM;
                TemporaryLightManager.addTemporaryLight(level, lightPos, isBeamWeapon);
            }

            int shotCount = ServerPlayHandler.RatKingAndQueenModel.GunFireEventRatHandler.getShotCount();
            boolean mirror = (heldItem.getItem() instanceof AnimatedDualWieldGunItem && (shotCount % 2 == 1));

            if (Config.COMMON.gameplay.spawnCasings.get()) {
                if (gun.getProjectile().ejectsCasing() && !gun.getProjectile().ejectDuringReload()) {
                    if (tag.getInt("AmmoCount") >= 1 || player.getAbilities().instabuild) {
                        ejectCasing(level, player, mirror);
                    }
                }

            }

            if (heldItem.isDamageableItem()) {
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() / 1.5) && Math.random() < 0.15) {
                    level.playSound(player, player.blockPosition(), ModSounds.COPPER_GUN_JAM.get(), SoundSource.PLAYERS, 1.0F, 1.0f);
                }
            }
        }
    }

    private static void applyGunKnockback(Player player, Gun gun) {
        Vec3 lookVec = player.getLookAngle();
        float baseStrength = gun.getGeneral().getPlayerKnockBackStrength();
        float totalKnockbackResistance = 0.0F;
        totalKnockbackResistance += (float) player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) * 0.5f;

        for (ItemStack armorPiece : player.getArmorSlots()) {
            if (armorPiece.getItem() instanceof ArmorItem armor) {
                ArmorMaterial material = armor.getMaterial().value();
                totalKnockbackResistance += material.knockbackResistance() * 0.25f;
            }
        }
        totalKnockbackResistance = Math.min(0.75F, totalKnockbackResistance);
        float effectiveStrength = baseStrength * (1.0F - totalKnockbackResistance);
        if (effectiveStrength > 0) {
            double verticalBoost;
            if (lookVec.y < -0.5 && !player.onGround() && player.getDeltaMovement().y > 0) {
                verticalBoost = effectiveStrength * 1.25;
            } else {
                verticalBoost = 0.1 * effectiveStrength;
            }

            player.setDeltaMovement(player.getDeltaMovement().add(
                    -lookVec.x * effectiveStrength,
                    verticalBoost,
                    -lookVec.z * effectiveStrength
            ));

            if (verticalBoost > 0.5) {
                player.fallDistance = 0;
            }

            if (player instanceof ServerPlayer) {
                ((ServerPlayer) player).connection.send(new ClientboundSetEntityMotionPacket(player));
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof GunItem &&
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL, heldItem) > 0) {
            int levelBefore = HotBarrelCache.getHotBarrelLevel(player, heldItem);
            if (levelBefore > 0) {
            }
            HotBarrelCache.tickHotBarrel(player, heldItem);

        } else {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack itemStack = player.getInventory().getItem(i);
                if (itemStack.getItem() instanceof GunItem &&
                        EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL, itemStack) > 0) {

                    if (HotBarrelCache.getHotBarrelLevel(player, itemStack) > 0) {
                        HotBarrelCache.clearHotBarrel(player, itemStack);

                    }
                }
            }
        }
        if (player.tickCount % 1200 == 0) {
            HotBarrelCache.cleanupOldEntries();
        }
    }




    /**
     * Backtank air used per shot. A full Create backtank (900 air) gives a potato cannon 200 shots; guns hit harder,
     * so they draw a bit more: a quarter of the gun's air rating, i.e. roughly 90-225 shots per tank.
     */
    public static int getAirCostPerShot(Gun gun) {
        return Math.max(1, Math.round(gun.getGeneral().getEnergyUse() * 0.25F));
    }

    public static void broken(ItemStack stack, Level level, Player player) {
        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamageValue();
        if (currentDamage >= (maxDamage - 2)) {
            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
    public static void damageGun(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild && Config.COMMON.gameplay.enableGunDamage.get()) {
            if (stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int currentDamage = stack.getDamageValue();
                boolean isUnderwater = player.isUnderWater();
                boolean isUnderwaterGun = stack.getItem() instanceof UnderwaterGunItem
                        || stack.getItem() instanceof AnimatedUnderWaterGunItem;

                int damageAmount = 1;
                int waterProofLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WATER_PROOF, stack);
                int acceleratorLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ACCELERATOR, stack);

                if (isUnderwater) {
                    if (isUnderwaterGun) {
                        if (waterProofLevel > 0) {
                            if (Math.random() < 0.25) {
                                damageAmount = 0;
                            }
                        }
                    } else {
                        if (waterProofLevel > 0) {
                        } else {
                            damageAmount = 3;
                        }
                    }
                }

                if (acceleratorLevel > 0 && damageAmount > 0) {
                    float extraWearChance = 0.15f * acceleratorLevel;
                    if (Math.random() < extraWearChance) {
                        damageAmount *= 2;
                    }
                }

                if (currentDamage >= (maxDamage - damageAmount)) {
                    if (currentDamage >= (maxDamage - damageAmount - 1)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                } else {
                    stack.hurtAndBreak(damageAmount, player, null);
                }
            }
        }
    }


    public static void damageAttachments(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild && Config.COMMON.gameplay.enableAttachmentDamage.get()) {
            if (stack.getItem() instanceof GunItem) {
                damageAttachment(stack, level, player, IAttachment.Type.SCOPE);
                damageAttachment(stack, level, player, IAttachment.Type.BARREL);
                damageAttachment(stack, level, player, IAttachment.Type.STOCK);
                damageAttachment(stack, level, player, IAttachment.Type.MAGAZINE);
                damageAttachment(stack, level, player, IAttachment.Type.UNDER_BARREL);
            }
        }
    }

    private static void damageAttachment(ItemStack gunStack, Level level, Player player, IAttachment.Type type) {
        if (!Gun.hasAttachmentEquipped(gunStack, type)) {
            return;
        }

        ItemStack attachmentStack = Gun.getAttachment(type, gunStack);
        if (!attachmentStack.isDamageableItem()) {
            return;
        }

        int maxDamage = attachmentStack.getMaxDamage();
        int currentDamage = attachmentStack.getDamageValue();
        if (currentDamage >= maxDamage - 1) {
            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            Gun.removeAttachment(gunStack, type.getTagKey());
        } else {
            attachmentStack.hurtAndBreak(1, player, null);
            Gun.setAttachment(gunStack, type, attachmentStack, player.registryAccess());
        }
    }

    public static void ejectCasing(Level level, LivingEntity livingEntity, boolean mirror) {
        if (!level.isClientSide()) return;

        Player playerEntity = (Player) livingEntity;
        ItemStack heldItem = playerEntity.getMainHandItem();
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        Vec3 lookVec = playerEntity.getLookAngle();
        Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        Vec3 forwardVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        double offsetX = (mirror ? -rightVec.x : rightVec.x) * 0.5 + forwardVec.x * 0.5;
        double offsetY = playerEntity.getEyeHeight() - 0.4;
        double offsetZ = (mirror ? -rightVec.z : rightVec.z) * 0.5 + forwardVec.z * 0.5;

        Vec3 particlePos = playerEntity.getPosition(1).add(offsetX, offsetY, offsetZ);
        ResourceLocation particleLocation = gun.getProjectile().getCasingParticle();

        if (particleLocation != null) {
            ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.getOptional(particleLocation).orElse(null);
            if (particleType instanceof SimpleParticleType simpleParticleType) {
                level.addParticle(simpleParticleType,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0, 0);
            }
        }
    }


    /**
     * Every fired round leaves its casing behind. With Shell Catcher the casing goes into a casing pouch,
     * then the player's inventory, and only falls to the ground when neither has room.
     */
    public static void dropCasing(Player player, ItemStack gunStack, ItemStack casingStack) {
        if (casingStack.isEmpty()) {
            return;
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHELL_CATCHER, gunStack) > 0) {
            if (addCasingToPouch(player, casingStack)) {
                return;
            }
            ItemStack remainder = casingStack.copy();
            player.getInventory().add(remainder);
            if (remainder.isEmpty()) {
                return;
            }
            casingStack = remainder;
        }
        spawnCasingInWorld(player.level(), player, casingStack);
    }

    public static void spawnCasingInWorld(Level level, Player player, ItemStack casingStack) {
        ItemEntity casingEntity = new ItemEntity(level, player.getX(), player.getY() + 1.5, player.getZ(), casingStack);
        casingEntity.setPickUpDelay(40);
        casingEntity.setDeltaMovement(0, 0.2, 0);
        level.addFreshEntity(casingEntity);
    }

    public static boolean addCasingToPouch(Player player, ItemStack casingStack) {
        ItemStack casingCopy = casingStack.copy();

        // Check regular inventory for Empty Casing Pouches
        for (ItemStack itemStack : player.getInventory().items) {
            if (itemStack.getItem() instanceof EmptyCasingPouchItem) {
                int insertedItems = EmptyCasingPouchItem.add(itemStack, casingCopy);
                if (insertedItems > 0) {
                    return true;
                }
            }
        }


        // Check Curios slots for Empty Casing Pouches
        final boolean[] result = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            IItemHandlerModifiable curios = handler.getEquippedCurios();
            for (int i = 0; i < curios.getSlots(); i++) {
                ItemStack stack = curios.getStackInSlot(i);
                if (stack.getItem() instanceof EmptyCasingPouchItem) {
                    int insertedItems = EmptyCasingPouchItem.add(stack, casingCopy);
                    if (insertedItems > 0) {
                        result[0] = true;
                        return;
                    }
                }
            }
        });

        return result[0];
    }







    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static CompoundTag getOrCreateCustomData(ItemStack stack) {
        return getCustomData(stack);
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static final class CreateBacktankBridge {
        private static final String BACKTANK_UTIL = "com.simibubi.create.content.equipment.armor.BacktankUtil";

        private static List<ItemStack> getAllWithAir(Player player) {
            try {
                Class<?> bridge = Class.forName(BACKTANK_UTIL);
                Method method = bridge.getMethod("getAllWithAir", net.minecraft.world.entity.LivingEntity.class);
                Object value = method.invoke(null, player);
                if (value instanceof List<?> list) {
                    return list.stream()
                            .filter(ItemStack.class::isInstance)
                            .map(ItemStack.class::cast)
                            .toList();
                }
            } catch (ReflectiveOperationException ignored) {
                return Collections.emptyList();
            }
            return Collections.emptyList();
        }

        private static void consumeAir(Player player, ItemStack stack, float amount) {
            try {
                Class<?> bridge = Class.forName(BACKTANK_UTIL);
                Method method = bridge.getMethod("consumeAir", net.minecraft.world.entity.LivingEntity.class, ItemStack.class, int.class);
                method.invoke(null, player, stack, Math.max(1, Math.round(amount)));
            } catch (ReflectiveOperationException ignored) {
            }
        }

    }
}
