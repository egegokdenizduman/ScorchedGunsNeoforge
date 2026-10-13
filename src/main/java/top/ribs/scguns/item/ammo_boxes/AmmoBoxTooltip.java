package top.ribs.scguns.item.ammo_boxes;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Tooltip data for ammo boxes: the stored stacks plus used / total capacity in weight units
 * ({@link top.ribs.scguns.item.AmmoBoxItem#UNITS_PER_STACK} per stack). Rendered by
 * {@code ClientAmmoBoxTooltip}, which draws one slot per stored stack and an empty slot for
 * every whole stack that is still free, so the preview always matches the real capacity.
 */
public record AmmoBoxTooltip(List<ItemStack> contents, int usedUnits, int capacityUnits) implements TooltipComponent {
}
