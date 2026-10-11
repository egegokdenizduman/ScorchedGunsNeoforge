package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.event.GunEventBus;
import top.ribs.scguns.init.ModEnchantments;
import top.ribs.scguns.item.GunItem;

import java.util.Objects;

public class C2SMessageEjectCasing
{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageEjectCasing> STREAM_CODEC = StreamCodec.of(
            C2SMessageEjectCasing::encode,
            C2SMessageEjectCasing::decode
    );

    public C2SMessageEjectCasing() {}

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageEjectCasing message) {}

    private static C2SMessageEjectCasing decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageEjectCasing();
    }

    public static void handle(C2SMessageEjectCasing message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if (player != null && !player.isSpectator()) {
                ItemStack heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof GunItem) {
                    if (!heldItem.getItem().getClass().getPackageName().startsWith("top.ribs.scguns")) {
                        return;
                    }

                    Gun gun = ((GunItem)heldItem.getItem()).getModifiedGun(heldItem);
                    if (gun.getProjectile().casingType != null && !player.getAbilities().instabuild) {
                        ItemStack casingStack = new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.get(gun.getProjectile().casingType)));
                        GunEventBus.dropCasing(player, heldItem, casingStack);
                    }
                }
            }
            context.setHandled(true);
        });
    }
}
