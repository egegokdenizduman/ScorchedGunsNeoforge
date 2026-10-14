package top.ribs.scguns.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import top.ribs.scguns.Reference;
import top.ribs.scguns.init.ModItems;

/**
 * Arrows of Healing fired from the Crusader aren't an attack: no damage, no hurt flash, no anger or
 * villager reputation loss. The potion still applies (so undead are still harmed by it) and the target
 * is pushed only very slightly.
 */
@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CrusaderArrowEvents {
    private static final float HEALING_ARROW_KNOCKBACK = 0.05F;

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow) || arrow.level().isClientSide()) {
            return;
        }
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)) {
            return;
        }
        PotionContents potion = getCrusaderHealingPotion(arrow);
        if (potion == null) {
            return;
        }
        // Skip vanilla's hit (which goes through hurt()) and apply the arrow's effects the way Arrow#doPostHurtEffects does
        event.setCanceled(true);
        for (MobEffectInstance effect : potion.getAllEffects()) {
            target.addEffect(new MobEffectInstance(effect.getEffect(), Math.max(effect.mapDuration(d -> d / 8), 1),
                    effect.getAmplifier(), effect.isAmbient(), effect.isVisible()), arrow.getEffectSource());
        }
        Vec3 motion = arrow.getDeltaMovement();
        target.knockback(HEALING_ARROW_KNOCKBACK, -motion.x, -motion.z);
        arrow.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (arrow.getRandom().nextFloat() * 0.2F + 0.9F));
        arrow.discard();
    }

    private static PotionContents getCrusaderHealingPotion(AbstractArrow arrow) {
        ItemStack weapon = arrow.getWeaponItem();
        if (weapon == null || !weapon.is(ModItems.CRUSADER.get())) {
            return null;
        }
        PotionContents potion = arrow.getPickupItemStackOrigin().get(DataComponents.POTION_CONTENTS);
        return potion != null && (potion.is(Potions.HEALING) || potion.is(Potions.STRONG_HEALING)) ? potion : null;
    }
}
