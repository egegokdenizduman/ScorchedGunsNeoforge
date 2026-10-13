package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class PistolAmmoBoxItem extends AmmoBoxItem {
    private static final int STACK_CAPACITY = 5;
    private static final int BAR_COLOR = 0xD24A3C;

    public PistolAmmoBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected ResourceLocation getAmmoTag() {
        return ResourceLocation.fromNamespaceAndPath("scguns", "pistol_ammo");
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
