package top.ribs.scguns.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.init.ModItems;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Anvil repair materials for guns. Each gun is listed in a "scguns:repair_material/&lt;material&gt;" item tag
 * (generated from the frame it is crafted with); anything not listed, including attachments, repairs with iron.
 */
public final class GunRepairMaterials {
    private static final Map<TagKey<Item>, Predicate<ItemStack>> MATERIALS = Map.of(
            tag("copper"), stack -> stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/copper"))),
            tag("iron"), GunRepairMaterials::isIron,
            tag("anthralite"), stack -> stack.is(ModItems.ANTHRALITE_INGOT.get()),
            tag("treated_iron"), stack -> stack.is(ModItems.TREATED_IRON_INGOT.get()),
            tag("treated_brass"), stack -> stack.is(ModItems.TREATED_BRASS_INGOT.get()),
            tag("diamond_steel"), stack -> stack.is(ModItems.DIAMOND_STEEL_INGOT.get()),
            tag("scorched"), stack -> stack.is(ModItems.SCORCHED_INGOT.get())
    );

    private GunRepairMaterials() {
    }

    public static boolean isValidRepair(ItemStack toRepair, ItemStack repair) {
        for (Map.Entry<TagKey<Item>, Predicate<ItemStack>> entry : MATERIALS.entrySet()) {
            if (toRepair.is(entry.getKey())) {
                return entry.getValue().test(repair);
            }
        }
        return isIron(repair);
    }

    private static boolean isIron(ItemStack stack) {
        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/iron")));
    }

    private static TagKey<Item> tag(String material) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("scguns", "repair_material/" + material));
    }
}
