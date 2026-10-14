package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;

public class EmptyCasingPouchItem extends AmmoBoxItem {
    private static final int BAR_COLOR = 0xE6B85C;

    public EmptyCasingPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public ResourceLocation getAmmoTag() {
        return ResourceLocation.fromNamespaceAndPath("scguns", "empty_casing");
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isFull(stack) ? super.getBarColor(stack) : BAR_COLOR;
    }
}
