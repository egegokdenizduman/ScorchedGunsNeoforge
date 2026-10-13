package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class EnergyAmmoBoxItem extends AmmoBoxItem {
    private static final int STACK_CAPACITY = 5;
    private static final int BAR_COLOR = 0x58B8E8;

    public EnergyAmmoBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected ResourceLocation getAmmoTag() {
        return ResourceLocation.fromNamespaceAndPath("scguns", "energy_ammo");
    }

    @Override
    protected int getBaseStackCapacity() {
        return STACK_CAPACITY;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isFull(stack) ? super.getBarColor(stack) : BAR_COLOR;
    }
}
