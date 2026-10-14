package top.ribs.scguns.compat;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.init.ModItems;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Reflection bridge to Create's BacktankUtil, plus the Crusader's backtank boost:
 * with backtank air it fires 2.5x faster and each shot uses air.
 */
public final class CreateBacktank {
    private static final String BACKTANK_UTIL = "com.simibubi.create.content.equipment.armor.BacktankUtil";
    private static final float CRUSADER_RATE_BOOST = 2.5F;

    private CreateBacktank() {
    }

    public static boolean isCrusaderBoosted(Player player, ItemStack weapon) {
        return player != null && weapon.is(ModItems.CRUSADER.get()) && !getAllWithAir(player).isEmpty();
    }

    public static int getCrusaderRate(Player player, ItemStack weapon, int rate) {
        return isCrusaderBoosted(player, weapon) ? Math.max(1, Math.round(rate / CRUSADER_RATE_BOOST)) : rate;
    }

    public static List<ItemStack> getAllWithAir(Player player) {
        if (player == null || !ScorchedGuns.createLoaded) {
            return Collections.emptyList();
        }
        try {
            Method method = Class.forName(BACKTANK_UTIL).getMethod("getAllWithAir", LivingEntity.class);
            if (method.invoke(null, player) instanceof List<?> list) {
                return list.stream().filter(ItemStack.class::isInstance).map(ItemStack.class::cast).toList();
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Collections.emptyList();
    }

    public static void consumeAir(Player player, ItemStack backtank, int amount) {
        try {
            Method method = Class.forName(BACKTANK_UTIL).getMethod("consumeAir", LivingEntity.class, ItemStack.class, int.class);
            method.invoke(null, player, backtank, Math.max(1, amount));
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static int maxAir(ItemStack backtank) {
        try {
            Method method = Class.forName(BACKTANK_UTIL).getMethod("maxAir", ItemStack.class);
            return method.invoke(null, backtank) instanceof Integer result ? result : 0;
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    public static float getAir(ItemStack backtank) {
        try {
            Method method = Class.forName(BACKTANK_UTIL).getMethod("getAir", ItemStack.class);
            return method.invoke(null, backtank) instanceof Number result ? result.floatValue() : 0.0F;
        } catch (ReflectiveOperationException ignored) {
            return 0.0F;
        }
    }

    public static int getBarColor(ItemStack backtank) {
        try {
            Method method = Class.forName(BACKTANK_UTIL).getMethod("getBarColor", ItemStack.class, int.class);
            return method.invoke(null, backtank, 1) instanceof Integer result ? result : Mth.hsvToRgb(1.0F, 1.0F, 1.0F);
        } catch (ReflectiveOperationException ignored) {
            return Mth.hsvToRgb(1.0F, 1.0F, 1.0F);
        }
    }
}
