package top.ribs.scguns.client.render;

import com.simibubi.create.AllPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.Reference;
import top.ribs.scguns.item.AmmoBoxItem;
import top.ribs.scguns.item.ammo_boxes.CreativeAmmoBoxItem;

import java.util.List;

/**
 * Create draws placed, thrown and chain-carried packages from {@link AllPartialModels#PACKAGES}, keyed by
 * item id, and hangs them on chains with {@link AllPartialModels#PACKAGE_RIGGING}. Ammo boxes add their
 * empty model under the item id, plus partial and full models under {@code <box>_partial} / {@code <box>_full},
 * which the client package mixins pick via {@link #modelKey}. Must run before models load (mod constructor).
 */
public final class AmmoBoxPackageModels {
    private static final List<String> BOXES = List.of(
            "pistol_ammo_box", "rifle_ammo_box", "shotgun_ammo_box", "magnum_ammo_box", "energy_ammo_box",
            "rocket_ammo_box", "special_ammo_box", "empty_casing_pouch", "creative_ammo_box");
    private static final List<String> FILL_STATES = List.of("_partial", "_full");

    private AmmoBoxPackageModels() {
    }

    public static void init() {
        PartialModel rigging = PartialModel.of(ResourceLocation.fromNamespaceAndPath("create", "item/package/rigging_12x10"));
        for (String name : BOXES) {
            register(name, rigging);
            if (!name.equals("creative_ammo_box")) {
                for (String state : FILL_STATES) {
                    register(name + state, rigging);
                }
            }
        }
    }

    private static void register(String name, PartialModel rigging) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, name);
        AllPartialModels.PACKAGES.put(id, PartialModel.of(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "item/package/" + name)));
        AllPartialModels.PACKAGE_RIGGING.put(id, rigging);
    }

    /**
     * The {@link AllPartialModels#PACKAGES} key for a package stack: the fill-state key for a partly filled or
     * full ammo box (same thresholds as the {@code ammo_count} item property), otherwise {@code itemKey}.
     * A package's contents can't change while it's placed or on a chain, so callers only need to ask once.
     */
    public static ResourceLocation modelKey(ItemStack stack, ResourceLocation itemKey) {
        if (!(stack.getItem() instanceof AmmoBoxItem) || stack.getItem() instanceof CreativeAmmoBoxItem
                || !itemKey.getNamespace().equals(Reference.MOD_ID) || AmmoBoxItem.getUsedUnits(stack) <= 0) {
            return itemKey;
        }
        return itemKey.withSuffix(AmmoBoxItem.isFull(stack) ? "_full" : "_partial");
    }
}
