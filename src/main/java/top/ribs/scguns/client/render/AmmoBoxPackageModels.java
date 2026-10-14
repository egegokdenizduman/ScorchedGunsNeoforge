package top.ribs.scguns.client.render;

import com.simibubi.create.AllPartialModels;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import top.ribs.scguns.Reference;

import java.util.List;

/**
 * Create draws placed, thrown and chain-carried packages from {@link AllPartialModels#PACKAGES}, keyed by
 * item id, and hangs them on chains with {@link AllPartialModels#PACKAGE_RIGGING}. Ammo boxes add their
 * static (empty-state) model and the 12x10 rigging there. Must run before models load (mod constructor).
 */
public final class AmmoBoxPackageModels {
    private static final List<String> BOXES = List.of(
            "pistol_ammo_box", "rifle_ammo_box", "shotgun_ammo_box", "magnum_ammo_box", "energy_ammo_box",
            "rocket_ammo_box", "special_ammo_box", "empty_casing_pouch", "creative_ammo_box");

    private AmmoBoxPackageModels() {
    }

    public static void init() {
        PartialModel rigging = PartialModel.of(ResourceLocation.fromNamespaceAndPath("create", "item/package/rigging_12x10"));
        for (String name : BOXES) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, name);
            AllPartialModels.PACKAGES.put(id, PartialModel.of(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "item/package/" + name)));
            AllPartialModels.PACKAGE_RIGGING.put(id, rigging);
        }
    }
}
