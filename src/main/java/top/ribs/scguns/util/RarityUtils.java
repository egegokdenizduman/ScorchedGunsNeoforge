package top.ribs.scguns.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.HashMap;
import java.util.Map;


public class RarityUtils {

    private static final Map<ResourceLocation, String> ITEM_RARITY_MAP = new HashMap<>();

    static {
        // Add your custom items and their rarities here
        ITEM_RARITY_MAP.put(scguns("floundergat"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(scguns("marlin"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(scguns("bomb_lance"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(scguns("ocean_blueprint"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(scguns("sequoia"), Constants.OCEANIC);
        ITEM_RARITY_MAP.put(scguns("hullbreaker"), Constants.OCEANIC);

        ITEM_RARITY_MAP.put(scguns("super_shotgun"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(scguns("blasphemy"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(scguns("freyr"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(scguns("pyroclastic_flow"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(scguns("vulcanic_repeater"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(scguns("piglin_blueprint"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(scguns("mangalitsa"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(scguns("trotters"), Constants.PIGLISH);
        ITEM_RARITY_MAP.put(scguns("hog_round"), Constants.PIGLISH);

        ITEM_RARITY_MAP.put(scguns("whispers"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(scguns("echoes_2"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(scguns("sculk_resonator"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(scguns("deep_dark_blueprint"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(scguns("forlorn_hope"), Constants.DEEP_DARK);
        ITEM_RARITY_MAP.put(scguns("sculk_cell"), Constants.DEEP_DARK);

        ITEM_RARITY_MAP.put(scguns("end_blueprint"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(scguns("lone_wonder"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(scguns("raygun"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(scguns("dark_matter"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(scguns("shellurker"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(scguns("carapice"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(scguns("weevil"), Constants.ENDISH);
        ITEM_RARITY_MAP.put(scguns("shulkshot"), Constants.ENDISH);

        ITEM_RARITY_MAP.put(scguns("scorched_blueprint"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("scorched_ingot"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("earths_corpse"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("rat_king_and_queen"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("locust"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("sterilizer"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("newborn_cyst"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("astella"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("flayed_god"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("nervepinch"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("prima_materia"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("terra_incognita"), Constants.SCORCHED);

        ITEM_RARITY_MAP.put(scguns("advanced_exo_suit_core"), Constants.SCORCHED);
        ITEM_RARITY_MAP.put(scguns("the_pact"), Constants.SCORCHED);

        ITEM_RARITY_MAP.put(scguns("felix_memorial"), Constants.PIGLISH);

        ITEM_RARITY_MAP.put(scguns("ultra_knight_hawk"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(scguns("big_bore"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(scguns("kiln_gun"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(scguns("whizzbanger"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(scguns("osborne_slug"), Constants.BIZARRE);
        ITEM_RARITY_MAP.put(scguns("ribs_glory"), Constants.PIGLISH);

    }



    public static Rarity GetRarityFromResourceLocation(ResourceLocation location, Rarity oldRarity) {
        String rarityName = ITEM_RARITY_MAP.get(location);
        return rarityName != null ? getRarity(rarityName, oldRarity) : oldRarity;
    }

    public static Rarity GetRarityFromItem(Item item, Rarity old) {
        var items = BuiltInRegistries.ITEM;
        if (items.containsValue(item)) {
            return GetRarityFromResourceLocation(items.getKey(item), old);
        }
        return old;
    }

    private static ResourceLocation scguns(String path) {
        return ResourceLocation.fromNamespaceAndPath("scguns", path);
    }

    private static Rarity getRarity(String rarityName, Rarity fallback) {
        try {
            return Rarity.valueOf(rarityName);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    private static final class Constants {
        private static final String OCEANIC = "SCGUNS_OCEANIC";
        private static final String UNIQUE = "SCGUNS_UNIQUE";
        private static final String PIGLISH = "SCGUNS_PIGLISH";
        private static final String SCORCHED = "SCGUNS_SCORCHED";
        private static final String DEEP_DARK = "SCGUNS_DEEP_DARK";
        private static final String ENDISH = "SCGUNS_ENDISH";
        private static final String BIZARRE = "SCGUNS_BIZARRE";
    }
}
