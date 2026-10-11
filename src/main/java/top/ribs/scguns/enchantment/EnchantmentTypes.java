package top.ribs.scguns.enchantment;

import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.init.ModTags;
import top.ribs.scguns.item.GunItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import top.ribs.scguns.item.NonUnderwaterGunItem;
import top.ribs.scguns.item.animated.NonUnderwaterAnimatedGunItem;

import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class EnchantmentTypes {
    public static final Predicate<Item> GUN = item -> item instanceof GunItem;
    public static final Predicate<Item> WEAPON = item -> item instanceof SwordItem;
    public static final Predicate<Item> SEMI_AUTO_GUN = item -> item instanceof GunItem && ((GunItem) item).getGun().getGeneral().getFireMode() != FireMode.AUTOMATIC;

    public static final Predicate<Item> TRIGGER_FINGER_COMPATIBLE = item -> item instanceof GunItem && !item.builtInRegistryHolder().is(ModTags.Items.SINGLE_SHOT);


    public static final Predicate<Item> WATER_PROOF_COMPATIBLE = item -> item instanceof GunItem;

    // Shell catcher compatible
    public static final Predicate<Item> SHELL_CATCHER_COMPATIBLE = item -> item instanceof GunItem && !item.builtInRegistryHolder().is(ModTags.Items.DOES_NOT_EJECT_CASINGS);

    // Collateral compatible
    public static final Predicate<Item> COLLATERAL_COMPATIBLE = item -> item instanceof GunItem && !item.builtInRegistryHolder().is(ModTags.Items.NON_COLLATERAL);
}
