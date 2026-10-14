package top.ribs.scguns.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModItems;

/**
 * Arrows of Healing fired from the Crusader do no impact damage and barely push the target;
 * the potion itself still applies (so undead are still harmed by it).
 */
@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CrusaderArrowEvents {
    private static final float HEALING_ARROW_KNOCKBACK = 0.05F;

    // The hit currently being processed, so the knockback that follows it in the same hurt() call can be softened
    private static LivingEntity softKnockbackTarget;

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof AbstractArrow arrow && isCrusaderHealingArrow(arrow)) {
            // Keep the hit itself (so the arrow lands and applies its potion), just with no damage
            event.setAmount(0.0F);
            softKnockbackTarget = event.getEntity();
        } else {
            softKnockbackTarget = null;
        }
    }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity() == softKnockbackTarget) {
            softKnockbackTarget = null;
            event.setStrength(Math.min(event.getStrength(), HEALING_ARROW_KNOCKBACK));
        }
    }

    private static boolean isCrusaderHealingArrow(AbstractArrow arrow) {
        ItemStack weapon = arrow.getWeaponItem();
        if (weapon == null || !weapon.is(ModItems.CRUSADER.get())) {
            return false;
        }
        PotionContents potion = arrow.getPickupItemStackOrigin().get(DataComponents.POTION_CONTENTS);
        return potion != null && (potion.is(Potions.HEALING) || potion.is(Potions.STRONG_HEALING));
    }
}
