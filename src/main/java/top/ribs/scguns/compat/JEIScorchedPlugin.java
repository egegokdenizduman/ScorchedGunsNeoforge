package top.ribs.scguns.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.GunRepairMaterials;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.*;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEIScorchedPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {

    }
    public static MutableComponent getTranslation(String key, Object... args) {
        return Component.translatable("scguns." + key, args);
    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        registration.addIngredientInfo(new ItemStack(ModItems.BLASPHEMY.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.blasphemy"));
        registration.addIngredientInfo(new ItemStack(ModItems.PISTOL_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.pistol_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.RIFLE_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.rifle_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.SHOTGUN_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.shotgun_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.MAGNUM_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.magnum_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.ENERGY_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.energy_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.ROCKET_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.rocket_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.SPECIAL_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.special_ammo_box"));
        registration.addIngredientInfo(new ItemStack(ModItems.EMPTY_CASING_POUCH.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.empty_casing_pouch"));
        registration.addIngredientInfo(new ItemStack(ModItems.CREATIVE_AMMO_BOX.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.creative_ammo_box"));


        registration.addIngredientInfo(new ItemStack(ModItems.SUPER_SHOTGUN.get()), VanillaTypes.ITEM_STACK, getTranslation("jei.info.super_shotgun"));

        // Anvil repairs: each gun is repaired with the ingot of its tier
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        List<IJeiAnvilRecipe> repairs = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof GunItem)) {
                continue;
            }
            ItemStack gun = new ItemStack(item);
            if (!gun.isDamageableItem()) {
                continue;
            }
            List<ItemStack> materials = GunRepairMaterials.getRepairItems(gun);
            if (materials.isEmpty()) {
                continue;
            }
            ItemStack damaged = gun.copy();
            damaged.setDamageValue(damaged.getMaxDamage() * 3 / 4);
            ItemStack repaired = gun.copy();
            repaired.setDamageValue(repaired.getMaxDamage() / 2);
            repairs.add(factory.createAnvilRecipe(damaged, materials, List.of(repaired)));
        }
        registration.addRecipes(RecipeTypes.ANVIL, repairs);







    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    }
}
