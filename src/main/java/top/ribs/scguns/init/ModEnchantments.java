package top.ribs.scguns.init;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;

import top.ribs.scguns.Reference;
import top.ribs.scguns.enchantment.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;

import static top.ribs.scguns.enchantment.GunEnchantment.Rarity;
import static top.ribs.scguns.enchantment.GunEnchantment.Type;

/**
 * Author: MrCrayfish
 */
public class ModEnchantments
{
    public static final DeferredRegister<Enchantment> REGISTER = DeferredRegister.create(Registries.ENCHANTMENT, Reference.MOD_ID);

    public static final DeferredHolder<Enchantment, Enchantment> QUICK_HANDS = register("quick_hands", EnchantmentTypes.GUN, Rarity.UNCOMMON, 2, 1, 10, 61, 10, Type.AMMO, ModEnchantments::ammoExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> TRIGGER_FINGER = register("trigger_finger", EnchantmentTypes.TRIGGER_FINGER_COMPATIBLE, Rarity.UNCOMMON, 2, 15, 10, 55, 10, Type.WEAPON, ModEnchantments::weaponExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> LIGHTWEIGHT = register("lightweight", EnchantmentTypes.GUN, Rarity.RARE, 2, 15, 0, 35, 0, Type.WEAPON, ModEnchantments::weaponExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> COLLATERAL = register("collateral", EnchantmentTypes.COLLATERAL_COMPATIBLE, Rarity.RARE, 1, 10, 0, 30, 0, Type.PROJECTILE, ModEnchantments::projectileExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> RECLAIMED = register("reclaimed", EnchantmentTypes.GUN, Rarity.RARE, 2, 15, 10, 25, 10, Type.AMMO, ModEnchantments::ammoExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> ACCELERATOR = register("accelerator", EnchantmentTypes.GUN, Rarity.UNCOMMON, 3, 10, 10, 30, 10, Type.PROJECTILE, ModEnchantments::projectileExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> PUNCTURING = register("puncturing", EnchantmentTypes.GUN, Rarity.UNCOMMON, 3, 1, 10, 11, 10, Type.PROJECTILE, ModEnchantments::projectileExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> SHELL_CATCHER = register("shell_catcher", EnchantmentTypes.SHELL_CATCHER_COMPATIBLE, Rarity.UNCOMMON, 1, 15, 0, 35, 0, Type.WEAPON, ModEnchantments::weaponExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> HEAVY_SHOT = register("heavy_shot", EnchantmentTypes.GUN, Rarity.RARE, 3, 10, 10, 30, 10, Type.PROJECTILE, ModEnchantments::projectileExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> ELEMENTAL_POP = register("elemental_pop", EnchantmentTypes.GUN, Rarity.VERY_RARE, 2, 15, 10, 55, 10, Type.WEAPON, ModEnchantments::weaponExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> WATER_PROOF = register("waterproof", EnchantmentTypes.WATER_PROOF_COMPATIBLE, Rarity.UNCOMMON, 1, 10, 10, 30, 10, Type.PROJECTILE, ModEnchantments::projectileExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> HOT_BARREL = register("hot_barrel", EnchantmentTypes.GUN, Rarity.VERY_RARE, 2, 10, 10, 30, 10, Type.PROJECTILE, ModEnchantments::projectileExclusive);
    public static final DeferredHolder<Enchantment, Enchantment> GUN_RUST = register("gun_rust", EnchantmentTypes.GUN, Rarity.VERY_RARE, 1, 25, 0, 75, 0, Type.WEAPON, ModEnchantments::weaponExclusive);

    private static DeferredHolder<Enchantment, Enchantment> register(String name, Predicate<Item> supportedItems, Rarity rarity,
                                                                     int maxLevel, int minCostBase, int minCostPerLevel,
                                                                     int maxCostBase, int maxCostPerLevel, Type type,
                                                                     Supplier<HolderSet<Enchantment>> exclusiveSet) {
        return REGISTER.register(name, id -> GunEnchantment.build(id, supportedItems, rarity, maxLevel, minCostBase, minCostPerLevel, maxCostBase, maxCostPerLevel, exclusiveSet.get()));
    }

    @SafeVarargs
    private static HolderSet<Enchantment> exclusive(Holder<Enchantment>... enchantments) {
        return HolderSet.direct(enchantments);
    }

    private static HolderSet<Enchantment> weaponExclusive() {
        return exclusive(TRIGGER_FINGER, LIGHTWEIGHT, SHELL_CATCHER, ELEMENTAL_POP, GUN_RUST);
    }

    private static HolderSet<Enchantment> ammoExclusive() {
        return exclusive(QUICK_HANDS, RECLAIMED);
    }

    private static HolderSet<Enchantment> projectileExclusive() {
        return exclusive(COLLATERAL, ACCELERATOR, PUNCTURING, HEAVY_SHOT, WATER_PROOF, HOT_BARREL);
    }
}





