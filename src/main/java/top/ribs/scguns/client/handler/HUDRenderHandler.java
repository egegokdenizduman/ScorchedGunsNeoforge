package top.ribs.scguns.client.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.cache.HotBarrelCache;
import top.ribs.scguns.common.ChargeHandler;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.init.ModSyncedDataKeys;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;
import top.ribs.scguns.item.animated.AnimatedGunItem;
import top.ribs.scguns.util.GunModifierHelper;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class HUDRenderHandler {
    private static final ResourceLocation CHARGE_BAR = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/charging_bar.png");
    private static final ResourceLocation FILL_BAR = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/fill_bar.png");
    private static final ResourceLocation MELEE_ATTACK_INDICATOR_PROGRESS = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/melee_attack_indicator_progress.png");
    private static final ResourceLocation MELEE_ATTACK_INDICATOR_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/melee_attack_indicator_background.png");
    public static final ResourceLocation HOT_BARREL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/hot_barrel.png");

    static int meleeCooldown = 0;
    static int maxMeleeCooldown = 0;
    static boolean isMeleeCooldownActive = false;

    private static boolean playingHitMarker = false;
    private static int hitMarkerTime;
    private static int prevHitMarkerTime;
    private static final int hitMarkerMaxTime = 2;
    private static boolean hitMarkerCrit = false;

    private static int reserveAmmo = 0;
    private static int ammoAutoUpdateTimer = 0;
    private static final int ammoAutoUpdateRate = 20;

    private static ItemStack cachedGunStack = ItemStack.EMPTY;
    private static Gun cachedGun = null;
    private static CompoundTag cachedTag = null;
    private static boolean cachedHasCreativeBox = false;
    private static int cacheValidityTimer = 0;
    private static final int CACHE_DURATION = 10;

    private static int cachedScreenWidth = -1;
    private static int cachedScreenHeight = -1;
    private static int screenCacheTimer = 0;
    private static final int SCREEN_CACHE_DURATION = 60;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }

        updateScreenCache(mc);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();


        if (!Config.CLIENT.display.displayGunInfo.get()) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof GunItem)) {
            return;
        }

        updateCache(heldItem, player);

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        renderGunInfoHUD(heldItem, poseStack, guiGraphics, player);
        renderChargeBarHUD(heldItem, partialTick, poseStack, guiGraphics, player);
        renderMeleeCooldownHUD(partialTick, poseStack, guiGraphics);
        renderHitMarker(partialTick, poseStack, guiGraphics);
        renderHotBarrelOverlay(heldItem, poseStack, guiGraphics);
    }

    private static void updateCache(ItemStack heldItem, Player player) {
        if (cacheValidityTimer <= 0 || !ItemStack.matches(cachedGunStack, heldItem)) {
            cachedGunStack = heldItem.copy();
            cachedGun = ((GunItem) heldItem.getItem()).getGun();
            cachedTag = getCustomData(heldItem);

            updateCreativeBoxCache(player);
            cacheValidityTimer = CACHE_DURATION;
        } else {
            cacheValidityTimer--;
        }
    }

    private static void updateCreativeBoxCache(Player player) {
        cachedHasCreativeBox = player.getInventory().items.stream()
                .anyMatch(itemStack -> itemStack.getItem() instanceof CreativeAmmoBoxItem);

        if (!cachedHasCreativeBox) {
            AtomicBoolean hasBox = new AtomicBoolean(false);
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                IItemHandlerModifiable curios = handler.getEquippedCurios();
                for (int i = 0; i < curios.getSlots() && !hasBox.get(); i++) {
                    ItemStack stack = curios.getStackInSlot(i);
                    if (stack.getItem() instanceof CreativeAmmoBoxItem) {
                        hasBox.set(true);
                    }
                }
            });
            cachedHasCreativeBox = hasBox.get();
        }
    }






    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : null;
    }

    private static HolderLookup.Provider registryAccess() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null ? minecraft.level.registryAccess() : Gun.builtInRegistryProvider();
    }

    private static void updateScreenCache(Minecraft mc) {
        if (screenCacheTimer <= 0) {
            cachedScreenWidth = mc.getWindow().getGuiScaledWidth();
            cachedScreenHeight = mc.getWindow().getGuiScaledHeight();
            screenCacheTimer = SCREEN_CACHE_DURATION;
        } else {
            screenCacheTimer--;
        }
    }

    public static void renderHotBarrelOverlay(ItemStack heldItem, PoseStack poseStack, GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HOT_BARREL, heldItem) <= 0) {
            return;
        }

        float chargeRatio = HotBarrelCache.getSmoothHotBarrelPercentage(player, heldItem);
        if (chargeRatio <= 0.0f) {
            return;
        }

        int barWidth = 128;
        int barHeight = 8;
        float scale = 1.25f;
        int barX = (int) (((float) cachedScreenWidth / 2) - ((barWidth * scale) / 2));
        int barY = (int) (cachedScreenHeight - 45 * scale);

        poseStack.pushPose();
        poseStack.scale(scale, scale, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, HOT_BARREL_TEXTURE);

        int scaledBarX = (int) (barX / scale);
        int scaledBarY = (int) (barY / scale);

        guiGraphics.blit(HOT_BARREL_TEXTURE, scaledBarX, scaledBarY, 0, 0, barWidth, 5, barWidth, barHeight);

        int fillWidth = (int) (barWidth * chargeRatio);
        if (fillWidth > 0) {
            int fillY = scaledBarY + 1;
            guiGraphics.blit(HOT_BARREL_TEXTURE, scaledBarX, fillY, 0, 5, fillWidth, 3, barWidth, barHeight);
        }

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderMeleeCooldownHUD(float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (!isMeleeCooldownActive) {
            return;
        }

        int barWidth = 16;
        int barHeight = 16;
        int barX = (int) (cachedScreenWidth * 0.87);
        int barY = (int) (cachedScreenHeight * 0.73);

        float cooldownRatio = (meleeCooldown > 0) ? (float) meleeCooldown / maxMeleeCooldown : 0;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShaderTexture(0, MELEE_ATTACK_INDICATOR_BACKGROUND);
        guiGraphics.blit(MELEE_ATTACK_INDICATOR_BACKGROUND, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (cooldownRatio > 0) {
            int fillHeight = (int) (barHeight * cooldownRatio);
            int fillY = barY + (barHeight - fillHeight);
            RenderSystem.setShaderTexture(0, MELEE_ATTACK_INDICATOR_PROGRESS);
            guiGraphics.blit(MELEE_ATTACK_INDICATOR_PROGRESS, barX, fillY, 0, barHeight - fillHeight, barWidth, fillHeight, barWidth, barHeight);
        }

        RenderSystem.disableBlend();
    }

    private static void renderGunInfoHUD(ItemStack heldItem, PoseStack poseStack, GuiGraphics guiGraphics, Player player) {
        if (!Config.CLIENT.display.displayGunInfo.get()) {
            return;
        }

        if (cachedTag == null) {
            return;
        }
        if (Config.CLIENT.display.immersiveGunInfo.get()) {
            if (heldItem.getItem() instanceof AnimatedGunItem animatedGun) {
                long id = GeoItem.getId(heldItem);
                AnimationController<GeoAnimatable> controller = animatedGun.getAnimatableInstanceCache()
                        .getManagerForId(id)
                        .getAnimationControllers()
                        .get("controller");

                if (controller == null ||
                        (!animatedGun.isAnimationPlaying(controller, "inspect") &&
                                !animatedGun.isAnimationPlaying(controller, "carbine_inspect"))) {
                    return;
                }
            }
        }

        int currentAmmo = cachedTag.getInt("AmmoCount");
        int maxAmmo = GunModifierHelper.getModifiedAmmoCapacity(heldItem, cachedGun);

        boolean isCreative = player.isCreative();
        MutableComponent ammoCountValue;
        MutableComponent reserveAmmoValue;

        if (isCreative) {
            ammoCountValue = Component.literal("∞ / ∞").withStyle(ChatFormatting.BOLD);
            reserveAmmoValue = Component.literal("∞").withStyle(ChatFormatting.BOLD);
        } else {
            ammoCountValue = Component.literal(currentAmmo + " / " + maxAmmo).withStyle(ChatFormatting.BOLD);
            if (cachedHasCreativeBox) {
                reserveAmmoValue = Component.literal("∞").withStyle(ChatFormatting.BOLD);
            } else {
                reserveAmmoValue = Component.literal(String.valueOf(reserveAmmo)).withStyle(ChatFormatting.BOLD);
            }
        }

        int ammoPosX = (int) (cachedScreenWidth * 0.88);
        int ammoPosY = (int) (cachedScreenHeight * 0.8);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft mc = Minecraft.getInstance();

        if (ModSyncedDataKeys.RELOADING.getValue(player)) {
            if (player.isAlive()) {
                guiGraphics.drawString(mc.font, "Reloading...", ammoPosX, ammoPosY - 10, 0xFFFF55);
            }
        }

        guiGraphics.drawString(mc.font, ammoCountValue, ammoPosX, ammoPosY, (currentAmmo > 0 || isCreative ? 0xFFFFFF : 0xFF5555));
        int reserveAmmoPosY = ammoPosY + 10;
        guiGraphics.drawString(mc.font, reserveAmmoValue, ammoPosX, reserveAmmoPosY, (reserveAmmo <= 0 && !Gun.hasUnlimitedReloads(heldItem) ? 0x555555 : 0xAAAAAA));

        ItemStack ammoItemStack = new ItemStack(Objects.requireNonNull(Gun.getDisplayProjectile(heldItem, cachedGun).getItem()));
        renderAmmoTypeTexture(ammoItemStack, ammoPosX - 20, ammoPosY, guiGraphics, mc);

        RenderSystem.disableBlend();
    }

    private static void renderAmmoTypeTexture(ItemStack ammoItemStack, int x, int y, GuiGraphics guiGraphics, Minecraft mc) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        ResourceLocation ammoTexture = BuiltInRegistries.ITEM.getKey(ammoItemStack.getItem());
        TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ResourceLocation.fromNamespaceAndPath(ammoTexture.getNamespace(), "item/" + ammoTexture.getPath()));
        int iconSize = 16;
        int zLevel = 0;
        guiGraphics.blit(x, y, zLevel, iconSize, iconSize, sprite);
    }

    private static void renderChargeBarHUD(ItemStack heldItem, float partialTick, PoseStack poseStack, GuiGraphics guiGraphics, LocalPlayer player) {
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        if (gun.getGeneral().getFireMode() != FireMode.PULSE) {
            return;
        }

        int maxChargeTime = gun.getGeneral().getFireTimer();
        int chargeTime = ChargeHandler.getChargeTime(player.getUUID());
        if (chargeTime <= 0) {
            return;
        }

        float chargeRatio = (float) chargeTime / maxChargeTime;

        int barWidth = 16;
        int barHeight = 16;
        int barX = cachedScreenWidth / 2 - 15;
        int barY = cachedScreenHeight / 2 - barHeight / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.setShaderTexture(0, CHARGE_BAR);
        guiGraphics.blit(CHARGE_BAR, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        if (chargeRatio > 0) {
            int fillBarWidth = 16;
            int fillBarHeight = 16;
            int fillHeight = (int) (fillBarHeight * chargeRatio);
            int fillY = barY + (fillBarHeight - fillHeight);

            RenderSystem.setShaderTexture(0, FILL_BAR);
            guiGraphics.blit(FILL_BAR, barX, fillY, 0, fillBarHeight - fillHeight, fillBarWidth, fillHeight, fillBarWidth, fillBarHeight);
        }

        RenderSystem.disableBlend();
    }

    private static void renderHitMarker(float partialTick, PoseStack poseStack, GuiGraphics guiGraphics) {
        if (!playingHitMarker) {
            return;
        }

        float progress = (prevHitMarkerTime + (hitMarkerTime - prevHitMarkerTime) * partialTick) / (float) hitMarkerMaxTime;
        if (progress >= 1.0F) {
            playingHitMarker = false;
            hitMarkerTime = 0;
        }
    }

    public static void playHitMarker(boolean crit) {
        playingHitMarker = true;
        hitMarkerCrit = crit;
        hitMarkerTime = 1;
        prevHitMarkerTime = 0;
    }

    private static void fetchReserveAmmo(Player player, Gun gun) {
        ItemStack stack = player.getMainHandItem();
        reserveAmmo = Gun.getReserveAmmoCount(player, stack, gun);
        ammoAutoUpdateTimer = 0;
    }

    public static void stageReserveAmmoUpdate() {
        ammoAutoUpdateTimer = ammoAutoUpdateRate;
    }

    public static void updateReserveAmmo(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            fetchReserveAmmo(player, modifiedGun);
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        updateHitMarker();

        if (++ammoAutoUpdateTimer >= ammoAutoUpdateRate) {
            ammoAutoUpdateTimer = 0;
        }

        if (isMeleeCooldownActive) {
            meleeCooldown--;
            if (meleeCooldown <= 0) {
                isMeleeCooldownActive = false;
                meleeCooldown = 0;
            }
        }
    }

    private static void updateHitMarker() {
        prevHitMarkerTime = hitMarkerTime;

        if (playingHitMarker) {
            hitMarkerTime++;
            if (hitMarkerTime > hitMarkerMaxTime) {
                playingHitMarker = false;
                hitMarkerTime = 0;
            }
        } else {
            hitMarkerTime = 0;
        }
    }

    public static boolean isRenderingHitMarker() {
        return playingHitMarker;
    }

    public static float getHitMarkerProgress(float partialTicks) {
        return (prevHitMarkerTime + (hitMarkerTime - prevHitMarkerTime) * partialTicks) / (float) hitMarkerMaxTime;
    }

    public static boolean getHitMarkerCrit() {
        return hitMarkerCrit;
    }












}
