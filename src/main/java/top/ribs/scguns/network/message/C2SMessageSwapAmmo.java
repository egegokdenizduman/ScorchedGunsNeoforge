package top.ribs.scguns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;

public class C2SMessageSwapAmmo {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageSwapAmmo> STREAM_CODEC = StreamCodec.of(
            C2SMessageSwapAmmo::encode,
            C2SMessageSwapAmmo::decode
    );
    private final ResourceLocation selectedAmmo;

    public C2SMessageSwapAmmo(ResourceLocation selectedAmmo) {
        this.selectedAmmo = selectedAmmo;
    }

    private static CompoundTag getStackTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : new CompoundTag();
    }

    private static void setStackTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static void encode(RegistryFriendlyByteBuf buffer, C2SMessageSwapAmmo message) {
        buffer.writeResourceLocation(message.selectedAmmo);
    }

    private static C2SMessageSwapAmmo decode(RegistryFriendlyByteBuf buffer) {
        return new C2SMessageSwapAmmo(buffer.readResourceLocation());
    }

    public static void handle(C2SMessageSwapAmmo message, MessageContext context) {
        context.execute(() -> {
            ServerPlayer player = context.getPlayer()
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .orElse(null);
            if (player == null || player.isSpectator()) {
                return;
            }

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof GunItem gunItem)) {
                return;
            }

            Gun gun = gunItem.getModifiedGun(stack);
            if (!gun.getGeneral().allowsAmmoChange()) {
                player.displayClientMessage(Component.translatable("message.scguns.ammo_swap.not_supported").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (gun.getAcceptedProjectiles().size() <= 1) {
                player.displayClientMessage(Component.translatable("message.scguns.ammo_swap.no_types").withStyle(ChatFormatting.RED), true);
                return;
            }

            Item selected = BuiltInRegistries.ITEM.getOptional(message.selectedAmmo).orElse(null);
            if (selected == null) {
                player.displayClientMessage(Component.translatable("message.scguns.ammo_swap.no_types").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!gun.acceptsProjectileItem(selected)) {
                player.displayClientMessage(Component.translatable("message.scguns.ammo_swap.not_supported").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!player.isCreative() && Gun.findAmmo(player, selected).stack().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.scguns.ammo_swap.no_available").withStyle(ChatFormatting.RED), true);
                return;
            }

            Gun.setSelectedProjectileItem(stack, gun, selected);
            CompoundTag tag = getStackTag(stack);
            int loadedAmmo = tag.getInt("AmmoCount");
            if (loadedAmmo > 0) {
                if (!player.isCreative()) {
                    ItemStack returnedAmmo = Gun.getLoadedAmmoStack(stack, gun, loadedAmmo, player.registryAccess());
                    if (!returnedAmmo.isEmpty()) {
                        if (!player.getInventory().add(returnedAmmo)) {
                            player.drop(returnedAmmo, false);
                        }
                    }
                }
                tag.putInt("AmmoCount", 0);
                setStackTag(stack, tag);
                Gun.clearLoadedProjectileItem(stack);
                PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateAmmo(0));
            }

            player.displayClientMessage(Component.translatable("message.scguns.ammo_swap.changed")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new ItemStack(selected).getHoverName().copy().withStyle(ChatFormatting.YELLOW)), true);
        });
        context.setHandled(true);
    }
}
