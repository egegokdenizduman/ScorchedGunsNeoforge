package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import top.ribs.scguns.Reference;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Attachments;
import top.ribs.scguns.common.GunModifiers;
import top.ribs.scguns.item.*;
import top.ribs.scguns.item.ammo_boxes.*;

import top.ribs.scguns.item.animated.*;
import top.ribs.scguns.item.attachment.impl.Barrel;
import top.ribs.scguns.item.attachment.impl.Magazine;
import top.ribs.scguns.item.attachment.impl.Stock;
import top.ribs.scguns.item.attachment.impl.UnderBarrel;

import java.lang.reflect.Constructor;

public class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(Reference.MOD_ID);

    private static ResourceKey<JukeboxSong> jukeboxSong(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, name));
    }

    public static DeferredItem<GunItem> GALE;
    public static DeferredItem<GunItem> UMAX_PISTOL;
    public static DeferredItem<Item> VENTURI;
    public static DeferredItem<Item> SCRATCHES;

    public static void registerItems() {
        SCRATCHES = REGISTER.register("scratches", () -> {
            Item.Properties properties = new Item.Properties().stacksTo(1).durability(1400);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "scratches",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "scratches",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4900
                );
            }
        });
        GALE = REGISTER.register("gale", () -> {
            Item.Properties properties = new Item.Properties().stacksTo(1).durability(1400);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "gale",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "gale",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4900
                );
            }
        });
        UMAX_PISTOL = REGISTER.register("umax_pistol", () -> {
            Item.Properties properties = new Item.Properties().stacksTo(1).durability(400);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "umax_pistol",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "umax_pistol",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4900
                );
            }
        });


        VENTURI = REGISTER.register("venturi", () -> {
            Item.Properties properties = new Item.Properties().stacksTo(1).durability(800);
            if (!ScorchedGuns.shouldUseEnergyGuns()) {
                return new AnimatedAirGunItem(properties,
                        "venturi",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get()
                );
            } else {
                return new AnimatedEnergyGunItem(properties,
                        "venturi",
                        ModSounds.MAG_OUT.get(),
                        ModSounds.MAG_IN.get(),
                        ModSounds.RELOAD_END.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        ModSounds.COPPER_GUN_JAM.get(),
                        4900
                );
            }
        });
    }

    /**
     * Creates either an AirGunItem or EnergyGunItem based on whether Create is loaded
     * @param durability The durability for both gun types
     * @param energyCapacity The energy capacity (only used for EnergyGunItem)
     * @return The appropriate GunItem instance
     */
    private static GunItem createGunItem(int durability, int energyCapacity) {
        Item.Properties properties = new Item.Properties().stacksTo(1).durability(durability);

        if (!ScorchedGuns.shouldUseEnergyGuns()) {
            return new AirGunItem(properties);
        } else {
            return new EnergyGunItem(properties, energyCapacity);
        }
    }

    public static final DeferredItem<AnimatedGunItem> M3_CARABINE = REGISTER.register("m3_carabine",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "m3_carabine",                               // Model path
                    ModSounds.MAG_OUT.get(),        // Reload sound mag out
                    ModSounds.MAG_IN.get(),         // Reload sound mag in
                    ModSounds.RELOAD_END.get(),           // Reload sound end
                    ModSounds.COPPER_GUN_JAM.get(),      // Ejector sound pull
                    ModSounds.COPPER_GUN_JAM.get()    // Ejector sound release
            )
    );
    public static final DeferredItem<AnimatedGunItem> M3_MARKSMAN = REGISTER.register("m3_marksman",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "m3_marksman",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MAKESHIFT_RIFLE = REGISTER.register("makeshift_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "makeshift_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> DRILL = REGISTER.register("drill",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1200),
                    "drill",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> DRILL_CONVERSION = REGISTER.register("drill_conversion",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1200),
                    "drill_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> LOCKEWOOD = REGISTER.register("lockewood",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "lockewood",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> MICINA = REGISTER.register("micina",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "micina",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> RG_JIGSAW = REGISTER.register("rg_jigsaw",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "rg_jigsaw",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelAirGunItem> NAILER = REGISTER.register("nailer",
            () -> new AnimatedDiamondSteelAirGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "nailer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> MINKSY = REGISTER.register("minksy",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "minksy",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> MAS_PEDDLER = REGISTER.register("mas_peddler",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "mas_peddler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> INQUISITOR = REGISTER.register("inquisitor",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "inquisitor",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GRANDLE = REGISTER.register("grandle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "grandle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GRANDLE_OG = REGISTER.register("grandle_og",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "grandle_og",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> DEFENDER_PISTOL = REGISTER.register("defender_pistol",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "defender_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TRENCHUR = REGISTER.register("trenchur",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "trenchur",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> AUVTOMAG = REGISTER.register("auvtomag",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "auvtomag",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GREASER_SMG = REGISTER.register("greaser_smg",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "greaser_smg",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BOOMSTICK = REGISTER.register("boomstick",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "boomstick",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> INERTIAL = REGISTER.register("inertial",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "inertial",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> M22_WALTZ = REGISTER.register("m22_waltz",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "m22_waltz",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedUnderWaterGunItem> FLOUNDERGAT = REGISTER.register("floundergat",
            () -> new AnimatedUnderWaterGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "floundergat",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedUnderWaterGunItem> SPIRULIDA = REGISTER.register("spirulida",
            () -> new AnimatedUnderWaterGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "spirulida",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedUnderWaterGunItem> HYPERBARIA = REGISTER.register("hyperbaria",
            () -> new AnimatedUnderWaterGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "hyperbaria",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedUnderWaterGunItem> HULLBREAKER = REGISTER.register("hullbreaker",
            () -> new AnimatedUnderWaterGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "hullbreaker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.CANNON_RELOAD.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> KRAUSER = REGISTER.register("krauser",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "krauser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> UPPERCUT = REGISTER.register("uppercut",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "uppercut",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> PRUSH_GUN = REGISTER.register("prush_gun",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "prush_gun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> SOUL_DRUMMER = REGISTER.register("soul_drummer",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "soul_drummer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> VALORA = REGISTER.register("valora",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "valora",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> RUSTY_GNAT = REGISTER.register("rusty_gnat",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "rusty_gnat",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CALLWELL = REGISTER.register("callwell",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "callwell",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CALLWELL_TERMINAL = REGISTER.register("callwell_terminal",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "callwell_terminal",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CALLWELL_CONVERSION = REGISTER.register("callwell_conversion",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "callwell_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> COMBAT_SHOTGUN = REGISTER.register("combat_shotgun",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "combat_shotgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> FLINTLOCK_PISTOL = REGISTER.register("flintlock_pistol",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "flintlock_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> HANDCANNON = REGISTER.register("handcannon",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "handcannon",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> MUSKET = REGISTER.register("musket",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> REPEATING_MUSKET = REGISTER.register("repeating_musket",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "repeating_musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> LONGARM = REGISTER.register("longarm",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(200),
                    "longarm",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedAirGunItem> RED_RAYDAR = REGISTER.register("red_raydar",
            () -> new AnimatedAirGunItem(
                    new Item.Properties().stacksTo(1).durability(200),
                    "red_raydar",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> BLUNDERBUSS = REGISTER.register("blunderbuss",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "blunderbuss",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> FENCER_CARABINE = REGISTER.register("fencer_carabine",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(180),
                    "fencer_carabine",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> FENCER_THUMPER = REGISTER.register("fencer_thumper",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(180),
                    "fencer_thumper",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<NonUnderwaterAnimatedGunItem> DOUBLET = REGISTER.register("doublet",
            () -> new NonUnderwaterAnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "doublet",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> MAS_55 = REGISTER.register("mas_55",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "mas_55",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> COGLOADER = REGISTER.register("cogloader",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "cogloader",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SAKETINI = REGISTER.register("saketini",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "saketini",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SAKETINI_IRONPORT = REGISTER.register("saketini_ironport",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "saketini_ironport",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    SoundEvents.LEVER_CLICK,
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SCRAPPER = REGISTER.register("scrapper",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "scrapper",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BRAWLER = REGISTER.register("brawler",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "brawler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WINNIE = REGISTER.register("winnie",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "winnie",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WINNIE_MILLEND = REGISTER.register("winnie_millend",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "winnie_millend",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BRUISER = REGISTER.register("bruiser",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "bruiser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> CYCLONE = REGISTER.register("cyclone",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "cyclone",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> PLASGUN = REGISTER.register("plasgun",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "plasgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> ROCKET_RIFLE = REGISTER.register("rocket_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "rocket_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelUnderWaterGunItem> ZILK_45 = REGISTER.register("zilk_45",
            () -> new AnimatedDiamondSteelUnderWaterGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "zilk_45",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDiamondSteelGunItem> TRUANT = REGISTER.register("truant",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "truant",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TRIQUETRA = REGISTER.register("triquetra",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "triquetra",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MARLIN = REGISTER.register("marlin",
            () -> new AnimatedUnderWaterGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "marlin",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GAUSS_RIFLE = REGISTER.register("gauss_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "gauss_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> IRON_SPEAR = REGISTER.register("iron_spear",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "iron_spear",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> IRON_JAVELIN = REGISTER.register("iron_javelin",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "iron_javelin",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );

    public static final DeferredItem<AnimatedGunItem> LLR_DIRECTOR = REGISTER.register("llr_director",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "llr_director",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BIRDFEEDER = REGISTER.register("birdfeeder",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "birdfeeder",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TURNPIKE = REGISTER.register("turnpike",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "turnpike",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> STILETTO = REGISTER.register("stiletto",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "stiletto",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedAirGunItem> RAILWORKER = REGISTER.register("railworker",
            () -> new AnimatedAirGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "railworker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WHIZZBANGER = REGISTER.register("whizzbanger",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(15),
                    "whizzbanger",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BASKER = REGISTER.register("basker",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "basker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> KALASKAH = REGISTER.register("kalaskah",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "kalaskah",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MOKOVA = REGISTER.register("mokova",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "mokova",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MAK_MKII = REGISTER.register("mak_mkii",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "mak_mkii",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TL_RUNNER = REGISTER.register("tl_runner",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "tl_runner",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> KILLER_23 = REGISTER.register("killer_23",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "killer_23",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> HOMEMAKER = REGISTER.register("homemaker",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "homemaker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> RIBS_GLORY = REGISTER.register("ribs_glory",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "ribs_glory",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> STIGG = REGISTER.register("stigg",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(540),
                    "stigg",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> PAX = REGISTER.register("pax",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "pax",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BIG_BORE = REGISTER.register("big_bore",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(9),
                    "big_bore",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> HOWLER = REGISTER.register("howler",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "howler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> HOWLER_CONVERSION = REGISTER.register("howler_conversion",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "howler_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> PULSAR = REGISTER.register("pulsar",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "pulsar",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> ARC_WORKER = REGISTER.register("arc_worker",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "arc_worker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BLOOPER = REGISTER.register("blooper",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "blooper",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WHISTLER = REGISTER.register("whistler",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "whistler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> LASER_MUSKET = REGISTER.register("laser_musket",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(512),
                    "laser_musket",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> HAMMER_GL = REGISTER.register("hammer_gl",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "hammer_gl",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> LIBERTAS = REGISTER.register("libertas",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "libertas",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TESLOCK_RIFLE = REGISTER.register("teslock_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(512),
                    "teslock_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> KILN_GUN = REGISTER.register("kiln_gun",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(200),
                    "kiln_gun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> PLASMABUSS = REGISTER.register("plasmabuss",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(512),
                    "plasmabuss",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> JACKHAMMER = REGISTER.register("jackhammer",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "jackhammer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> JR_WRISTBREAKER = REGISTER.register("jr_wristbreaker",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "jr_wristbreaker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SEQUOIA = REGISTER.register("sequoia",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "sequoia",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> ULTRA_KNIGHT_HAWK = REGISTER.register("ultra_knight_hawk",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(15),
                    "ultra_knight_hawk",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SUPER_SHOTGUN = REGISTER.register("super_shotgun",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "super_shotgun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BOMB_LANCE = REGISTER.register("bomb_lance",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "bomb_lance",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> DOZIER_RL = REGISTER.register("dozier_rl",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(512),
                    "dozier_rl",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> DARK_MATTER = REGISTER.register("dark_matter",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1600),
                    "dark_matter",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MK43_RIFLE = REGISTER.register("mk43_rifle",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "mk43_rifle",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<CrusaderItem> CRUSADER = REGISTER.register("crusader",
            () -> new CrusaderItem(
                    new Item.Properties().stacksTo(1).durability(256),
                    "crusader",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GYROJET_PISTOL = REGISTER.register("gyrojet_pistol",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(800),
                    "gyrojet_pistol",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> THUNDERHEAD = REGISTER.register("thunderhead",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(600),
                    "thunderhead",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> GATTALER = REGISTER.register("gattaler",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1000),
                    "gattaler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CR4K_MINING_LASER = REGISTER.register("cr4k_mining_laser",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "cr4k_mining_laser",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SHARD_CULLER = REGISTER.register("shard_culler",
            () -> new AnimatedDiamondSteelGunItem(
                    new Item.Properties().stacksTo(1).durability(1350),
                    "shard_culler",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SPITFIRE = REGISTER.register("spitfire",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "spitfire",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WALTZ_CONVERSION = REGISTER.register("waltz_conversion",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "waltz_conversion",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> OSGOOD_50 = REGISTER.register("osgood_50",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1400),
                    "osgood_50",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> FREYR = REGISTER.register("freyr",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1050),
                    "freyr",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> VULCANIC_REPEATER = REGISTER.register("vulcanic_repeater",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1050),
                    "vulcanic_repeater",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> PYROCLASTIC_FLOW = REGISTER.register("pyroclastic_flow",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1050),
                    "pyroclastic_flow",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> MANGALITSA = REGISTER.register("mangalitsa",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1050),
                    "mangalitsa",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> TROTTERS = REGISTER.register("trotters",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1050),
                    "trotters",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> BLASPHEMY = REGISTER.register("blasphemy",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1050),
                    "blasphemy",
                    SoundEvents.GENERIC_DRINK,
                    SoundEvents.PLAYER_BURP,
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedSculkGunItem> WHISPERS = REGISTER.register("whispers",
            () -> new AnimatedSculkGunItem(
                    new Item.Properties().stacksTo(1).durability(1100),
                    "whispers",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedSculkGunItem> SCULK_RESONATOR = REGISTER.register("sculk_resonator",
            () -> new AnimatedSculkGunItem(
                    new Item.Properties().stacksTo(1).durability(1100),
                    "sculk_resonator",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedSculkGunItem> ECHOES_2 = REGISTER.register("echoes_2",
            () -> new AnimatedSculkGunItem(
                    new Item.Properties().stacksTo(1).durability(1100),
                    "echoes_2",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedSculkGunItem> FORLORN_HOPE = REGISTER.register("forlorn_hope",
            () -> new AnimatedSculkGunItem(
                    new Item.Properties().stacksTo(1).durability(1100),
                    "forlorn_hope",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> RAYGUN = REGISTER.register("raygun",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1600),
                    "raygun",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> CARAPICE = REGISTER.register("carapice",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1600),
                    "carapice",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> SHELLURKER = REGISTER.register("shellurker",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1600),
                    "shellurker",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> WEEVIL = REGISTER.register("weevil",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1600),
                    "weevil",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> LONE_WONDER = REGISTER.register("lone_wonder",
            () -> new AnimatedGunItem(
                    new Item.Properties().stacksTo(1).durability(1600),
                    "lone_wonder",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedDualWieldGunItem> RAT_KING_AND_QUEEN = REGISTER.register("rat_king_and_queen",
            () -> new AnimatedDualWieldGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "rat_king_and_queen",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> LOCUST = REGISTER.register("locust",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "locust",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> STERILIZER = REGISTER.register("sterilizer",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "sterilizer",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.HISS.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedGunItem> NEWBORN_CYST = REGISTER.register("newborn_cyst",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "newborn_cyst",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );

    public static final DeferredItem<AnimatedScorchedGunItem> ASTELLA = REGISTER.register("astella",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "astella",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> TERRA_INCOGNITA = REGISTER.register("terra_incognita",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "terra_incognita",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    SoundEvents.PISTON_EXTEND
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> PRIMA_MATERIA = REGISTER.register("prima_materia",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "prima_materia",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> NERVEPINCH = REGISTER.register("nervepinch",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "nervepinch",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> EARTHS_CORPSE = REGISTER.register("earths_corpse",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "earths_corpse",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );
    public static final DeferredItem<AnimatedScorchedGunItem> FLAYED_GOD = REGISTER.register("flayed_god",
            () -> new AnimatedScorchedGunItem(
                    new Item.Properties().stacksTo(1).durability(2400),
                    "flayed_god",
                    ModSounds.MAG_OUT.get(),
                    ModSounds.MAG_IN.get(),
                    ModSounds.RELOAD_END.get(),
                    ModSounds.COPPER_GUN_JAM.get(),
                    ModSounds.COPPER_GUN_JAM.get()
            )
    );










    public static final DeferredItem<Item> STANDARD_BULLET = REGISTER.register("standard_bullet", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ADVANCED_BULLET = REGISTER.register("hardened_bullet", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NITRO_POWDER = REGISTER.register("nitro_powder", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NITRO_POWDER_DUST = REGISTER.register("nitro_powder_dust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHEOL = REGISTER.register("sheol", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PEAL = REGISTER.register("peal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PEAL_DUST = REGISTER.register("peal_dust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> VEHEMENT_COAL = REGISTER.register("vehement_coal", () -> new FuelItem(new Item.Properties(), 4800));
    public static final DeferredItem<Item> SHEOL_DUST = REGISTER.register("sheol_dust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NITER_DUST = REGISTER.register("niter_dust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LIME_POWDER = REGISTER.register("lime_powder", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SULFUR_DUST = REGISTER.register("sulfur_dust", () -> new SulfurDustItem(new Item.Properties()));
    public static final DeferredItem<Item> PHOSPHOR_DUST = REGISTER.register("phosphor_dust", () -> new PhosphorItem(new Item.Properties()));
    public static final DeferredItem<Item> BUCKSHOT = REGISTER.register("buckshot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FLECHETTE = REGISTER.register("flechette", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NEEDLE = REGISTER.register("needle", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NITRO_BUCKSHOT = REGISTER.register("nitro_buckshot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GUNPOWDER_DUST = REGISTER.register("gunpowder_dust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAW_ANTHRALITE = REGISTER.register("raw_anthralite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CRUSHED_RAW_ANTHRALITE = REGISTER.register("crushed_raw_anthralite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ANTHRALITE_DUST = REGISTER.register("anthralite_dust", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CLUMP_ANTHRALITE = REGISTER.register("clump_anthralite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHARD_ANTHRALITE = REGISTER.register("shard_anthralite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DIRTY_DUST_ANTHRALITE = REGISTER.register("dirty_dust_anthralite", () -> new Item(new Item.Properties()));


    public static final DeferredItem<Item> ANTHRALITE_INGOT = REGISTER.register("anthralite_ingot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ANTHRALITE_NUGGET = REGISTER.register("anthralite_nugget", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TREATED_IRON_BLEND = REGISTER.register("treated_iron_blend", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TREATED_IRON_INGOT = REGISTER.register("treated_iron_ingot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TREATED_IRON_NUGGET = REGISTER.register("treated_iron_nugget", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> TREATED_BRASS_BLEND = REGISTER.register("treated_brass_blend", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TREATED_BRASS_INGOT = REGISTER.register("treated_brass_ingot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DIAMOND_STEEL_BLEND = REGISTER.register("diamond_steel_blend", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DEPLETED_DIAMOND_STEEL_INGOT = REGISTER.register("depleted_diamond_steel_ingot", () -> new DepletedDiamondSteelItem(new Item.Properties()));
    public static final DeferredItem<Item> DIAMOND_STEEL_INGOT = REGISTER.register("diamond_steel_ingot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SCORCHED_BLEND = REGISTER.register("scorched_blend", () -> new ScorchedItem(new Item.Properties()));
    public static final DeferredItem<Item> SCORCHED_INGOT = REGISTER.register("scorched_ingot", () -> new ScorchedItem(new Item.Properties()));
    public static final DeferredItem<Item> CHARGED_AMETHYST_SHARD = REGISTER.register("charged_amethyst_shard", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> EMPTY_TANK = REGISTER.register("empty_tank", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> EMPTY_CORE = REGISTER.register("empty_core", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ENERGY_CORE = REGISTER.register("energy_core", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DEPLETED_ENERGY_CORE = REGISTER.register("depleted_energy_core", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLASMA_CORE = REGISTER.register("plasma_core", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NETHER_STAR_FRAGMENT = REGISTER.register("nether_star_fragment", () -> new NetherStarFragmentItem(new Item.Properties()));
    public static final DeferredItem<Item> EMPTY_BLASPHEMY = REGISTER.register("empty_blasphemy", () -> new EmptyBlasphemyItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> GUN_GRIP = REGISTER.register("gun_grip", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GUN_BARREL = REGISTER.register("gun_barrel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> HEAVY_GUN_BARREL = REGISTER.register("heavy_gun_barrel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STONE_GUN_BARREL = REGISTER.register("stone_gun_barrel", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GUN_MAGAZINE = REGISTER.register("gun_magazine", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GUN_PARTS = REGISTER.register("gun_parts", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> HEAVY_GUN_PARTS = REGISTER.register("heavy_gun_parts", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FIRING_UNIT = REGISTER.register("firing_unit", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RAPID_FIRING_UNIT = REGISTER.register("rapid_firing_unit", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> COPPER_GUN_FRAME = REGISTER.register("copper_gun_frame", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SCORCHED_GUN_FRAME = REGISTER.register("scorched_gun_frame", () -> new ScorchedItem(new Item.Properties()));
    public static final DeferredItem<Item> TREATED_IRON_GUN_FRAME = REGISTER.register("treated_iron_gun_frame", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> IRON_GUN_FRAME = REGISTER.register("iron_gun_frame", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> TREATED_BRASS_GUN_FRAME = REGISTER.register("treated_brass_gun_frame", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DIAMOND_STEEL_GUN_FRAME = REGISTER.register("diamond_steel_gun_frame", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BLANK_MOLD = REGISTER.register("blank_mold", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SMALL_CASING_MOLD = REGISTER.register("small_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY), () -> ModItems.BROKEN_SMALL_CASING_MOLD.get()));
    public static final DeferredItem<Item> BROKEN_SMALL_CASING_MOLD = REGISTER.register("broken_small_casing_mold", () -> new BrokenMoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY).component(net.minecraft.core.component.DataComponents.DAMAGE, MoldItem.DURABILITY), () -> ModItems.SMALL_CASING_MOLD.get()));
    public static final DeferredItem<Item> MEDIUM_CASING_MOLD = REGISTER.register("medium_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY), () -> ModItems.BROKEN_MEDIUM_CASING_MOLD.get()));
    public static final DeferredItem<Item> BROKEN_MEDIUM_CASING_MOLD = REGISTER.register("broken_medium_casing_mold", () -> new BrokenMoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY).component(net.minecraft.core.component.DataComponents.DAMAGE, MoldItem.DURABILITY), () -> ModItems.MEDIUM_CASING_MOLD.get()));
    public static final DeferredItem<Item> LARGE_CASING_MOLD = REGISTER.register("large_casing_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY), () -> ModItems.BROKEN_LARGE_CASING_MOLD.get()));
    public static final DeferredItem<Item> BROKEN_LARGE_CASING_MOLD = REGISTER.register("broken_large_casing_mold", () -> new BrokenMoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY).component(net.minecraft.core.component.DataComponents.DAMAGE, MoldItem.DURABILITY), () -> ModItems.LARGE_CASING_MOLD.get()));
    public static final DeferredItem<Item> BULLET_MOLD = REGISTER.register("bullet_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY), () -> ModItems.BROKEN_BULLET_MOLD.get()));
    public static final DeferredItem<Item> BROKEN_BULLET_MOLD = REGISTER.register("broken_bullet_mold", () -> new BrokenMoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY).component(net.minecraft.core.component.DataComponents.DAMAGE, MoldItem.DURABILITY), () -> ModItems.BULLET_MOLD.get()));
    public static final DeferredItem<Item> DISC_MOLD = REGISTER.register("disc_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY), () -> ModItems.BROKEN_DISC_MOLD.get()));
    public static final DeferredItem<Item> BROKEN_DISC_MOLD = REGISTER.register("broken_disc_mold", () -> new BrokenMoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY).component(net.minecraft.core.component.DataComponents.DAMAGE, MoldItem.DURABILITY), () -> ModItems.DISC_MOLD.get()));
    public static final DeferredItem<Item> GUN_PARTS_MOLD = REGISTER.register("gun_parts_mold", () -> new MoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY), () -> ModItems.BROKEN_GUN_PARTS_MOLD.get()));
    public static final DeferredItem<Item> BROKEN_GUN_PARTS_MOLD = REGISTER.register("broken_gun_parts_mold", () -> new BrokenMoldItem(new Item.Properties().stacksTo(1).durability(MoldItem.DURABILITY).component(net.minecraft.core.component.DataComponents.DAMAGE, MoldItem.DURABILITY), () -> ModItems.GUN_PARTS_MOLD.get()));
    public static final DeferredItem<Item> COPPER_DISC = REGISTER.register("copper_disc", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SMALL_COPPER_CASING = REGISTER.register("small_copper_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MEDIUM_COPPER_CASING = REGISTER.register("medium_copper_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SMALL_IRON_CASING = REGISTER.register("small_iron_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LARGE_IRON_CASING = REGISTER.register("large_iron_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> EMPTY_CELL = REGISTER.register("empty_cell", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SHULKER_CASING = REGISTER.register("shulker_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SMALL_DIAMOND_STEEL_CASING = REGISTER.register("small_diamond_steel_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MEDIUM_DIAMOND_STEEL_CASING = REGISTER.register("medium_diamond_steel_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> SMALL_BRASS_CASING = REGISTER.register("small_brass_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MEDIUM_BRASS_CASING = REGISTER.register("medium_brass_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LARGE_BRASS_CASING = REGISTER.register("large_brass_casing", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POWDER_AND_BALL = REGISTER.register("powder_and_ball", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> GRAPESHOT = REGISTER.register("grapeshot", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> COMPACT_COPPER_ROUND = REGISTER.register("compact_copper_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> HOG_ROUND = REGISTER.register("hog_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> STANDARD_COPPER_ROUND = REGISTER.register("standard_copper_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> COMPACT_ADVANCED_ROUND = REGISTER.register("compact_advanced_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> RAMROD_ROUND = REGISTER.register("ramrod_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> ADVANCED_ROUND = REGISTER.register("advanced_round",
            () -> new TooltipAmmo(new Item.Properties(), 2));
    public static final DeferredItem<Item> SHATTER_ROUND = REGISTER.register("shatter_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> KRAHG_ROUND = REGISTER.register("krahg_round",
            () -> new TooltipAmmo(new Item.Properties(), 4));
    public static final DeferredItem<Item> BEOWULF_ROUND = REGISTER.register("beowulf_round",
            () -> new TooltipAmmo(new Item.Properties(), 2));
    public static final DeferredItem<Item> GIBBS_ROUND = REGISTER.register("gibbs_round",
            () -> new TooltipAmmo(new Item.Properties(), 2));
    public static final DeferredItem<Item> SHOTGUN_SHELL = REGISTER.register("shotgun_shell", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> BEARPACK_SHELL = REGISTER.register("bearpack_shell", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> BLAZE_FUEL = REGISTER.register("blaze_fuel",
            () -> new FuelAmmoItem(
                    new Item.Properties(),
                    3200,
                    ModItems.EMPTY_TANK,
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0),
                    new MobEffectInstance(MobEffects.WEAKNESS, 100, 0)
            ));

    public static final DeferredItem<Item> SHOTBALL = REGISTER.register("shotball", () -> new ThrowableShotballItem(new Item.Properties()));
    public static final DeferredItem<Item> FROG_DART = REGISTER.register("frog_dart", () -> new TooltipAmmo(new Item.Properties(), "tooltip.scguns.water"));
    public static final DeferredItem<Item> ENERGY_CELL = REGISTER.register("energy_cell", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> SCULK_CELL = REGISTER.register("sculk_cell",
            () -> new TooltipAmmo(new Item.Properties(), 6));
    public static final DeferredItem<Item> SHOCK_CELL = REGISTER.register("shock_cell",
            () -> new TooltipAmmo(new Item.Properties(), "tooltip.scguns.arcing"));
    public static final DeferredItem<Item> MICROJET = REGISTER.register("microjet", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> SHULKSHOT = REGISTER.register("shulkshot",
            () -> new TooltipAmmo(new Item.Properties(), "tooltip.scguns.homing"));

    public static final DeferredItem<Item> UNFINISHED_COMPACT_COPPER_ROUND = REGISTER.register("unfinished_compact_copper_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_HOG_ROUND = REGISTER.register("unfinished_hog_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_STANDARD_COPPER_ROUND = REGISTER.register("unfinished_standard_copper_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_COMPACT_ADVANCED_ROUND = REGISTER.register("unfinished_compact_advanced_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_RAMROD_ROUND = REGISTER.register("unfinished_ramrod_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_ADVANCED_ROUND = REGISTER.register("unfinished_advanced_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_SHATTER_ROUND = REGISTER.register("unfinished_shatter_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_KRAHG_ROUND = REGISTER.register("unfinished_krahg_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_BEOWULF_ROUND = REGISTER.register("unfinished_beowulf_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_GIBBS_ROUND = REGISTER.register("unfinished_gibbs_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_SHOTGUN_SHELL = REGISTER.register("unfinished_shotgun_shell", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_BEARPACK_SHELL = REGISTER.register("unfinished_bearpack_shell", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_ENERGY_CELL = REGISTER.register("unfinished_energy_cell", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_SCULK_CELL = REGISTER.register("unfinished_sculk_cell", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_MICROJET = REGISTER.register("unfinished_microjet", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_SHULKSHOT = REGISTER.register("unfinished_shulkshot", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_ROCKET = REGISTER.register("unfinished_rocket", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_GUN_PARTS = REGISTER.register("unfinished_gun_parts", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_HEAVY_GUN_PARTS = REGISTER.register("unfinished_heavy_gun_parts", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_PLASMA_CORE = REGISTER.register("unfinished_plasma_core", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_OSBORNE_SLUG = REGISTER.register("unfinished_osborne_slug", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_FROG_DART = REGISTER.register("unfinished_frog_dart", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_HE_GRENADE_ROUND = REGISTER.register("unfinished_he_grenade_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_GAS_GRENADE_ROUND = REGISTER.register("unfinished_gas_grenade_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_BOUNCY_GRENADE_ROUND = REGISTER.register("unfinished_bouncy_grenade_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> UNFINISHED_FIRE_GRENADE_ROUND = REGISTER.register("unfinished_fire_grenade_round", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ROCKET = REGISTER.register("rocket", () -> new AmmoItem(new Item.Properties().stacksTo(32)));
    public static final DeferredItem<Item> OSBORNE_SLUG = REGISTER.register("osborne_slug", () -> new AmmoItem(new Item.Properties().stacksTo(4)));
    public static final DeferredItem<Item> HE_GRENADE_ROUND = REGISTER.register("he_grenade_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> GAS_GRENADE_ROUND = REGISTER.register("gas_grenade_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> BOUNCY_GRENADE_ROUND = REGISTER.register("bouncy_grenade_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> FIRE_GRENADE_ROUND = REGISTER.register("fire_grenade_round", () -> new AmmoItem(new Item.Properties()));
    public static final DeferredItem<Item> PEBBLES = REGISTER.register("pebbles", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> NETHERITE_SCRAP_CHUNK = REGISTER.register("netherite_scrap_chunk", () -> new ScorchedItem(new Item.Properties()));

    public static final DeferredItem<Item> PLASMA = REGISTER.register("plasma", () -> new FuelItem(new Item.Properties(), 1800));
    public static final DeferredItem<Item> PLASMA_NUGGET = REGISTER.register("plasma_nugget", () -> new FuelItem(new Item.Properties(), 360));
    public static final DeferredItem<Item> PISTOL_AMMO_BOX = REGISTER.register("pistol_ammo_box", () -> new PistolAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> RIFLE_AMMO_BOX = REGISTER.register("rifle_ammo_box", () -> new RifleAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SHOTGUN_AMMO_BOX = REGISTER.register("shotgun_ammo_box", () -> new ShotgunAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> MAGNUM_AMMO_BOX = REGISTER.register("magnum_ammo_box", () -> new MagnumAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> ENERGY_AMMO_BOX = REGISTER.register("energy_ammo_box", () -> new EnergyAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> EMPTY_CASING_POUCH = REGISTER.register("empty_casing_pouch", () -> new EmptyCasingPouchItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> ROCKET_AMMO_BOX = REGISTER.register("rocket_ammo_box", () -> new RocketAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SPECIAL_AMMO_BOX = REGISTER.register("special_ammo_box", () -> new SpecialAmmoBoxItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CREATIVE_AMMO_BOX = REGISTER.register("creative_ammo_box", () -> new CreativeAmmoBoxItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));


    // Projectiles And Throwables
    public static final DeferredItem<Item> GRENADE = REGISTER.register("grenade", () -> new GrenadeItem(new Item.Properties().stacksTo(32), 20 * 3));
    public static final DeferredItem<Item> STUN_GRENADE = REGISTER.register("stun_grenade", () -> new StunGrenadeItem(new Item.Properties().stacksTo(32), 72000));
    public static final DeferredItem<Item> MOLOTOV_COCKTAIL = REGISTER.register("molotov_cocktail", () -> new MolotovCocktailItem(new Item.Properties().stacksTo(32), 72000));
    public static final DeferredItem<Item> SOUL_MOLOTOV_COCKTAIL = REGISTER.register("soul_molotov_cocktail", () -> new SoulMolotovCocktailItem(new Item.Properties().stacksTo(32), 72000));

    // Medical Items
    public static final DeferredItem<Item> BASIC_POULTICE = REGISTER.register("basic_poultice",
            () -> new HealingBandageItem(new Item.Properties().stacksTo(16), 4, (MobEffectInstance) null));

    public static final DeferredItem<Item> HONEY_SULFUR_POULTICE = REGISTER.register("honey_sulfur_poultice",
            () -> new HealingBandageItem(new Item.Properties().stacksTo(16), 8, new MobEffectInstance(MobEffects.REGENERATION, 100, 0)));

    public static final DeferredItem<Item> ENCHANTED_BANDAGE = REGISTER.register("enchanted_bandage", () -> new GlintedHealingBandageItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 12, new MobEffectInstance(MobEffects.REGENERATION, 100, 1), new MobEffectInstance(MobEffects.ABSORPTION, 400, 0)));

    public static final DeferredItem<Item> DRAGON_SALVE = REGISTER.register("dragon_salve", () -> new GlintedHealingBandageItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 16, new MobEffectInstance(MobEffects.REGENERATION, 200, 1), new MobEffectInstance(MobEffects.ABSORPTION, 700, 0)));


    // Scope Attachments
    public static final DeferredItem<Item> LASER_SIGHT = REGISTER.register("laser_sight", () -> new LaserSightItem(Attachments.LASER_SIGHT, new Item.Properties().stacksTo(1).durability(1300)));
    public static final DeferredItem<Item> LONG_SCOPE = REGISTER.register("long_scope", () -> new ScopeItem(Attachments.LONG_SCOPE, new Item.Properties().stacksTo(1).durability(1600)));
    public static final DeferredItem<Item> MEDIUM_SCOPE = REGISTER.register("medium_scope", () -> new ScopeItem(Attachments.MEDIUM_SCOPE, new Item.Properties().stacksTo(1).durability(1400)));
    public static final DeferredItem<Item> REFLEX_SIGHT = REGISTER.register("reflex_sight", () -> new ScopeItem(Attachments.REFLEX_SIGHT, new Item.Properties().stacksTo(1).durability(1300)));
    // Stock Attachments
    public static final DeferredItem<Item> LIGHT_STOCK = REGISTER.register("light_stock", () -> new StockItem(Stock.create(GunModifiers.LIGHT_STOCK_MODIFIER), new Item.Properties().stacksTo(1).durability(1350), false));
    public static final DeferredItem<Item> WEIGHTED_STOCK = REGISTER.register("weighted_stock", () -> new StockItem(Stock.create(GunModifiers.WEIGHTED_STOCK_MODIFIER), new Item.Properties().stacksTo(1).durability(1700)));
    public static final DeferredItem<Item> WOODEN_STOCK = REGISTER.register("wooden_stock", () -> new StockItem(Stock.create(GunModifiers.WOODEN_STOCK_MODIFIER), new Item.Properties().stacksTo(1).durability(1550), false));
    public static final DeferredItem<Item> BUMP_STOCK = REGISTER.register("bump_stock", () -> new StockItem(Stock.create(GunModifiers.BUMP_STOCK_MODIFIER), new Item.Properties().stacksTo(1).durability(1200), true));
    // Barrel Attachments
    public static final DeferredItem<Item> SILENCER = REGISTER.register("silencer", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.SILENCER_MODIFIER, GunModifiers.SILENCED, GunModifiers.REDUCED_DAMAGE), new Item.Properties().stacksTo(1).durability(550)));
    public static final DeferredItem<Item> ADVANCED_SILENCER = REGISTER.register("advanced_silencer", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.ADVANCED_SILENCER_MODIFIER, GunModifiers.SILENCED), new Item.Properties().stacksTo(1).durability(1200)));
    public static final DeferredItem<Item> MUZZLE_BRAKE = REGISTER.register("muzzle_brake", () -> new BarrelItem(Barrel.create(0.0F, GunModifiers.MUZZLE_BRAKE_MODIFIER), new Item.Properties().stacksTo(1).durability(1400)));
    public static final DeferredItem<Item> EXTENDED_BARREL = REGISTER.register("extended_barrel", () -> new ExtendedBarrelItem(Barrel.create(0.0F, GunModifiers.EXTENDED_BARREL_MODIFIER), new Item.Properties().stacksTo(1).durability(700)));

    // Under Barrel Attachments
    public static final DeferredItem<Item> LIGHT_GRIP = REGISTER.register("light_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.LIGHT_RECOIL), new Item.Properties().stacksTo(1).durability(1400)));
    public static final DeferredItem<Item> VERTICAL_GRIP = REGISTER.register("vertical_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.REDUCED_RECOIL), new Item.Properties().stacksTo(1).durability(1600)));


    //Magazines
    public static final DeferredItem<Item> EXTENDED_MAG = REGISTER.register("extended_mag", () -> new MagazineItem(Magazine.create(GunModifiers.EXTENDED_MAG_MODIFIER), new Item.Properties().stacksTo(1).durability(1700)));
    public static final DeferredItem<Item> SPEED_MAG = REGISTER.register("speed_mag", () -> new MagazineItem(Magazine.create(GunModifiers.SPEED_MAG_MODIFIER), new Item.Properties().stacksTo(1).durability(1550)));
    public static final DeferredItem<Item> PLUS_P_MAG = REGISTER.register("plus_p_mag", () -> new MagazineItem(Magazine.create(GunModifiers.INCREASED_DAMAGE, GunModifiers.PLUS_P_MAG), new Item.Properties().stacksTo(1).durability(1400)));
    //ITEMS
    // Mobs
    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }


}





