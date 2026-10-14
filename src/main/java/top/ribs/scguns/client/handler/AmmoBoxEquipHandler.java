package top.ribs.scguns.client.handler;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import top.ribs.scguns.client.KeyBinds;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.C2SMessageEquipAmmoBoxes;

/**
 * Alt + right-click (rebindable) with an ammo box in the selected hotbar slot puts it on the belt, like armour.
 */
public class AmmoBoxEquipHandler {
    private static boolean holdingAmmoBox() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getMainHandItem().getItem() instanceof AmmoBoxItem;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (KeyBinds.KEY_EQUIP_AMMO_BOXES.consumeClick()) {
            if (mc.screen == null && holdingAmmoBox()) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageEquipAmmoBoxes());
            }
        }
    }

    // Right-click is still held as "use" while Alt is down, so stop the box from opening or being placed.
    @SubscribeEvent
    public static void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isUseItem() && KeyBinds.KEY_EQUIP_AMMO_BOXES.isDown() && holdingAmmoBox()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }
}
