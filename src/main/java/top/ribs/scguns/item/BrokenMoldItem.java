package top.ribs.scguns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * A worn-out mold (like a broken elytra). It cannot be used in recipes, keeps its enchantments, and turns back into
 * its working mold as soon as any durability is restored (Mending orbs, liquid experience from a spout, anvil).
 * Tagged scguns:broken_mold so Create attribute filters can pull it out of a basin for repair.
 */
public class BrokenMoldItem extends Item {
    private final Supplier<? extends Item> functionalMold;

    public BrokenMoldItem(Properties properties, Supplier<? extends Item> functionalMold) {
        super(properties);
        this.functionalMold = functionalMold;
    }

    public Item getFunctionalMold() {
        return this.functionalMold.get();
    }

    /** Returns the working mold if this broken mold has been repaired at all, otherwise the stack itself. */
    public static ItemStack restoreIfRepaired(ItemStack stack) {
        if (stack.getItem() instanceof BrokenMoldItem broken && stack.getDamageValue() < stack.getMaxDamage()) {
            return stack.transmuteCopy(broken.getFunctionalMold(), stack.getCount());
        }
        return stack;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (level.isClientSide || stack.getDamageValue() >= stack.getMaxDamage()) {
            return;
        }
        ItemStack restored = restoreIfRepaired(stack);
        if (entity instanceof net.minecraft.world.entity.player.Player player && player.getInventory().getItem(slot) == stack) {
            player.getInventory().setItem(slot, restored);
        }
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return repair.getItem() == this.getFunctionalMold();
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.scguns.broken_mold.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
