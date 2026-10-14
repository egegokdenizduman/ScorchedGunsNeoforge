package top.ribs.scguns.item.animated;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import top.ribs.scguns.compat.CreateBacktank;

import java.util.List;

/**
 * The Crusader shows backtank air on its item bar while a backtank with air is worn (like the air guns),
 * and durability otherwise. The air itself is used in GunEventBus, the faster fire rate in CreateBacktank.
 */
public class CrusaderItem extends AnimatedGunItem {

    public CrusaderItem(Properties properties, String path,
                        SoundEvent reloadSoundMagOut,
                        SoundEvent reloadSoundMagIn,
                        SoundEvent reloadSoundEnd,
                        SoundEvent boltPullSound,
                        SoundEvent boltReleaseSound) {
        super(properties, path, reloadSoundMagOut, reloadSoundMagIn, reloadSoundEnd, boltPullSound, boltReleaseSound);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getBacktank() != null || super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        ItemStack backtank = getBacktank();
        if (backtank != null) {
            int maxAir = CreateBacktank.maxAir(backtank);
            return maxAir > 0 ? Math.round(13.0F * CreateBacktank.getAir(backtank) / maxAir) : 0;
        }
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        ItemStack backtank = getBacktank();
        return backtank != null ? CreateBacktank.getBarColor(backtank) : super.getBarColor(stack);
    }

    private static ItemStack getBacktank() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return null;
        }
        List<ItemStack> backtanks = CreateBacktank.getAllWithAir(getClientPlayer());
        return backtanks.isEmpty() ? null : backtanks.get(0);
    }

    @OnlyIn(Dist.CLIENT)
    private static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
