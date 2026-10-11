package top.ribs.scguns.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Molds are used in a Create basin under a mechanical press. Every press consumes one point of durability
 * (returned to the basin as a crafting remainder). Molds accept Unbreaking and Mending, and a Create spout
 * filled with Create: Enchantment Industry liquid experience repairs them like Mending does.
 */
public class MoldItem extends Item {
    public static final int DURABILITY = 1024;

    public MoldItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack remainingItem = stack.copy();
        remainingItem.setCount(1);
        int unbreaking = getUnbreakingLevel(remainingItem);
        // Same odds as vanilla Unbreaking: a 1 / (level + 1) chance to actually take damage
        if (unbreaking > 0 && ThreadLocalRandom.current().nextInt(unbreaking + 1) > 0) {
            return remainingItem;
        }
        remainingItem.setDamageValue(remainingItem.getDamageValue() + 1);
        if (remainingItem.getDamageValue() >= remainingItem.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return remainingItem;
    }

    private static int getUnbreakingLevel(ItemStack stack) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getTagEnchantments().entrySet()) {
            if (entry.getKey().is(Enchantments.UNBREAKING)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    /**
     * Lets a Create spout pour liquid experience onto a damaged mold. One mB of experience is one experience point,
     * which repairs two durability, matching Mending.
     */
    public static class ExperienceRepairHandler implements IFluidHandlerItem {
        private static final ResourceLocation CEI_EXPERIENCE = ResourceLocation.fromNamespaceAndPath("create_enchantment_industry", "experience");
        private static final TagKey<Fluid> EXPERIENCE_TAG = FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", "experience"));
        private static final int DURABILITY_PER_MB = 2;

        private final ItemStack container;

        public ExperienceRepairHandler(ItemStack container) {
            this.container = container;
        }

        public static boolean isExperience(FluidStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            return stack.is(EXPERIENCE_TAG) || CEI_EXPERIENCE.equals(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
        }

        private int getRequiredAmount() {
            return (this.container.getDamageValue() + DURABILITY_PER_MB - 1) / DURABILITY_PER_MB;
        }

        @Override
        public @NotNull ItemStack getContainer() {
            return this.container;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return getRequiredAmount();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return isExperience(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (this.container.getCount() != 1 || !isExperience(resource)) {
                return 0;
            }
            int amount = Math.min(resource.getAmount(), getRequiredAmount());
            if (amount <= 0) {
                return 0;
            }
            if (action.execute()) {
                this.container.setDamageValue(Math.max(0, this.container.getDamageValue() - amount * DURABILITY_PER_MB));
            }
            return amount;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
