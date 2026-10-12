package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.interfaces.IAirGun;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AirGunItem extends GunItem implements IAirGun {

    public AirGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return false;
        }
        Player player = getClientPlayer();
        List<ItemStack> backtanks = CreateBacktankBridge.getAllWithAir(player);
        return !backtanks.isEmpty() || stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return 0;
        }
        Player player = getClientPlayer();
        List<ItemStack> backtanks = CreateBacktankBridge.getAllWithAir(player);
        if (!backtanks.isEmpty()) {
            ItemStack backtank = backtanks.get(0);
            int maxAir = CreateBacktankBridge.maxAir(backtank);
            float air = CreateBacktankBridge.getAir(backtank);
            return maxAir > 0 ? Math.round(13.0F * air / maxAir) : 0;
        }
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / (float) stack.getMaxDamage());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return Mth.hsvToRgb(1.0F, 1.0F, 1.0F);
        }
        Player player = getClientPlayer();
        List<ItemStack> backtanks = CreateBacktankBridge.getAllWithAir(player);
        if (!backtanks.isEmpty()) {
            return CreateBacktankBridge.getBarColor(backtanks.get(0), 1);
        }
        if (stack.getDamageValue() >= (stack.getMaxDamage() / 1.5)) {
            return Objects.requireNonNull(ChatFormatting.RED.getColor());
        }
        float f = Math.max(0.0F, (stack.getMaxDamage() - (float) stack.getDamageValue()) / (float) stack.getMaxDamage());
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        Gun gun = this.getModifiedGun(stack);
        int airUsage = top.ribs.scguns.event.GunEventBus.getAirCostPerShot(gun);

        tooltip.add(Component.translatable("info.airgun.air_usage")
                .append(": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(airUsage)).withStyle(ChatFormatting.WHITE)));

        if (context.level() != null && context.level().isClientSide) {
            Player player = getClientPlayer();
            if (player != null) {
                List<ItemStack> backtanks = CreateBacktankBridge.getAllWithAir(player);
                if (backtanks.isEmpty()) {
                    tooltip.add(Component.translatable("info.airgun.requires_airtank")
                            .withStyle(ChatFormatting.RED));
                }
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
    public interface IAirGun {
        // Empty interface just for type checking
    }

    private static final class CreateBacktankBridge {
        private static final String BACKTANK_UTIL = "com.simibubi.create.content.equipment.armor.BacktankUtil";

        private static List<ItemStack> getAllWithAir(Player player) {
            if (player == null) {
                return Collections.emptyList();
            }
            try {
                Class<?> bridge = Class.forName(BACKTANK_UTIL);
                Method method = bridge.getMethod("getAllWithAir", net.minecraft.world.entity.LivingEntity.class);
                Object value = method.invoke(null, player);
                if (value instanceof List<?> list) {
                    return list.stream()
                            .filter(ItemStack.class::isInstance)
                            .map(ItemStack.class::cast)
                            .toList();
                }
            } catch (ReflectiveOperationException ignored) {
                return Collections.emptyList();
            }
            return Collections.emptyList();
        }

        private static int maxAir(ItemStack stack) {
            try {
                Class<?> bridge = Class.forName(BACKTANK_UTIL);
                Method method = bridge.getMethod("maxAir", ItemStack.class);
                Object value = method.invoke(null, stack);
                return value instanceof Integer result ? result : 0;
            } catch (ReflectiveOperationException ignored) {
                return 0;
            }
        }

        private static float getAir(ItemStack stack) {
            try {
                Class<?> bridge = Class.forName(BACKTANK_UTIL);
                Method method = bridge.getMethod("getAir", ItemStack.class);
                Object value = method.invoke(null, stack);
                return value instanceof Number result ? result.floatValue() : 0.0F;
            } catch (ReflectiveOperationException ignored) {
                return 0.0F;
            }
        }

        private static int getBarColor(ItemStack stack, int bars) {
            try {
                Class<?> bridge = Class.forName(BACKTANK_UTIL);
                Method method = bridge.getMethod("getBarColor", ItemStack.class, int.class);
                Object value = method.invoke(null, stack, bars);
                return value instanceof Integer result ? result : Mth.hsvToRgb(1.0F, 1.0F, 1.0F);
            } catch (ReflectiveOperationException ignored) {
                return Mth.hsvToRgb(1.0F, 1.0F, 1.0F);
            }
        }
    }
}
