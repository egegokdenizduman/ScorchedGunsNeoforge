package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.entity.projectile.*;
import top.ribs.scguns.entity.throwable.*;

import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, Reference.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("projectile", ProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BearPackShellProjectileEntity>> BEARPACK_SHELL_PROJECTILE = registerBasic("bearpack_shell_projectile", BearPackShellProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<OsborneSlugProjectileEntity>> OSBORNE_SLUG_PROJECTILE = registerBasic("osborne_slug_projectile", OsborneSlugProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<PlasmaProjectileEntity>> PLASMA_PROJECTILE = registerBasic("plasma_projectile", PlasmaProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<RamrodProjectileEntity>> RAMROD_PROJECTILE = registerBasic("ramrod_projectile", RamrodProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<HogRoundProjectileEntity>> HOG_ROUND_PROJECTILE = registerBasic("hog_round_projectile", HogRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BeowulfProjectileEntity>> BEOWULF_PROJECTILE = registerBasic("beowulf_projectile", BeowulfProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BlazeRodProjectileEntity>> BLAZE_ROD_PROJECTILE = registerBasic("blaze_rod_projectile", BlazeRodProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BasicBulletProjectileEntity>> BASIC_BULLET_PROJECTILE = registerBasic("basic_bullet_projectile", BasicBulletProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<HardenedBulletProjectileEntity>> HARDENED_BULLET_PROJECTILE = registerBasic("hardened_bullet_projectile", HardenedBulletProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BuckshotProjectileEntity>> BUCKSHOT_PROJECTILE = registerBasic("buckshot_projectile", BuckshotProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<FireRoundEntity>> FIRE_ROUND_PROJECTILE = registerBasic("fire_round_projectile", FireRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<GrenadeEntity>> GRENADE = registerBasic("grenade", GrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<RocketEntity>> ROCKET = registerBasic("rocket", RocketEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<MicroJetEntity>> MICROJET = registerBasic("microjet", MicroJetEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ShulkshotProjectileEntity>> SHULKSHOT = registerBasic("shulkshot_projectile", ShulkshotProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkCellEntity>> SCULK_CELL = registerBasic("sculk_cell", SculkCellEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ShatterRoundProjectileEntity>> SHATTER_ROUND_PROJECTILE = registerBasic("shatter_round_projectile", ShatterRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<KrahgRoundProjectileEntity>> KRAHG_ROUND_PROJECTILE = registerBasic("krahg_round_projectile", KrahgRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<AdvancedRoundProjectileEntity>> ADVANCED_ROUND_PROJECTILE = registerBasic("advanced_round_projectile", AdvancedRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<GibbsRoundProjectileEntity>> GIBBS_ROUND_PROJECTILE = registerBasic("gibbs_round_projectile", GibbsRoundProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ShotballProjectileEntity>> SHOTBALL_PROJECTILE = registerBasic("shotball_projectile", ShotballProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<HeGrenadeRoundEntity>> HE_GRENADE_ROUND = registerBasic("he_grenade_round", HeGrenadeRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<FireGrenadeRoundEntity>> FIRE_GRENADE_ROUND = registerBasic("fire_grenade_round", FireGrenadeRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<GasGrenadeRoundEntity>> GAS_GRENADE_ROUND = registerBasic("gas_grenade_round", GasGrenadeRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<BouncyGrenadeRoundEntity>> BOUNCY_GRENADE_ROUND = registerBasic("bouncy_grenade_round", BouncyGrenadeRoundEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableGrenadeEntity>> THROWABLE_GRENADE = registerBasic("throwable_grenade", ThrowableGrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableStunGrenadeEntity>> THROWABLE_STUN_GRENADE = registerBasic("throwable_stun_grenade", ThrowableStunGrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableMolotovCocktailEntity>> THROWABLE_MOLOTOV_COCKTAIL = registerBasic("throwable_molotov_cocktail", ThrowableMolotovCocktailEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableSoulMolotovCocktailEntity>> THROWABLE_SOUL_MOLOTOV_COCKTAIL = registerBasic("throwable_soul_molotov_cocktail", ThrowableSoulMolotovCocktailEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableShotballEntity>> THROWABLE_SHOTBALL = registerBasic("throwable_shotball", ThrowableShotballEntity::new);
    /* Mobs */

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerBasic(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(100)
                .setUpdateInterval(1)
                .noSummon()
                .fireImmune()
                .noSave()
                .setShouldReceiveVelocityUpdates(true).build(id));
    }

    private static <T extends ProjectileEntity> DeferredHolder<EntityType<?>, EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(0)
                .noSummon()
                .fireImmune()
                .noSave()
                .setShouldReceiveVelocityUpdates(false)
                .build(id));
    }
}





