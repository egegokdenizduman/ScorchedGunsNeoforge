package top.ribs.scguns.client;

import com.mrcrayfish.framework.api.client.FrameworkClientAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.compat.OptionalClientCompat;
import top.ribs.scguns.client.handler.*;
import top.ribs.scguns.client.render.block.*;
import top.ribs.scguns.client.render.curios.AmmoBoxRenderer;
import top.ribs.scguns.client.render.gun.ModelOverrides;
import top.ribs.scguns.client.render.gun.model.*;
import top.ribs.scguns.client.screen.*;
import top.ribs.scguns.client.util.PropertyHelper;
import top.ribs.scguns.debug.IEditorMenu;
import top.ribs.scguns.debug.client.screen.EditorScreen;
import top.ribs.scguns.init.*;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.*;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

 /**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    public static void registerClientHandlers(IEventBus bus) {
        FrameworkClientAPI.registerDataLoader(MetaLoader.getInstance());
       // onRegisterCreativeTab(bus);
        bus.addListener(KeyBinds::registerKeyMappings);
        bus.addListener(CrosshairHandler::onConfigReload);
        bus.addListener(ClientHandler::onRegisterReloadListener);
        bus.addListener(ClientHandler::registerAdditional);
        bus.addListener(ClientHandler::registerMenuScreens);
        bus.addListener(ClientHandler::onClientSetup);
        bus.addListener(ClientHandler::onRegisterTooltipComponents);
        NeoForge.EVENT_BUS.register(HUDRenderHandler.class);
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && KeyBinds.KEY_MELEE.consumeClick()) {
            if (mc.player.getMainHandItem().getItem() instanceof GunItem) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageMeleeAttack());
            }
        }
    }
    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft.getInstance().getTextureManager().register(
                    ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png"),
                    new SimpleTexture(ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png"))
            );
        });
        BlockEntityRenderers.register(ModBlockEntities.GUN_SHELF_BLOCK_ENTITY.get(), GunShelfRenderer::new);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FAKE_SOUL_FIRE.get(), RenderType.cutout());

        registerAmmoCountProperty(ModItems.PISTOL_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.RIFLE_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.SHOTGUN_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.MAGNUM_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.ENERGY_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.ROCKET_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.SPECIAL_AMMO_BOX.get());
        registerAmmoCountProperty(ModItems.EMPTY_CASING_POUCH.get());
        registerAmmoCountProperty(ModItems.CREATIVE_AMMO_BOX.get());
        NeoForge.EVENT_BUS.register(new PlayerModelHandler());



        // Register the AmmoBoxRenderer for each ammo box item
        CuriosRendererRegistry.register(ModItems.PISTOL_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.RIFLE_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.SHOTGUN_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.MAGNUM_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.ENERGY_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.ROCKET_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.SPECIAL_AMMO_BOX.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.EMPTY_CASING_POUCH.get(), AmmoBoxRenderer::new);
        CuriosRendererRegistry.register(ModItems.CREATIVE_AMMO_BOX.get(), AmmoBoxRenderer::new);

        event.enqueueWork(ModMuzzleFlashes::init);
        event.enqueueWork(ClientHandler::setup);
        event.enqueueWork(OptionalClientCompat::registerEntityModelFeaturesVariables);
    }
    private ResourceLocation getFlashTexture(String flashType) {
        return ModMuzzleFlashes.getMuzzleFlashTexture(flashType);
    }
    public static void setup() {
        NeoForge.EVENT_BUS.register(AimingHandler.get());
        NeoForge.EVENT_BUS.register(BulletTrailRenderingHandler.get());
        NeoForge.EVENT_BUS.register(CrosshairHandler.get());
        NeoForge.EVENT_BUS.register(GunRenderingHandler.get());
        NeoForge.EVENT_BUS.register(RecoilHandler.get());
        NeoForge.EVENT_BUS.register(ReloadHandler.get());
        NeoForge.EVENT_BUS.register(ShootingHandler.get());
        NeoForge.EVENT_BUS.register(SoundHandler.get());
        NeoForge.EVENT_BUS.register(DistantGunSoundHandler.get());
        NeoForge.EVENT_BUS.register(new PlayerModelHandler());

        if (ScorchedGuns.controllableLoaded) {
            ControllerHandler.init();
            GunButtonBindings.register();
        }
        setupRenderLayers();
        registerModelOverrides();
    }

    private static void setupRenderLayers() {
        // Implement render layer setup here
    }
    private static void registerModelOverrides() {

        ModelOverrides.register(ModItems.EARTHS_CORPSE.get(), new EarthsCorpseModel());
        ModelOverrides.register(ModItems.FLAYED_GOD.get(), new FlayedGodModel());
        ModelOverrides.register(ModItems.NERVEPINCH.get(), new NervepinchModel());
        ModelOverrides.register(ModItems.RAT_KING_AND_QUEEN.get(), new RatKingAndQueenModel());
        ModelOverrides.register(ModItems.LOCUST.get(), new LocustModel());
        ModelOverrides.register(ModItems.NEWBORN_CYST.get(), new NewbornCystModel());
        ModelOverrides.register(ModItems.LONE_WONDER.get(), new LoneWonderModel());
        ModelOverrides.register(ModItems.CARAPICE.get(), new CarapiceModel());
        ModelOverrides.register(ModItems.SHELLURKER.get(), new ShellurkerModel());

        ModelOverrides.register(ModItems.ECHOES_2.get(), new Echoes2Model());
        ModelOverrides.register(ModItems.RAYGUN.get(), new RaygunModel());
        ModelOverrides.register(ModItems.SCULK_RESONATOR.get(), new SculkResonatorModel());
        ModelOverrides.register(ModItems.BLASPHEMY.get(), new BlasphemyModel());
        ModelOverrides.register(ModItems.WHISPERS.get(), new WhispersModel());
        ModelOverrides.register(ModItems.PYROCLASTIC_FLOW.get(), new PyroclasticFlowModel());
        ModelOverrides.register(ModItems.FREYR.get(), new FreyrModel());
        ModelOverrides.register(ModItems.VULCANIC_REPEATER.get(), new VulcanicRepeaterModel());
        ModelOverrides.register(ModItems.SCRATCHES.get(), new ScratchesModel());
        ModelOverrides.register(ModItems.OSGOOD_50.get(), new Osgood50Model());
        ModelOverrides.register(ModItems.GALE.get(), new GaleModel());
        ModelOverrides.register(ModItems.WALTZ_CONVERSION.get(), new WaltzConversionModel());
        ModelOverrides.register(ModItems.UMAX_PISTOL.get(), new UmaxPistolModel());
        ModelOverrides.register(ModItems.SPITFIRE.get(), new SpitfireModel());
        ModelOverrides.register(ModItems.SHARD_CULLER.get(), new ShardCullerModel());
        ModelOverrides.register(ModItems.GATTALER.get(), new GattalerModel());
        ModelOverrides.register(ModItems.CR4K_MINING_LASER.get(), new Cr4kMiningLaserModel());
        ModelOverrides.register(ModItems.THUNDERHEAD.get(), new ThunderheadModel());
        ModelOverrides.register(ModItems.GYROJET_PISTOL.get(), new GyrojetPistolModel());
        ModelOverrides.register(ModItems.DARK_MATTER.get(), new DarkMatterModel());
        ModelOverrides.register(ModItems.DOZIER_RL.get(), new DozierRLModel());
        ModelOverrides.register(ModItems.SUPER_SHOTGUN.get(), new SuperShotgunModel());
        ModelOverrides.register(ModItems.BOMB_LANCE.get(), new BombLanceModel());
        ModelOverrides.register(ModItems.VENTURI.get(), new VenturiModel());
        ModelOverrides.register(ModItems.MK43_RIFLE.get(), new Mk43RifleModel());
        ModelOverrides.register(ModItems.PLASGUN.get(), new PlasgunModel());
        ModelOverrides.register(ModItems.REPEATING_MUSKET.get(), new RepeatingMusketModel());
        ModelOverrides.register(ModItems.LONGARM.get(), new LongarmModel());
        ModelOverrides.register(ModItems.ULTRA_KNIGHT_HAWK.get(), new UltraKnightHawkModel());
        ModelOverrides.register(ModItems.SEQUOIA.get(), new SequoiaModel());
        ModelOverrides.register(ModItems.LASER_MUSKET.get(), new LaserMusketModel());
        ModelOverrides.register(ModItems.TESLOCK_RIFLE.get(), new TeslockRifleModel());
        ModelOverrides.register(ModItems.KILN_GUN.get(), new KilnGunModel());
        ModelOverrides.register(ModItems.PLASMABUSS.get(), new PlasmabussModel());
        ModelOverrides.register(ModItems.HAMMER_GL.get(), new HammerGlModel());
        ModelOverrides.register(ModItems.WHISTLER.get(), new WhistlerModel());
        ModelOverrides.register(ModItems.MINKSY.get(), new MinksyModel());
        ModelOverrides.register(ModItems.MAS_PEDDLER.get(), new MasPeddlerModel());
        ModelOverrides.register(ModItems.LIBERTAS.get(), new LibertasModel());
        ModelOverrides.register(ModItems.TRUANT.get(), new TruantModel());
        ModelOverrides.register(ModItems.ZILK_45.get(), new Zilk45Model());
        ModelOverrides.register(ModItems.JR_WRISTBREAKER.get(), new JrWristbreakerModel());
        ModelOverrides.register(ModItems.JACKHAMMER.get(), new JackhammerModel());
        ModelOverrides.register(ModItems.KILLER_23.get(), new Killer23Model());
        ModelOverrides.register(ModItems.HOMEMAKER.get(), new HomemakerModel());
        ModelOverrides.register(ModItems.RIBS_GLORY.get(), new RibsGloryModel());
        ModelOverrides.register(ModItems.PAX.get(), new PaxModel());
        ModelOverrides.register(ModItems.PULSAR.get(), new PulsarModel());
        ModelOverrides.register(ModItems.HOWLER.get(), new HowlerModel());
        ModelOverrides.register(ModItems.HOWLER_CONVERSION.get(), new HowlerConversionModel());
        ModelOverrides.register(ModItems.BIG_BORE.get(), new BigBoreModel());
        ModelOverrides.register(ModItems.ARC_WORKER.get(), new ArcWorkerModel());
        ModelOverrides.register(ModItems.BLOOPER.get(), new BlooperModel());
        ModelOverrides.register(ModItems.FLINTLOCK_PISTOL.get(), new FlintlockPistolModel());
        ModelOverrides.register(ModItems.HANDCANNON.get(), new HandcannonPistolModel());
        ModelOverrides.register(ModItems.MUSKET.get(), new MusketModel());
        ModelOverrides.register(ModItems.BLUNDERBUSS.get(), new BlunderbussModel());
        ModelOverrides.register(ModItems.FENCER_CARABINE.get(), new FencerCarabineModel());
        ModelOverrides.register(ModItems.FENCER_THUMPER.get(), new FencerThumperModel());
        ModelOverrides.register(ModItems.DOUBLET.get(), new DoubletModel());
        ModelOverrides.register(ModItems.ASTELLA.get(), new AstellaModel());
        ModelOverrides.register(ModItems.BRAWLER.get(), new BrawlerModel());
        ModelOverrides.register(ModItems.FLOUNDERGAT.get(), new FloundergatModel());
        ModelOverrides.register(ModItems.SPIRULIDA.get(), new SpirulidaModel());
        ModelOverrides.register(ModItems.HYPERBARIA.get(), new HyperbariaModel());
        ModelOverrides.register(ModItems.HULLBREAKER.get(), new HullbreakerModel());
        ModelOverrides.register(ModItems.SAKETINI.get(), new SaketiniModel());
        ModelOverrides.register(ModItems.SAKETINI_IRONPORT.get(), new SaketiniIronPortModel());
        ModelOverrides.register(ModItems.CALLWELL.get(), new CallwellModel());
        ModelOverrides.register(ModItems.WINNIE.get(), new WinnieModel());
        ModelOverrides.register(ModItems.WINNIE_MILLEND.get(), new WinnieMillendModel());
        ModelOverrides.register(ModItems.SCRAPPER.get(), new ScrapperModel());
        ModelOverrides.register(ModItems.MAKESHIFT_RIFLE.get(), new MakeshiftRifleModel());
        ModelOverrides.register(ModItems.BOOMSTICK.get(), new BoomstickModel());
        ModelOverrides.register(ModItems.RUSTY_GNAT.get(), new RustyGnatModel());
        ModelOverrides.register(ModItems.BRUISER.get(), new BruiserModel());
        ModelOverrides.register(ModItems.LLR_DIRECTOR.get(), new LlrDirectorModel());
        ModelOverrides.register(ModItems.BIRDFEEDER.get(), new BirdfeederModel());
        ModelOverrides.register(ModItems.TURNPIKE.get(), new TurnpikeModel());
        ModelOverrides.register(ModItems.RAILWORKER.get(), new RailworkerModel());
        ModelOverrides.register(ModItems.WHIZZBANGER.get(), new WhizzbangerModel());
        ModelOverrides.register(ModItems.BASKER.get(), new BaskerModel());
        ModelOverrides.register(ModItems.WEEVIL.get(), new WeevilModel());
        ModelOverrides.register(ModItems.TL_RUNNER.get(), new TlRunnerModel());
        ModelOverrides.register(ModItems.KALASKAH.get(), new KalaskahModel());
        ModelOverrides.register(ModItems.MOKOVA.get(), new MokovaModel());
        ModelOverrides.register(ModItems.MAK_MKII.get(), new MakMkIIModel());
        ModelOverrides.register(ModItems.STIGG.get(), new StiggModel());
        ModelOverrides.register(ModItems.TERRA_INCOGNITA.get(), new TerraIncognitaModel());
        ModelOverrides.register(ModItems.MARLIN.get(), new MarlinModel());
        ModelOverrides.register(ModItems.IRON_SPEAR.get(), new IronSpearModel());
        ModelOverrides.register(ModItems.IRON_JAVELIN.get(), new IronJavelinModel());
        ModelOverrides.register(ModItems.M3_CARABINE.get(), new M3CarabineModel());
        ModelOverrides.register(ModItems.M3_MARKSMAN.get(), new M3MarksmanModel());
        ModelOverrides.register(ModItems.STILETTO.get(), new StilettoModel());
        ModelOverrides.register(ModItems.DRILL.get(), new DrillModel());
        ModelOverrides.register(ModItems.DRILL_CONVERSION.get(), new DrillConversionModel());
        ModelOverrides.register(ModItems.LOCKEWOOD.get(), new LockewoodModel());
        ModelOverrides.register(ModItems.RG_JIGSAW.get(), new RgJigsawModel());
        ModelOverrides.register(ModItems.NAILER.get(), new NailerModel());
        ModelOverrides.register(ModItems.GREASER_SMG.get(), new GreaserSmgModel());
        ModelOverrides.register(ModItems.DEFENDER_PISTOL.get(), new DefenderPistolModel());
        ModelOverrides.register(ModItems.COMBAT_SHOTGUN.get(), new CombatShotgunModel());
        ModelOverrides.register(ModItems.AUVTOMAG.get(), new AuvtomagModel());
         ModelOverrides.register(ModItems.GAUSS_RIFLE.get(), new GaussRifleModel());
        ModelOverrides.register(ModItems.ROCKET_RIFLE.get(), new RocketRifleModel());
        ModelOverrides.register(ModItems.TRIQUETRA.get(), new TriquetraModel());
        ModelOverrides.register(ModItems.PRUSH_GUN.get(), new PrushGunModel());
        ModelOverrides.register(ModItems.INERTIAL.get(), new InertialModel());
        ModelOverrides.register(ModItems.INQUISITOR.get(), new InquisitorModel());
        ModelOverrides.register(ModItems.COGLOADER.get(), new CogloaderModel());
        ModelOverrides.register(ModItems.GRANDLE.get(), new GrandleModel());
        ModelOverrides.register(ModItems.GRANDLE_OG.get(), new GrandleOgModel());
        ModelOverrides.register(ModItems.UPPERCUT.get(), new UppercutModel());
        ModelOverrides.register(ModItems.MAS_55.get(), new Mas55Model());
         ModelOverrides.register(ModItems.CYCLONE.get(), new CycloneModel());
         ModelOverrides.register(ModItems.SOUL_DRUMMER.get(), new SoulDrummerModel());
        ModelOverrides.register(ModItems.VALORA.get(), new ValoraModel());
       ModelOverrides.register(ModItems.KRAUSER.get(), new KrauserModel());
        ModelOverrides.register(ModItems.M22_WALTZ.get(), new M22WaltzModel());
        ModelOverrides.register(ModItems.TRENCHUR.get(), new TrenchurModel());
        ModelOverrides.register(ModItems.MICINA.get(), new MicinaModel());
        ModelOverrides.register(ModItems.MANGALITSA.get(), new MangalitsaModel());
        ModelOverrides.register(ModItems.NIAMI.get(), new NiamiModel());
        ModelOverrides.register(ModItems.CRUSADER.get(), new CrusaderModel());
        ModelOverrides.register(ModItems.CALLWELL_CONVERSION.get(), new CallwellConversionModel());
        ModelOverrides.register(ModItems.CALLWELL_TERMINAL.get(), new CallwellTerminalModel());
        ModelOverrides.register(ModItems.FORLORN_HOPE.get(), new ForlornHopeModel());
        ModelOverrides.register(ModItems.PRIMA_MATERIA.get(), new PrimaMateriaModel());
        ModelOverrides.register(ModItems.RED_RAYDAR.get(), new RedRaydarModel());
        ModelOverrides.register(ModItems.STERILIZER.get(), new SterilizerModel());
        ModelOverrides.register(ModItems.TROTTERS.get(), new TrottersModel());


    }


    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModContainers.ATTACHMENTS.get(), AttachmentScreen::new);
    }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && event.getAction() == GLFW.GLFW_PRESS) {
            if (KeyBinds.KEY_ATTACHMENTS.isDown()) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
            }
        }
    }

    public static void onRegisterReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            PropertyHelper.resetCache();
            top.ribs.scguns.client.render.EmissiveTextures.clearCache();
        });
    }

    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        //event.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "special/test"));
    }


    public static Screen createEditorScreen(IEditorMenu menu) {
        return new EditorScreen(Minecraft.getInstance().screen, menu);
    }


    private static void onRegisterTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(top.ribs.scguns.item.ammo_boxes.AmmoBoxTooltip.class, top.ribs.scguns.client.screen.ClientAmmoBoxTooltip::new);
    }

    /**
     * Fill-level predicate for ammo box models: 0 = empty, 0.5 = partly filled, 1 = completely full.
     * Uses the same capacity maths as the item bar and tooltip.
     */
    private static void registerAmmoCountProperty(Item item) {
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("ammo_count"),
                (stack, world, entity, seed) -> {
                    if (AmmoBoxItem.getUsedUnits(stack) <= 0) {
                        return 0.0f;
                    }
                    return AmmoBoxItem.isFull(stack) ? 1.0f : 0.5f;
                });
    }
}
