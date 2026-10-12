package top.ribs.scguns.init;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.ribs.scguns.item.EnergyGunItem;
import top.ribs.scguns.item.MoldItem;
import top.ribs.scguns.item.animated.AnimatedEnergyGunItem;

public class ModCapabilities {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new MoldItem.ExperienceRepairHandler(stack),
                ModItems.SMALL_CASING_MOLD.get(),
                ModItems.MEDIUM_CASING_MOLD.get(),
                ModItems.LARGE_CASING_MOLD.get(),
                ModItems.BULLET_MOLD.get(),
                ModItems.DISC_MOLD.get(),
                ModItems.GUN_PARTS_MOLD.get()
        );
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, context) -> {
                    if (stack.getItem() instanceof EnergyGunItem energyGun) {
                        return new EnergyGunItem.ItemEnergyStorage(stack, energyGun.getCapacity());
                    }
                    if (stack.getItem() instanceof AnimatedEnergyGunItem animatedEnergyGun) {
                        return new AnimatedEnergyGunItem.ItemEnergyStorage(stack, animatedEnergyGun.getCapacity());
                    }
                    return null;
                },
                ModItems.SCRATCHES.get(),
                ModItems.GALE.get(),
                ModItems.UMAX_PISTOL.get(),
                ModItems.VENTURI.get()
        );
    }
}
