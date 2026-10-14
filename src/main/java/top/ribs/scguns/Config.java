package top.ribs.scguns;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import top.ribs.scguns.client.SwayType;
import top.ribs.scguns.client.render.crosshair.DotRenderMode;
import top.ribs.scguns.client.screen.ButtonAlignment;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Config
{
    /**
     * Client related config options
     */
    public static class Client
    {
        public final Sounds sounds;
        public final Display display;
        public final Particle particle;
        public final Controls controls;
        public final Experimental experimental;
        public final ModConfigSpec.BooleanValue hideConfigButton;
        public final ModConfigSpec.EnumValue<ButtonAlignment> buttonAlignment;

        public Client(ModConfigSpec.Builder builder)
        {
            builder.push("client");
            {
                this.sounds = new Sounds(builder);
                this.display = new Display(builder);
                this.particle = new Particle(builder);
                this.controls = new Controls(builder);
                this.experimental = new Experimental(builder);
            }
            builder.pop();
            this.hideConfigButton = builder.comment("If enabled, hides the config button from the backpack screen").define("hideConfigButton", false);
            this.buttonAlignment = builder.comment("The alignment of the buttons in the backpack inventory screen").defineEnum("buttonAlignment", ButtonAlignment.RIGHT);
        }
    }

    /**
     * Sound related config options
     */
    public static class Sounds
    {
        public final ModConfigSpec.BooleanValue playSoundWhenHeadshot;
        public final ModConfigSpec.ConfigValue<String> headshotSound;
        public final ModConfigSpec.BooleanValue playSoundWhenCritical;
        public final ModConfigSpec.ConfigValue<String> criticalSound;
        public final ModConfigSpec.DoubleValue impactSoundDistance;

        public Sounds(ModConfigSpec.Builder builder)
        {
            builder.comment("Control sounds triggered by guns").push("sounds");
            {
                this.playSoundWhenHeadshot = builder.comment("If true, a sound will play when you successfully hit a headshot on a entity with a gun").define("playSoundWhenHeadshot", true);
                this.headshotSound = builder.comment("The sound to play when a headshot occurs").define("headshotSound", "minecraft:entity.player.attack.knockback");
                this.playSoundWhenCritical = builder.comment("If true, a sound will play when you successfully hit a critical on a entity with a gun").define("playSoundWhenCritical", true);
                this.criticalSound = builder.comment("The sound to play when a critical occurs").define("criticalSound", "minecraft:entity.player.attack.crit");
                this.impactSoundDistance = builder.comment("The maximum distance impact sounds from bullet can be heard").defineInRange("impactSoundDistance", 32.0, 0.0, 32.0);
            }
            builder.pop();
        }
    }

    /**
     * Display related config options
     */
    public static class Display
    {
        public final ModConfigSpec.BooleanValue oldAnimations;
        public final ModConfigSpec.ConfigValue<String> crosshair;
        public final ModConfigSpec.BooleanValue displayGunInfo;
        public final ModConfigSpec.BooleanValue immersiveGunInfo;
        public final ModConfigSpec.BooleanValue cinematicGunEffects;
        public final ModConfigSpec.BooleanValue cooldownIndicator;
        public final ModConfigSpec.BooleanValue weaponSway;
        public final ModConfigSpec.DoubleValue swaySensitivity;
        public final ModConfigSpec.EnumValue<SwayType> swayType;
        public final ModConfigSpec.BooleanValue cameraRollEffect;
        public final ModConfigSpec.DoubleValue cameraRollAngle;
        public final ModConfigSpec.BooleanValue restrictCameraRollToWeapons;
        public final ModConfigSpec.BooleanValue sprintAnimation;
        public final ModConfigSpec.DoubleValue bobbingIntensity;
        public final ModConfigSpec.BooleanValue fireLights;
        public final ModConfigSpec.BooleanValue puritySeals;
        public final ModConfigSpec.DoubleValue dynamicCrosshairBaseSpread;
        public final ModConfigSpec.DoubleValue dynamicCrosshairSpreadMultiplier;
        public final ModConfigSpec.DoubleValue dynamicCrosshairReactivity;
        public final ModConfigSpec.EnumValue<DotRenderMode> dynamicCrosshairDotMode;
        public final ModConfigSpec.BooleanValue onlyRenderDotWhileAiming;
        public final ModConfigSpec.DoubleValue dynamicCrosshairDotThreshold;
        public final ModConfigSpec.DoubleValue dynamicCrosshairMaxScale;
        public final ModConfigSpec.BooleanValue renderArms;



        public Display(ModConfigSpec.Builder builder)
        {
            builder.comment("Configuration for display related options").push("display");
            {
                this.oldAnimations = builder.comment("If true, uses the old animation poses for weapons. This is only for nostalgic reasons and not recommended to switch back.").define("oldAnimations", false);
                this.crosshair = builder.comment("The custom crosshair to use for weapons. Go to (Options > Controls > Mouse Settings > Crosshair) in game to change this!").define("crosshair", "scguns:dynamic");
                this.cooldownIndicator = builder.comment("If enabled, renders a cooldown indicator to make it easier to learn when you fire again.").define("cooldownIndicator", true);
                this.weaponSway = builder.comment("If enabled, the weapon will sway when the player moves their look direction. This does not affect aiming and is only visual.").define("weaponSway", true);
                this.swaySensitivity = builder.comment("The sensistivity of the visual weapon sway when the player moves their look direciton. The higher the value the more sway.").defineInRange("swaySensitivity", 0.3, 0.0, 1.0);
                this.swayType = builder.comment("The animation to use for sway. Directional follows the camera better while Drag is more immersive").defineEnum("swayType", SwayType.DRAG);
                this.cameraRollEffect = builder.comment("If enabled, the camera will roll when strafing while holding a gun. This creates a more immersive feeling.").define("cameraRollEffect", true);
                this.cameraRollAngle = builder.comment("When Camera Roll Effect is enabled, this is the absolute maximum angle the roll on the camera can approach.").defineInRange("cameraRollAngle", 1.5F, 0F, 45F);
                this.restrictCameraRollToWeapons = builder.comment("When enabled, the Camera Roll Effect is only applied when holding a weapon.").define("restrictCameraRollToWeapons", true);
                this.sprintAnimation = builder.comment("Enables the sprinting animation on weapons for better immersion. This only applies to weapons that support a sprinting animation.").define("sprintingAnimation", true);
                this.displayGunInfo = builder.comment("If enabled, renders a HUD element displaying the gun's ammo count and ammo capacity, as well as pulse weapon charge.").define("displayGunInfo", true);
                this.immersiveGunInfo = builder.comment("If enabled, the HUD will display when inspecting the gun.").define("immersiveGunInfo", false);
                this.bobbingIntensity = builder.comment("The intensity of the custom bobbing animation while holding a gun").defineInRange("bobbingIntensity", 1.0, 0.0, 2.0);
                this.cinematicGunEffects = builder.comment("If enabled, enables cinematic camera effects on guns ").define("cinematicGunEffects", true);
                this.fireLights = builder.comment("If enabled, enables light sources when firing guns").define("fireLights", true);
                this.puritySeals = builder.comment("If enabled, enables purity seals on weapons").define("puritySeals", true);
                this.dynamicCrosshairBaseSpread = builder.comment("The resting size of the Dynamic Crosshair when spread is zero.").defineInRange("dynamicCrosshairBaseSpread", 1.0, 0.0, 5.0);
                this.dynamicCrosshairSpreadMultiplier = builder.comment("The bloom factor of the Dynamic Crosshair when spread increases.").defineInRange("dynamicCrosshairSpreadMultiplier", 1.0, 1.0, 1.5);
                this.dynamicCrosshairReactivity = builder.comment("How reactive the Dynamic Crosshair is to shooting.").defineInRange("dynamicCrosshairReactivity", 2.0, 0.0, 10.0);
                this.dynamicCrosshairDotMode = builder.comment("The rendering mode used for the Dynamic Crosshair's center dot. At Min Spread will only render the dot when gun spread is stable.").defineEnum("dynamicCrosshairDotMode", DotRenderMode.AT_MIN_SPREAD);
                this.onlyRenderDotWhileAiming = builder.comment("If true, the Dynamic Crosshair's center dot will only render while aiming. Obeys dynamicCrosshairDotMode, and has no effect when mode is set to Never.").define("onlyRenderDotWhileAiming", true);
                this.dynamicCrosshairDotThreshold = builder.comment("The threshold of spread (including modifiers) below which the Dynamic Crosshair's center dot is rendered. Affects the At Min Spread and Threshold modes only.").defineInRange("dynamicCrosshairDotThreshold", 0.8, 0.0, 90.0);
                this.dynamicCrosshairMaxScale = builder
                        .comment("The maximum scale factor for the dynamic crosshair when spread is high")
                        .defineInRange("dynamicCrosshairMaxScale", 8.0, 1.0, 20.0);
                this.renderArms = builder.comment("If true, renders the player's arms when holding a gun").define("renderArms", true);
            }
            builder.pop();
        }
    }

    /**
     * Particle related config options
     */
    public static class Particle
    {
        public final ModConfigSpec.IntValue bulletHoleLifeMin;
        public final ModConfigSpec.IntValue bulletHoleLifeMax;
        public final ModConfigSpec.DoubleValue bulletHoleFadeThreshold;
        public final ModConfigSpec.BooleanValue enableBlood;
        public final ModConfigSpec.DoubleValue impactParticleDistance;
        public final ModConfigSpec.BooleanValue enableWaterImpactParticles;
        public final ModConfigSpec.BooleanValue enableLavaImpactParticles;

        public Particle(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to particles").push("particle");
            {
                this.bulletHoleLifeMin = builder.comment("The minimum duration in ticks before bullet holes will disappear").defineInRange("bulletHoleLifeMin", 150, 0, Integer.MAX_VALUE);
                this.bulletHoleLifeMax = builder.comment("The maximum duration in ticks before bullet holes will disappear").defineInRange("bulletHoleLifeMax", 200, 0, Integer.MAX_VALUE);
                this.bulletHoleFadeThreshold = builder.comment("The percentage of the maximum life that must pass before particles begin fading away. 0 makes the particles always fade and 1 removes facing completely").defineInRange("bulletHoleFadeThreshold", 0.98, 0, 1.0);
                this.enableBlood = builder.comment("If true, blood will will spawn from entities that are hit from a projectile").define("enableBlood", true);
                this.impactParticleDistance = builder.comment("The maximum distance impact particles can be seen from the player").defineInRange("impactParticleDistance", 32.0, 0.0, 64.0);
                this.enableWaterImpactParticles = builder.comment("If true, particles will spawn when projectiles impact water").define("enableWaterImpactParticles", true);
                this.enableLavaImpactParticles = builder.comment("If true, particles will spawn when projectiles impact lava").define("enableLavaImpactParticles", true);
            }
            builder.pop();
        }
    }

    public static class Controls
    {
        public final ModConfigSpec.DoubleValue aimDownSightSensitivity;
        public final ModConfigSpec.BooleanValue flipControls;

        public Controls(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to controls").push("controls");
            {
                this.aimDownSightSensitivity = builder.comment("A value to multiply the mouse sensitivity by when aiming down weapon sights.").defineInRange("aimDownSightSensitivity", 0.75, 0.0, 1.0);
                this.flipControls = builder.comment("When enabled, switches the shoot and aim controls of weapons. Due to technical reasons, you won't be able to use offhand items if you enable this setting.").define("flipControls", false);
            }
            builder.pop();
        }
    }

    public static class Experimental
    {
        public Experimental(ModConfigSpec.Builder builder)
        {
            builder.comment("Experimental options").push("experimental");
            {
            }
            builder.pop();
        }
    }

    /**
     * Common config options
     */    public static class Common
    {
        public final Gameplay gameplay;
        public final Network network;
        public final AggroMobs aggroMobs;
        public final FleeingMobs fleeingMobs;
        public final Rockets rockets;
        public final Grenades grenades;
        public final StunGrenades stunGrenades;
        public final ProjectileSpread projectileSpread;


        public Common(ModConfigSpec.Builder builder)
        {
            builder.push("common");
            {
                this.gameplay = new Gameplay(builder);
                this.network = new Network(builder);
                this.aggroMobs = new AggroMobs(builder);
                this.fleeingMobs = new FleeingMobs(builder);
                this.rockets = new Rockets(builder);
                this.grenades = new Grenades(builder);
                this.stunGrenades = new StunGrenades(builder);
                this.projectileSpread = new ProjectileSpread(builder);
            }
            builder.pop();
        }
    }

    /**
     * Gameplay related config options
     */
    public static class Gameplay
    {
        public final Griefing griefing;
        public final Shotball shotball;
        public final ModConfigSpec.BooleanValue enableFirePlacement;
        public final ModConfigSpec.BooleanValue enableGunDamage;
        public final ModConfigSpec.BooleanValue enableAttachmentDamage;
        public  final ModConfigSpec.BooleanValue spawnCasings;
        public final ModConfigSpec.DoubleValue growBoundingBoxAmount;
        public final ModConfigSpec.BooleanValue enableHeadShots;
        public final ModConfigSpec.DoubleValue headShotDamageMultiplier;
        public final ModConfigSpec.DoubleValue criticalDamageMultiplier;
        public final ModConfigSpec.BooleanValue ignoreLeaves;
        public final ModConfigSpec.BooleanValue enableKnockback;
        public final ModConfigSpec.DoubleValue knockbackStrength;
        public final ModConfigSpec.BooleanValue improvedHitboxes;
        public final ModConfigSpec.DoubleValue enemyBulletDamage;
        public final ModConfigSpec.IntValue ammoBoxStackCapacity;
        public final ModConfigSpec.IntValue energyProductionRate;
        public final ModConfigSpec.BooleanValue drawAnimation;
        public final ModConfigSpec.BooleanValue forceEnergyGuns;
        public final ModConfigSpec.BooleanValue toggleADS;
        public final ModConfigSpec.DoubleValue globalDamageMultiplier;
        public final ModConfigSpec.BooleanValue disableVillagerSpawning;
        public final ModConfigSpec.DoubleValue dissidentSpawnChance;
        public final ModConfigSpec.BooleanValue enableAutoReload;
        public final ModConfigSpec.DoubleValue mobFireRateMultiplier;
        public final ModConfigSpec.DoubleValue mobBurstDelayMultiplier;
        public final ModConfigSpec.DoubleValue mobGunDamageMultiplier;
        public Gameplay(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to gameplay").push("gameplay");
            {
                this.griefing = new Griefing(builder);
                this.shotball = new Shotball(builder);
                this.drawAnimation = builder.comment("If true, enables the draw animation for weapons").define("drawAnimation", true);
                this.ammoBoxStackCapacity = builder.comment("How many stacks every ammo box and the empty casing box hold. Boxes are Create packages, which have 9 slots, so 9 is the maximum.").defineInRange("ammoBoxStackCapacity", 9, 1, 9);
                this.enableGunDamage = builder.comment("If true, guns will be damageable and can break, coward if you toggle this").define("enableGunDamage", true);
                this.enableAttachmentDamage = builder.comment("If true, gun attachments will be damageable and can break, also a coward").define("enableAttachmentDamage", true);
                this.spawnCasings = builder.comment("Set to false to disable the spawning of casing items when firing guns.").define("gameplay.spawnCasings", true);
                this.growBoundingBoxAmount = builder.comment("The extra amount to expand an entity's bounding box when checking for projectile collision. Setting this value higher will make it easier to hit entities").defineInRange("growBoundingBoxAmount", 0.3, 0.0, 1.0);
                this.enableHeadShots = builder.comment("Enables the check for head shots for players. Projectiles that hit the head of a player will have increased damage.").define("enableHeadShots", true);
                this.headShotDamageMultiplier = builder.comment("The value to multiply the damage by if projectile hit the players head").defineInRange("headShotDamageMultiplier", 1.25, 1.0, Double.MAX_VALUE);
                this.criticalDamageMultiplier = builder.comment("The value to multiply the damage by if projectile is a critical hit").defineInRange("criticalDamageMultiplier", 1.5, 1.0, Double.MAX_VALUE);
                this.ignoreLeaves = builder.comment("If true, projectiles will ignore leaves when checking for collision").define("ignoreLeaves", true);
                this.enableKnockback = builder.comment("If true, projectiles will cause knockback when an entity is hit. By default this is set to true to match the behaviour of Minecraft.").define("enableKnockback", true);
                this.knockbackStrength = builder.comment("Sets the strength of knockback when shot by a bullet projectile. Knockback must be enabled for this to take effect. If value is equal to zero, knockback will use default minecraft value").defineInRange("knockbackStrength", 0.15, 0.0, 1.0);
                this.improvedHitboxes = builder.comment("If true, improves the accuracy of weapons by considering the ping of the player. This has no affect on singleplayer. This will add a little overhead if enabled.").define("improvedHitboxes", false);
                this.enemyBulletDamage = builder.comment("Damage dealt by the Enemy Guns")
                        .defineInRange("enemyBulletDamage", 4.5, 0.0, Double.MAX_VALUE);
                this.energyProductionRate = builder
                        .comment("Energy produced per tick by the Polar Generator. Adjust this value to balance the generator's output.")
                        .defineInRange("energyProductionRate", 50, 1, Integer.MAX_VALUE);
                this.forceEnergyGuns = builder
                        .comment("If true, guns will always use energy system even if Create mod is loaded NOT WORKING.")
                        .define("forceEnergyGuns", false);
                this.toggleADS = builder
                        .comment("If true, guns will toggle ADS mode.")
                        .define("toggleADS", false);
                this.globalDamageMultiplier = builder
                        .comment("Global multiplier for all gun damage. 1.0 = normal damage, 0.5 = half damage, 2.0 = double damage. Affects all projectile damage from guns.")
                        .defineInRange("globalDamageMultiplier", 1.0, 0.01, 10.0);
                this.disableVillagerSpawning = builder
                        .comment("If true, the Brass Mask ritual will never spawn villagers. When disabled, it will spawn Dissidents instead based on the spawn chance below.")
                        .define("disableVillagerSpawning", false);
                this.dissidentSpawnChance = builder
                        .comment("When villager spawning is disabled, this is the chance (0.0 to 1.0) that a Dissident will spawn. If a Dissident doesn't spawn, nothing will be created from the ritual.")
                        .defineInRange("dissidentSpawnChance", 0.1, 0.0, 1.0);
                this.enableAutoReload = builder
                        .comment("If true, guns will automatically start reloading when fired with an empty magazine if ammo is available")
                        .define("enableAutoReload", true);
                this.mobFireRateMultiplier = builder
                        .comment("Global multiplier for mob fire rate. 1.0 = normal speed, 0.5 = faster, 2.0 = slower.")
                        .defineInRange("mobFireRateMultiplier", 1.0, 0.1, 5.0);
                this.mobBurstDelayMultiplier = builder
                        .comment("Multiplier for delay between mob gun bursts. Higher values add longer pauses between bursts.")
                        .defineInRange("mobBurstDelayMultiplier", 1.0, 0.1, 5.0);
                this.mobGunDamageMultiplier = builder
                        .comment("Global multiplier for mob gun damage after difficulty scaling.")
                        .defineInRange("mobGunDamageMultiplier", 1.0, 0.01, 100.0);
                this.enableFirePlacement = builder.comment("If true, allows flamethrowers to place fire on blocks").define("enableFirePlacement", true);
            }
            builder.pop();
        }
    }

    /**
     * Gun griefing related config options
     */
    public static class Griefing
    {
        public final ModConfigSpec.BooleanValue enableBlockRemovalOnExplosions;
        public final ModConfigSpec.BooleanValue enableGlassBreaking;
        public final ModConfigSpec.BooleanValue fragileBlockDrops;
        public final ModConfigSpec.DoubleValue fragileBaseBreakChance;
        public final ModConfigSpec.BooleanValue setFireToBlocks;
        public final ModConfigSpec.BooleanValue enableBlockBreaking;
        public final ModConfigSpec.BooleanValue enableBeamMining;

        public Griefing(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties related to gun griefing").push("griefing");
            {
                this.enableBlockRemovalOnExplosions = builder.comment("If enabled, allows block removal on explosions").define("enableBlockRemovalOnExplosions", true);
                this.enableGlassBreaking = builder.comment("If enabled, allows guns to shoot out glass and other fragile objects").define("enableGlassBreaking", true);
                this.fragileBlockDrops = builder.comment("If enabled, fragile blocks will drop their loot when broken").define("fragileBlockDrops", true);
                this.fragileBaseBreakChance = builder.comment("The base chance that a fragile block is broken when impacted by a bullet. The hardness of a block will scale this value; the harder the block, the lower the final calculated chance will be.").defineInRange("fragileBlockBreakChance", 1.0, 0.0, 1.0);
                this.setFireToBlocks = builder.comment("If true, allows guns enchanted with Fire Starter to light and spread fires on blocks").define("setFireToBlocks", true);
                this.enableBlockBreaking = builder.comment("If true, allows heavy rounds to break blocks").define("enableBlockBreaking", true);
                this.enableBeamMining = builder.comment("If true, allows beam weapons to mine blocks").define("enableBeamMining", true);
            }
            builder.pop();
        }
    }

    /**
     * Hullbreaker shotball related config options
     */
    public static class Shotball
    {
        public final ModConfigSpec.IntValue woodPenetration;
        public final ModConfigSpec.DoubleValue explosionRadius;
        public final ModConfigSpec.DoubleValue explosionDamageFraction;
        public final ModConfigSpec.DoubleValue subLevelPenetrationMultiplier;
        public final ModConfigSpec.DoubleValue subLevelExplosionMultiplier;
        public final ModConfigSpec.DoubleValue subLevelImpulse;

        public Shotball(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties of the Hullbreaker shotball").push("shotball");
            {
                this.woodPenetration = builder.comment("How many blocks from the scguns:shotball_penetrable tag (wood, wool, envelopes) a shotball smashes through before it detonates. Needs griefing.enableBlockBreaking.").defineInRange("woodPenetration", 3, 0, 32);
                this.explosionRadius = builder.comment("Radius of the small explosion when a shotball detonates on a block.").defineInRange("explosionRadius", 1.5, 0.0, 8.0);
                this.explosionDamageFraction = builder.comment("Explosion damage to nearby entities, as a fraction of the shotball's projectile damage.").defineInRange("explosionDamageFraction", 0.35, 0.0, 2.0);
                this.subLevelPenetrationMultiplier = builder.comment("Sable / Create Aeronautics ships (sub-levels): multiplier on how many penetrable blocks a shotball smashes through.").defineInRange("subLevelPenetrationMultiplier", 2.0, 0.0, 16.0);
                this.subLevelExplosionMultiplier = builder.comment("Sable / Create Aeronautics ships (sub-levels): multiplier on the detonation radius when a shotball explodes against a ship.").defineInRange("subLevelExplosionMultiplier", 1.5, 0.0, 8.0);
                this.subLevelImpulse = builder.comment("Sable / Create Aeronautics ships (sub-levels): physics impulse pushed into the ship along the shot direction when a shotball detonates against it (each smashed ship block adds a quarter of this). For scale, Sable's own explosions push 5 per blasted block.").defineInRange("subLevelImpulse", 60.0, 0.0, 10000.0);
            }
            builder.pop();
        }
    }

    /**
     * Network related config options
     */
    public static class Network
    {
        public final ModConfigSpec.DoubleValue projectileTrackingRange;

        public Network(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to network").push("network");
            {
                this.projectileTrackingRange = builder.comment("The distance players need to be within to be able to track new projectiles trails. Higher values means you can see projectiles from that start from further away.").defineInRange("projectileTrackingRange", 200.0, 1, Double.MAX_VALUE);
            }
            builder.pop();
        }
    }

    /**
     * Mob aggression related config options
     */
    public static class AggroMobs {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.DoubleValue unsilencedRange;
        public final ModConfigSpec.DoubleValue aggroChance;
        public final ModConfigSpec.DoubleValue chainAggroChance;
        public final ModConfigSpec.DoubleValue chainAggroRadius;
        public final ModConfigSpec.ConfigValue<List<? extends String>> exemptEntities;

        public AggroMobs(ModConfigSpec.Builder builder) {
            builder.comment("Properties relating to mob aggression from gunfire").push("aggro_mobs");
            {
                this.enabled = builder.comment("If true, hostile mobs may become aggressive when guns are fired nearby.").define("enabled", true);
                this.unsilencedRange = builder.comment("Range in which hostile mobs may detect and react to unsilenced gunfire.").defineInRange("unsilencedRange", 20.0, 0.0, Double.MAX_VALUE);
                this.aggroChance = builder.comment("Chance (0.0 to 1.0) that a hostile mob will target the shooter when detecting gunfire.").defineInRange("aggroChance", 0.25, 0.0, 1.0);
                this.chainAggroChance = builder.comment("Chance (0.0 to 1.0) that nearby mobs of the same type will also aggro when one becomes hostile.").defineInRange("chainAggroChance", 0.5, 0.0, 1.0);
                this.chainAggroRadius = builder.comment("Radius in which chain aggro effects can spread to similar mobs.").defineInRange("chainAggroRadius", 8.0, 0.0, 32.0);
                this.exemptEntities = builder.comment("LEGACY: Entities that will not aggro from gunfire. Use the 'scguns:aggro_from_guns' entity tag for better mod compatibility.").defineList("exemptMobs", Collections.emptyList(), o -> true);
            }
            builder.pop();
        }
    }
    public static class FleeingMobs {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.DoubleValue silencedRange;
        public final ModConfigSpec.DoubleValue unsilencedRange;
        public final ModConfigSpec.ConfigValue<List<? extends String>> fleeingEntities;

        public FleeingMobs(ModConfigSpec.Builder builder) {
            builder.comment("Properties relating to mob fleeing").push("fleeing_mobs");
            {
                this.enabled = builder.comment("If true, nearby mobs will flee from the firing of guns.").define("enabled", true);
                this.unsilencedRange = builder.comment("Any mobs within a sphere of this radius will flee from the shooter of an unsilenced gun.").defineInRange("unsilencedRange", 20.0, 0.0, Double.MAX_VALUE);
                this.silencedRange = builder.comment("Any mobs within a sphere of this radius will flee from the shooter of a silenced gun.").defineInRange("silencedRange", 5.0, 0.0, Double.MAX_VALUE);
                this.fleeingEntities = builder.comment("LEGACY: Additional entities that will flee from gunshots. Animals are automatically detected. Use the 'scguns:fleeing_from_guns' entity tag instead for better mod compatibility.").defineList("fleeingEntities", Arrays.asList("minecraft:villager"), o -> o instanceof String);
            }
            builder.pop();
        }
    }

    /**
     * Missile related config options
     */
    public static class Rockets
    {
        public final ModConfigSpec.DoubleValue explosionRadius;

        public Rockets(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to rockets").push("rockets");
            {
                this.explosionRadius = builder.comment("The max distance which the explosion is effective to").defineInRange("explosionRadius", 4.0, 0.0, Double.MAX_VALUE);
            }
            builder.pop();
        }
    }

    /**
     * Grenade related config options
     */
    public static class Grenades
    {
        public final ModConfigSpec.DoubleValue explosionRadius;

        public Grenades(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to grenades").push("grenades");
            {
                this.explosionRadius = builder.comment("The max distance which the explosion is effective to").defineInRange("explosionRadius", 5.0, 0.0, Double.MAX_VALUE);
            }
            builder.pop();
        }
    }



    /**
     * Stun Grenade related config options
     */
    public static class StunGrenades
    {
        public final Blind blind;
        public final Deafen deafen;
        public final ModConfigSpec.ConfigValue<List<? extends String>> flashProtectionItems;

        public StunGrenades(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to stun grenades").push("stun_grenades");
            {
                this.blind = new Blind(builder);
                this.deafen = new Deafen(builder);
                this.flashProtectionItems = builder.comment("Extra helmet item ids (e.g. \"minecraft:iron_helmet\") that count as flash protection, on top of the 'scguns:flash_protection' item tag. Flash protection blocks stun grenade blinding and halves deafening.").defineList("flashProtectionItems", Collections.emptyList(), o -> o instanceof String);
            }
            builder.pop();
        }
    }

    /**
     * Stun grenade blinding related config options
     */
    public static class Blind
    {
        public final EffectCriteria criteria;
        public final ModConfigSpec.BooleanValue blindMobs;

        public Blind(ModConfigSpec.Builder builder)
        {
            builder.comment("Blinding properties of stun grenades").push("blind");
            {
                this.criteria = new EffectCriteria(builder, 15, 220, 10, 170, 0.75, true);
                this.blindMobs = builder.comment("If true, hostile mobs will be unable to target entities while they are blinded by a stun grenade.").define("blindMobs", true);
            }
            builder.pop();
        }
    }

    /**
     * Stun grenade deafening related config options
     */
    public static class Deafen
    {
        public final EffectCriteria criteria;
        public final ModConfigSpec.BooleanValue panicMobs;

        public Deafen(ModConfigSpec.Builder builder)
        {
            builder.comment("Deafening properties of stun grenades").push("deafen");
            {
                this.criteria = new EffectCriteria(builder, 15, 280, 100, 360, 0.75, false);
                this.panicMobs = builder.comment("If true, peaceful mobs will panic upon being deafened by a stun grenade.").define("panicMobs", true);
            }
            builder.pop();
        }
    }

    /**
     * Config options for effect criteria
     */
    public static class EffectCriteria
    {
        public final ModConfigSpec.DoubleValue radius;
        public final ModConfigSpec.IntValue durationMax;
        public final ModConfigSpec.IntValue durationMin;
        public final ModConfigSpec.DoubleValue angleEffect;
        public final ModConfigSpec.DoubleValue angleAttenuationMax;
        public final ModConfigSpec.BooleanValue raytraceOpaqueBlocks;

        public EffectCriteria(ModConfigSpec.Builder builder, double radius, int durationMax, int durationMin, double angleEffect, double angleAttenuationMax, boolean raytraceOpaqueBlocks)
        {
            builder.push("effect_criteria");
            {
                this.radius = builder.comment("Grenade must be no more than this many meters away to have an effect.").defineInRange("radius", radius, 0.0, Double.MAX_VALUE);
                this.durationMax = builder.comment("Effect will have this duration (in ticks) if the grenade is directly at the player's eyes while looking directly at it.").defineInRange("durationMax", durationMax, 0, Integer.MAX_VALUE);
                this.durationMin = builder.comment("Effect will have this duration (in ticks) if the grenade is the maximum distance from the player's eyes while looking directly at it.").defineInRange("durationMin", durationMin, 0, Integer.MAX_VALUE);
                this.angleEffect = builder.comment("Angle between the eye/looking direction and the eye/grenade direction must be no more than half this many degrees to have an effect.").defineInRange("angleEffect", angleEffect, 0, 360);
                this.angleAttenuationMax = builder.comment("After duration is attenuated by distance, it will be further attenuated depending on the angle (in degrees) between the eye/looking direction and the eye/grenade direction. This is done by multiplying it by 1 (no attenuation) if the angle is 0; and by this value if the angle is the maximum within the angle of effect.").defineInRange("angleAttenuationMax", angleAttenuationMax, 0.0, 1.0);
                this.raytraceOpaqueBlocks = builder.comment("If true, the effect is only applied if the line between the eyes and the grenade does not intersect any non-liquid blocks with an opacity greater than 0.").define("raytraceOpaqueBlocks", raytraceOpaqueBlocks);
            }
            builder.pop();
        }
    }

    /**
     * Projectile spread config options
     */
    public static class ProjectileSpread
    {
        public final ModConfigSpec.IntValue spreadThreshold;
        public final ModConfigSpec.IntValue maxCount;

        public ProjectileSpread(ModConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to projectile spread").push("projectile_spread");
            {
                this.spreadThreshold = builder.comment("The amount of time in milliseconds before logic to apply spread is skipped. The value indicates a reasonable amount of time before a weapon is considered stable again.").defineInRange("spreadThreshold", 300, 0, 1000);
                this.maxCount = builder.comment("The amount of times a player has to shoot within the spread threshold before the maximum amount of spread is applied. Setting the value higher means it will take longer for the spread to be applied.").defineInRange("maxCount", 10, 1, Integer.MAX_VALUE);
            }
            builder.pop();
        }
    }
    public static class GunScalingConfig {
        private static final ModConfigSpec SPEC;
        public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        private static final GunScalingConfig INSTANCE = new GunScalingConfig();

        private static final ModConfigSpec.BooleanValue enableScalingDamage;
        private static final ModConfigSpec.DoubleValue damageIncreaseRate;
        private static final ModConfigSpec.DoubleValue baseDamage;
        private static final ModConfigSpec.DoubleValue maxDamage;

        static {
            BUILDER.push("Scaling Damage");

            enableScalingDamage = BUILDER
                    .comment("If true, gun damage will scale with the days in the world.")
                    .define("Enable Scaling Damage", false);

            damageIncreaseRate = BUILDER
                    .comment("The decimal amount that gun damage increases per day.")
                    .defineInRange("Damage Increase Rate", 0.03, 0.0, Double.POSITIVE_INFINITY);

            baseDamage = BUILDER
                    .comment("The base damage value for the guns.")
                    .defineInRange("Base Damage", 1.0, 0.0, Double.POSITIVE_INFINITY);

            maxDamage = BUILDER
                    .comment("The maximum damage that gun scaling can reach.")
                    .defineInRange("Max Scaled Damage", Double.POSITIVE_INFINITY, 0.0, Double.POSITIVE_INFINITY);

            BUILDER.pop();

            SPEC = BUILDER.build();
        }

        // Getters
        public boolean isScalingEnabled() {
            return enableScalingDamage.get();
        }

        public double getDamageIncreaseRate() {
            return damageIncreaseRate.get();
        }

        public double getBaseDamage() {
            return baseDamage.get();
        }

        public double getMaxDamage() {
            return maxDamage.get();
        }

        // Setters
        public void setScalingEnabled(boolean enabled) {
            enableScalingDamage.set(enabled);
        }

        public void setDamageIncreaseRate(double rate) {
            damageIncreaseRate.set(rate);
        }

        public void setBaseDamage(double damage) {
            baseDamage.set(damage);
        }

        public void setMaxDamage(double max) {
            maxDamage.set(max);
        }

        public static void setup(ModContainer modContainer) {
            Path configPath = FMLPaths.CONFIGDIR.get();
            Path gunConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "gun_scaling");

            // Create the config folder
            try {
                Files.createDirectory(gunConfigPath);
            } catch (Exception e) {
                // Do nothing
            }

            modContainer.registerConfig(ModConfig.Type.COMMON, SPEC, "gun_scaling/main.toml");
        }

        public static GunScalingConfig getInstance() {
            return INSTANCE;
        }

        public void save() {
            SPEC.save();
        }
    }

    /**
     * Server related config options
     */
    public static class Server
    {
        public final ModConfigSpec.IntValue alphaOverlay;
        public final ModConfigSpec.IntValue alphaFadeThreshold;
        public final ModConfigSpec.DoubleValue soundPercentage;
        public final ModConfigSpec.IntValue soundFadeThreshold;
        public final ModConfigSpec.DoubleValue ringVolume;
        public final ModConfigSpec.DoubleValue gunShotMaxDistance;
        public final ModConfigSpec.DoubleValue reloadMaxDistance;
        public final ModConfigSpec.BooleanValue enableDistantGunshots;
        public final ModConfigSpec.DoubleValue distantGunShotMaxDistance;
        public final ModConfigSpec.DoubleValue distantGunShotVolumeMultiplier;
        public final ModConfigSpec.IntValue distantGunShotThrottleTicks;
        public final ModConfigSpec.DoubleValue distantGunShotSpeedOfSoundBlocksPerSecond;
        public final ModConfigSpec.IntValue distantGunShotMaxDelayTicks;
        public final ModConfigSpec.BooleanValue enableCameraRecoil;
        public final ModConfigSpec.IntValue cooldownThreshold;
        public final Experimental experimental;

        public Server(ModConfigSpec.Builder builder)
        {
            builder.push("server");
            {
                builder.comment("Stun Grenade related properties").push("grenade");
                {
                    this.alphaOverlay = builder.comment("After the duration drops to this many ticks, the transparency of the overlay when blinded will gradually fade to 0 alpha.").defineInRange("alphaOverlay", 255, 0, 255);
                    this.alphaFadeThreshold = builder.comment("Transparency of the overlay when blinded will be this alpha value, before eventually fading to 0 alpha.").defineInRange("alphaFadeThreshold", 40, 0, Integer.MAX_VALUE);
                    this.soundPercentage = builder.comment("Volume of most game sounds when deafened will play at this percent, before eventually fading back to %100.").defineInRange("soundPercentage", 0.05, 0.0, 1.0);
                    this.soundFadeThreshold = builder.comment("After the duration drops to this many ticks, the ringing volume will gradually fade to 0 and other sound volumes will fade back to %100.").defineInRange("soundFadeThreshold", 90, 0, Integer.MAX_VALUE);
                    this.ringVolume = builder.comment("Volume of the ringing sound when deafened will play at this volume, before eventually fading to 0.").defineInRange("ringVolume", 1.0, 0.0, 1.0);
                }
                builder.pop();

                builder.comment("Audio properties").push("audio");
                {
                    this.gunShotMaxDistance = builder.comment("The maximum distance weapons can be heard by players.").defineInRange("gunShotMaxDistance", 100, 0, Double.MAX_VALUE);
                    this.reloadMaxDistance = builder.comment("The maximum distance reloading can be heard by players.").defineInRange("reloadMaxDistance", 24, 0, Double.MAX_VALUE);
                    this.enableDistantGunshots = builder.comment("If true, players outside the normal gunshot radius can hear delayed distant gunshot reports.").define("enableDistantGunshots", true);
                    this.distantGunShotMaxDistance = builder.comment("The maximum distance distant unsilenced gunshots can be heard by players.").defineInRange("distantGunShotMaxDistance", 640.0, 0.0, Double.MAX_VALUE);
                    this.distantGunShotVolumeMultiplier = builder.comment("Volume multiplier for the distant gunshot layer.").defineInRange("distantGunShotVolumeMultiplier", 2.5, 0.0, 10.0);
                    this.distantGunShotThrottleTicks = builder.comment("Minimum ticks between distant gunshot packets from the same source to the same listener. Helps automatic weapons stay cheap on servers.").defineInRange("distantGunShotThrottleTicks", 8, 0, 200);
                    this.distantGunShotSpeedOfSoundBlocksPerSecond = builder.comment("Speed used to delay distant gunshot reports. Minecraft blocks are treated as meters.").defineInRange("distantGunShotSpeedOfSoundBlocksPerSecond", 343.0, 1.0, Double.MAX_VALUE);
                    this.distantGunShotMaxDelayTicks = builder.comment("Maximum client delay for distant gunshot reports in ticks.").defineInRange("distantGunShotMaxDelayTicks", 80, 0, 400);
                }
                builder.pop();

                this.enableCameraRecoil = builder.comment("If true, enables camera recoil when firing a weapon").define("enableCameraRecoil", true);
                this.cooldownThreshold = builder.comment("The maximum amount of cooldown time remaining before the server will accept another shoot packet from a client. This allows for a litle slack since the server may be lagging").defineInRange("cooldownThreshold", 0, 0, 1000);

                this.experimental = new Experimental(builder);
            }
            builder.pop();
        }

        public static class Experimental
        {
            public final ModConfigSpec.BooleanValue forceDyeableAttachments;

            public Experimental(ModConfigSpec.Builder builder)
            {
                builder.push("experimental");
                this.forceDyeableAttachments = builder.comment("Forces all attachments to be dyeable regardless if they have an affect on the model. This is useful if your server uses custom models for attachments and the models have dyeable elements").define("forceDyeableAttachments", false);
                builder.pop();
            }
        }
    }

    static final ModConfigSpec clientSpec;
    public static final Config.Client CLIENT;

    static final ModConfigSpec commonSpec;
    public static final Config.Common COMMON;

    static final ModConfigSpec serverSpec;
    public static final Config.Server SERVER;

    static
    {
        final Pair<Client, ModConfigSpec> clientSpecPair = new ModConfigSpec.Builder().configure(Config.Client::new);
        clientSpec = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();

        final Pair<Common, ModConfigSpec> commonSpecPair = new ModConfigSpec.Builder().configure(Common::new);
        commonSpec = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();

        final Pair<Server, ModConfigSpec> serverSpecPair = new ModConfigSpec.Builder().configure(Server::new);
        serverSpec = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }

    public static void saveClientConfig()
    {
        clientSpec.save();
    }
}


