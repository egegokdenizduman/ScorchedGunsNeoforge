package top.ribs.scguns.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import top.ribs.scguns.entity.throwable.ThrowableGrenadeEntity;
import top.ribs.scguns.entity.throwable.ThrowableSoulMolotovCocktailEntity;

/**
 * Molotov variant that spreads soul fire. Same cook / throw handling as {@link MolotovCocktailItem}.
 */
public class SoulMolotovCocktailItem extends MolotovCocktailItem
{
    public SoulMolotovCocktailItem(Properties properties, int maxCookTime)
    {
        super(properties, maxCookTime);
    }

    @Override
    public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft)
    {
        return new ThrowableSoulMolotovCocktailEntity(world, entity, timeLeft);
    }
}
