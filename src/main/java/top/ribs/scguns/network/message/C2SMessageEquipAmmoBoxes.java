package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import top.ribs.scguns.common.network.ServerPlayHandler;

/**
 * Alt + right-click: put the held ammo box on the belt.
 */
public class C2SMessageEquipAmmoBoxes
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageEquipAmmoBoxes> STREAM_CODEC = StreamCodec.of(
            C2SMessageEquipAmmoBoxes::encode,
            C2SMessageEquipAmmoBoxes::decode
    );

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageEquipAmmoBoxes message) {}
    private static C2SMessageEquipAmmoBoxes decode(RegistryFriendlyByteBuf buffer)
    {
        return new C2SMessageEquipAmmoBoxes();
    }
    public static void handle(C2SMessageEquipAmmoBoxes message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if(player != null && !player.isSpectator())
            {
                ServerPlayHandler.handleEquipAmmoBoxes(player);
            }
        });
        context.setHandled(true);
    }
}
