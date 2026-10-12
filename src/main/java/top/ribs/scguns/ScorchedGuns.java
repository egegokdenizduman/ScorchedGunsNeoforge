package top.ribs.scguns;

import com.mojang.serialization.MapCodec;
import com.mrcrayfish.framework.api.FrameworkAPI;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.attributes.SCEntityAttributes;
import top.ribs.scguns.client.handler.*;
import top.ribs.scguns.client.screen.*;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.ProjectileManager;
import top.ribs.scguns.compat.CreateModCondition;
import top.ribs.scguns.compat.FarmersDelightModCondition;
import top.ribs.scguns.compat.IEModCondition;
import top.ribs.scguns.config.ProjectileAdvantageConfig;
import top.ribs.scguns.entity.projectile.*;
import top.ribs.scguns.entity.throwable.GrenadeEntity;
import top.ribs.scguns.event.*;
import top.ribs.scguns.init.ModBlockEntities;
import top.ribs.scguns.client.ClientHandler;
import top.ribs.scguns.init.*;
import top.ribs.scguns.network.PacketHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static top.ribs.scguns.Reference.MOD_ID;

@Mod(MOD_ID)
public class ScorchedGuns {
    public static final String MODID = "scguns";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, MOD_ID);
    private static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<CreateModCondition>> CREATE_MOD_LOADED_CONDITION =
            CONDITION_CODECS.register(CreateModCondition.ID, () -> CreateModCondition.CODEC);
    private static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<FarmersDelightModCondition>> FARMERS_DELIGHT_MOD_LOADED_CONDITION =
            CONDITION_CODECS.register(FarmersDelightModCondition.ID, () -> FarmersDelightModCondition.CODEC);
    private static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<IEModCondition>> IE_MOD_LOADED_CONDITION =
            CONDITION_CODECS.register(IEModCondition.ID, () -> IEModCondition.CODEC);
    public static boolean backpackedLoaded;
    public static boolean curiosLoaded;
    public static boolean controllableLoaded;
    public static boolean playerReviveLoaded;
    public static boolean createLoaded;
    public static boolean farmersDelightLoaded;
    public static boolean mekanismLoaded;
    public static boolean ieLoaded;
    public static boolean valkyrienSkiesLoaded;
    public static boolean sableLoaded;
    public static boolean marjLoaded;
    private static boolean useEnergyGuns = false;

    public ScorchedGuns(IEventBus modEventBus, ModContainer modContainer) {
        // Common setup
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
        Config.GunScalingConfig.setup(modContainer);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        IEventBus bus = modEventBus;
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);
        ModItems.REGISTER.register(bus);
        CONDITION_CODECS.register(bus);

        SCAttributes.ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(ModCapabilities::registerCapabilities);
        modEventBus.addListener(SCEntityAttributes::onEntityAttributeModification);

        initializeModDependencies();
        ModItems.registerItems();
        // Register other mod features
        ModCreativeModeTabs.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModBlocks.REGISTER.register(bus);
        ModContainers.REGISTER.register(bus);
        ModEffects.REGISTER.register(bus);
        ModEnchantments.REGISTER.register(bus);
        ModEntities.REGISTER.register(bus);
        ModParticleTypes.REGISTER.register(bus);
        ModSounds.REGISTER.register(bus);
        ModLootModifiers.LOOT_MODIFIERS.register(bus);

        bus.addListener(this::onCommonSetup);

        // Ensure client-specific code is run only on the client side
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHandler.registerClientHandlers(bus);
            NeoForge.EVENT_BUS.register(HUDRenderHandler.class);
            NeoForge.EVENT_BUS.register(InspectHandler.get());
            NeoForge.EVENT_BUS.register(BeamHandler.class);
        }

        //NeoForge.EVENT_BUS.register(ArmorBoostEventHandler.class);
       /// NeoForge.EVENT_BUS.register(ArmorRemoveEventHandler.class);
        NeoForge.EVENT_BUS.register(WeaponMovementEventHandler.class);
        NeoForge.EVENT_BUS.register(OceanWeaponEventHandler.class);
        NeoForge.EVENT_BUS.register(PiglinWeaponEventHandler.class);
        NeoForge.EVENT_BUS.register(ProjectileAdvantageConfig.class);


    }
    private void onConfigLoad(ModConfigEvent.Loading event) {
        handleConfigEvent(event.getConfig());
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        handleConfigEvent(event.getConfig());
    }

    private void handleConfigEvent(ModConfig config) {
        if (!MOD_ID.equals(config.getModId())) {
            return;
        }
        if (config.getType() == ModConfig.Type.SERVER) {
            // Only call RecoilHandler on client side
            if (FMLEnvironment.dist == Dist.CLIENT) {
                RecoilHandler.get().updateConfig();
            }
        }
    }
    private void initializeModDependencies() {
        // Check for optional mod dependencies
        valkyrienSkiesLoaded = ModList.get().isLoaded("valkyrienskies");
        sableLoaded = ModList.get().isLoaded("sable");
        controllableLoaded = ModList.get().isLoaded("controllable");
        backpackedLoaded = ModList.get().isLoaded("backpacked");
        curiosLoaded = ModList.get().isLoaded("curios");
        playerReviveLoaded = ModList.get().isLoaded("playerrevive");
        createLoaded = ModList.get().isLoaded("create");
        farmersDelightLoaded = ModList.get().isLoaded("farmersdelight");
        ieLoaded = ModList.get().isLoaded("immersiveengineering");
        mekanismLoaded = ModList.get().isLoaded("mekanism");
        marjLoaded = ModList.get().isLoaded("majruszsdifficulty");
    }

    public static boolean shouldUseEnergyGuns() {
        return !createLoaded || useEnergyGuns;
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.init();
            FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.AIMING);
            FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.RELOADING);
            FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.SHOOTING);
            FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.BURSTCOUNT);
            FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.ONBURSTCOOLDOWN);
            FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.MELEE);

            ProjectileManager.getInstance().registerFactory(ModItems.POWDER_AND_BALL.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.GRAPESHOT.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.COMPACT_COPPER_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.HOG_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new HogRoundProjectileEntity(ModEntities.HOG_ROUND_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.STANDARD_COPPER_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.COMPACT_ADVANCED_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.RAMROD_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new RamrodProjectileEntity(ModEntities.RAMROD_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.ADVANCED_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new AdvancedRoundProjectileEntity(ModEntities.ADVANCED_ROUND_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.SHATTER_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ShatterRoundProjectileEntity(ModEntities.SHATTER_ROUND_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.KRAHG_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new KrahgRoundProjectileEntity(ModEntities.KRAHG_ROUND_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.BEOWULF_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new BeowulfProjectileEntity(ModEntities.BEOWULF_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.GIBBS_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new GibbsRoundProjectileEntity(ModEntities.GIBBS_ROUND_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.SHOTGUN_SHELL.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.BEARPACK_SHELL.get(), (worldIn, entity, weapon, item, modifiedGun) -> new BearPackShellProjectileEntity(ModEntities.BEARPACK_SHELL_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.BLAZE_FUEL.get(), (worldIn, entity, weapon, item, modifiedGun) -> new FireRoundEntity(ModEntities.FIRE_ROUND_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.SCULK_CELL.get(), (worldIn, entity, weapon, item, modifiedGun) -> new SculkCellEntity(ModEntities.SCULK_CELL.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.SHOCK_CELL.get(), (worldIn, entity, weapon, item, modifiedGun) -> new LightningProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.FROG_DART.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.SHULKSHOT.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ShulkshotProjectileEntity(ModEntities.SHULKSHOT.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.ENERGY_CELL.get(), (worldIn, entity, weapon, item, modifiedGun) -> new PlasmaProjectileEntity(ModEntities.PLASMA_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.OSBORNE_SLUG.get(), (worldIn, entity, weapon, item, modifiedGun) -> new OsborneSlugProjectileEntity(ModEntities.OSBORNE_SLUG_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(Items.BLAZE_ROD, (worldIn, entity, weapon, item, modifiedGun) -> new BlazeRodProjectileEntity(ModEntities.BLAZE_ROD_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.SYRINGE.get(), (worldIn, entity, weapon, item, modifiedGun) -> new SyringeProjectileEntity(ModEntities.SYRINGE_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.STANDARD_BULLET.get(), (worldIn, entity, weapon, item, modifiedGun) -> new BasicBulletProjectileEntity(ModEntities.BASIC_BULLET_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.ADVANCED_BULLET.get(), (worldIn, entity, weapon, item, modifiedGun) -> new HardenedBulletProjectileEntity(ModEntities.HARDENED_BULLET_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.BUCKSHOT.get(), (worldIn, entity, weapon, item, modifiedGun) -> new BuckshotProjectileEntity(ModEntities.BUCKSHOT_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.NITRO_BUCKSHOT.get(), (worldIn, entity, weapon, item, modifiedGun) -> new BuckshotProjectileEntity(ModEntities.BUCKSHOT_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.SHOTBALL.get(), (worldIn, entity, weapon, item, modifiedGun) -> new ShotballProjectileEntity(ModEntities.SHOTBALL_PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.ROCKET.get(), (worldIn, entity, weapon, item, modifiedGun) -> new RocketEntity(ModEntities.ROCKET.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.HE_GRENADE_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new HeGrenadeRoundEntity(ModEntities.HE_GRENADE_ROUND.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.FIRE_GRENADE_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new FireGrenadeRoundEntity(ModEntities.FIRE_GRENADE_ROUND.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.GAS_GRENADE_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new GasGrenadeRoundEntity(ModEntities.GAS_GRENADE_ROUND.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.BOUNCY_GRENADE_ROUND.get(), (worldIn, entity, weapon, item, modifiedGun) -> new BouncyGrenadeRoundEntity(ModEntities.BOUNCY_GRENADE_ROUND.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.MICROJET.get(), (worldIn, entity, weapon, item, modifiedGun) -> new MicroJetEntity(ModEntities.MICROJET.get(), worldIn, entity, weapon, item, modifiedGun));
            ProjectileManager.getInstance().registerFactory(ModItems.GRENADE.get(), (worldIn, entity, weapon, item, modifiedGun) -> new GrenadeEntity(ModEntities.GRENADE.get(), worldIn, entity, weapon, item, modifiedGun));
            useEnergyGuns = Config.COMMON.gameplay.forceEnergyGuns.get();

          if (Config.COMMON.gameplay.improvedHitboxes.get()) {
                NeoForge.EVENT_BUS.register(new BoundingBoxManager());
            }
        });
    }
    public static boolean isDebugging() {
        return false;
    }
}



