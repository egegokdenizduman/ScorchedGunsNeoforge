package top.ribs.scguns.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.item.ammo_boxes.AmmoBoxTooltip;

import java.util.List;

/**
 * Bundle-like contents preview for ammo boxes. Unlike the vanilla bundle tooltip (which
 * assumes a capacity of one stack and therefore showed boxes as "full" at 64 items) this
 * draws one slot per stored stack plus one empty slot per free whole stack.
 */
public class ClientAmmoBoxTooltip implements ClientTooltipComponent {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/background");
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/slot");
    private static final ResourceLocation BLOCKED_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/blocked_slot");
    private static final int SLOT_W = 18;
    private static final int SLOT_H = 20;
    private static final int MAX_COLUMNS = 8;
    private static final int MAX_SLOTS = 64;

    private final List<ItemStack> contents;
    private final int freeSlots;
    private final boolean full;

    public ClientAmmoBoxTooltip(AmmoBoxTooltip tooltip) {
        this.contents = tooltip.contents();
        int free = Math.max(0, tooltip.capacityUnits() - tooltip.usedUnits());
        this.freeSlots = free / AmmoBoxItem.UNITS_PER_STACK;
        this.full = free <= 0;
    }

    private int slotCount() {
        // a full box gets one extra greyed-out slot, like the vanilla bundle
        return Math.min(MAX_SLOTS, contents.size() + freeSlots + (full ? 1 : 0));
    }

    private int columns() {
        return Math.max(1, Math.min(MAX_COLUMNS, slotCount()));
    }

    private int rows() {
        return Math.max(1, (slotCount() + columns() - 1) / columns());
    }

    @Override
    public int getHeight() {
        return rows() * SLOT_H + 2 + 4;
    }

    @Override
    public int getWidth(Font font) {
        return columns() * SLOT_W + 2;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int columns = columns();
        int rows = rows();
        graphics.blitSprite(BACKGROUND_SPRITE, x, y, columns * SLOT_W + 2, rows * SLOT_H + 2);
        int total = slotCount();
        for (int i = 0; i < total; i++) {
            int sx = x + 1 + (i % columns) * SLOT_W;
            int sy = y + 1 + (i / columns) * SLOT_H;
            if (i < contents.size()) {
                ItemStack stack = contents.get(i);
                graphics.blitSprite(SLOT_SPRITE, sx, sy, 0, SLOT_W, SLOT_H);
                graphics.renderItem(stack, sx + 1, sy + 1, i);
                graphics.renderItemDecorations(font, stack, sx + 1, sy + 1);
            } else if (full && i == total - 1) {
                graphics.blitSprite(BLOCKED_SLOT_SPRITE, sx, sy, 0, SLOT_W, SLOT_H);
            } else {
                graphics.blitSprite(SLOT_SPRITE, sx, sy, 0, SLOT_W, SLOT_H);
            }
        }
    }
}
