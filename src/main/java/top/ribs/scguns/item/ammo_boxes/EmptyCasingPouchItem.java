package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class EmptyCasingPouchItem extends AmmoBoxItem {
    private static final int STACK_CAPACITY = 16;
    private static final int BAR_COLOR = 0xE6B85C;

    public EmptyCasingPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    protected ResourceLocation getAmmoTag() {
        return ResourceLocation.fromNamespaceAndPath("scguns", "empty_casing");
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
