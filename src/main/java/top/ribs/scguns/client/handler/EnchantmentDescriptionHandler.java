package top.ribs.scguns.client.handler;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import top.ribs.scguns.Reference;

import java.util.List;
import java.util.Optional;

/**
 * Shows "enchantment.&lt;namespace&gt;.&lt;id&gt;.desc" translations under each enchantment in item tooltips.
 * Descriptions are always visible on enchanted books and shown on other items while Shift is held.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class EnchantmentDescriptionHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        boolean book = stack.is(Items.ENCHANTED_BOOK);
        ItemEnchantments enchantments = book
                ? stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                : stack.getTagEnchantments();
        if (enchantments.isEmpty()) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        boolean show = book || Screen.hasShiftDown();
        boolean anyDescription = false;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Optional<ResourceKey<Enchantment>> key = entry.getKey().unwrapKey();
            if (key.isEmpty()) {
                continue;
            }
            String descKey = "enchantment." + key.get().location().getNamespace() + "." + key.get().location().getPath() + ".desc";
            if (!I18n.exists(descKey)) {
                continue;
            }
            anyDescription = true;
            if (!show) {
                continue;
            }
            String name = Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString();
            for (int i = 0; i < tooltip.size(); i++) {
                if (tooltip.get(i).getString().equals(name)) {
                    tooltip.add(i + 1, Component.literal(" ").append(Component.translatable(descKey)).withStyle(ChatFormatting.DARK_GRAY));
                    break;
                }
            }
        }

        if (anyDescription && !show) {
            tooltip.add(Component.translatable("tooltip.scguns.enchantment_descriptions.hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
