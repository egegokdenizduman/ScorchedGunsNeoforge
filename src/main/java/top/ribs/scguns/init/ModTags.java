package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import top.ribs.scguns.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class ModTags
{
    public static Map<ResourceLocation, TagKey<Block>> blockTagCache = new HashMap<>();

    public static class Blocks
    {
        public static final TagKey<Block> FRAGILE = tag("fragile");
        public static final TagKey<Block> SCULK_BLOCKS = tag("sculk_blocks");
        public static final TagKey<Block> TANK_BREAKABLE = tag("tank_breakable");
        public static final TagKey<Block> METAL_DETECTABLE = tag("metal_detectable");
        public static final TagKey<Block> SHOTBALL_PENETRABLE = tag("shotball_penetrable");
        private static TagKey<Block> tag(String name)
        {
            return BlockTags.create(Reference.id(name));
        }
        private static TagKey<Block> tag()
        {
            return BlockTags.create(Reference.id("fragile"));
        }
    }

    public static class Items
    {
        public static final TagKey<Item> DOES_NOT_EJECT_CASINGS = tag("does_not_eject_casings");
        public static final TagKey<Item> SINGLE_SHOT = tag("single_shot");
        public static final TagKey<Item> NON_COLLATERAL = tag("non_collateral");
        public static final TagKey<Item> NON_UNDERWATER = tag("non_underwater");
        public static final TagKey<Item> ONE_HANDED_CARBINE  = tag("one_handed_carbine");
        public static final TagKey<Item> HEAVY_WEAPON = tag("heavy_weapon");
        public static final TagKey<Item> OCEAN_GUN = tag("ocean_gun");
        public static final TagKey<Item> PIGLIN_GUN = tag("piglin_gun");
        public static final TagKey<Item> BUILT_IN_BAYONET  = tag("built_in_bayonet");
        public static final TagKey<Item> WEAK_FILTER = tag("weak_filter");
        public static final TagKey<Item> STRONG_FILTER = tag("strong_filter");
        public static final TagKey<Item> WEAK_COMPOST = tag("weak_compost");
        public static final TagKey<Item> NORMAL_COMPOST = tag("normal_compost");
        public static final TagKey<Item> STRONG_COMPOST = tag("strong_compost");
        public static final TagKey<Item> COMPOST_DROPS= tag("compost_drops");
        public static final TagKey<Item> GAS_MASK = tag("gas_mask");
        public static final TagKey<Item> FLASH_PROTECTION = tag("flash_protection");
        public static final TagKey<Item> EXPLOSIVE_BLOCK = tag("explosive_block");
        public static final TagKey<Item> GEOTHERMAL_VENT_OUTPUT = tag("geothermal_vent_output");
        public static final TagKey<Item> SULFUR_VENT_OUTPUT = tag("sulfur_vent_output");
        public static final TagKey<Item> MINING_GUN = tag("mining_gun");
        public static final TagKey<Item> ANTIQUE_GUN_TIER = tag("antique_gun_tier");
        public static final TagKey<Item> FRONTIER_GUN_TIER = tag("frontier_gun_tier");
        public static final TagKey<Item> COPPER_GUN_TIER = tag("copper_gun_tier");
        public static final TagKey<Item> IRON_GUN_TIER = tag("iron_gun_tier");
        public static final TagKey<Item> WRECKER_GUN_TIER = tag("wrecker_gun_tier");
        public static final TagKey<Item> OCEAN_GUN_TIER = tag("ocean_gun_tier");
        public static final TagKey<Item> DIAMOND_STEEL_GUN_TIER = tag("diamond_steel_gun_tier");
        public static final TagKey<Item> TREATED_BRASS_GUN_TIER = tag("treated_brass_gun_tier");
        public static final TagKey<Item> PIGLIN_GUN_TIER = tag("piglin_gun_tier");
        public static final TagKey<Item> DEEP_DARK_GUN_TIER = tag("deep_dark_gun_tier");
        public static final TagKey<Item> END_GUN_TIER = tag("end_gun_tier");
        public static final TagKey<Item> SCORCHED_GUN_TIER = tag("scorched_gun_tier");
        public static final TagKey<Item> GRENADES = tag("grenades");
        public static final TagKey<Item> VIVENTRUM_BANNED_ITEMS = tag("viventrum_banned_items");

        private static TagKey<Item> tag(String name)
        {
            return ItemTags.create(Reference.id(name));
        }
    }
    public static class Entities
    {

        public static final TagKey<EntityType<?>> RED_BLOOD = tag("red_blood");
        public static final TagKey<EntityType<?>> WHITE_BLOOD = tag("white_blood");
        public static final TagKey<EntityType<?>> GREEN_BLOOD = tag("green_blood");
        public static final TagKey<EntityType<?>> BLUE_BLOOD = tag("blue_blood");
        public static final TagKey<EntityType<?>> YELLOW_BLOOD = tag("yellow_blood");
        public static final TagKey<EntityType<?>> PURPLE_BLOOD = tag("purple_blood");
        public static final TagKey<EntityType<?>> BLACK_BLOOD = tag("black_blood");

        public static final TagKey<EntityType<?>> NON_SWARM_TARGETED = tag("non_swarm_targeted");
        public static final TagKey<EntityType<?>> FLEEING_FROM_GUNS = tag("fleeing_from_guns");
        public static final TagKey<EntityType<?>> AGGRO_FROM_GUNS = tag("aggro_from_guns");


        public static final TagKey<EntityType<?>> NONE = tag("none");
        public static final TagKey<EntityType<?>> HEAVY = tag("heavy");
        public static final TagKey<EntityType<?>> VERY_HEAVY = tag("very_heavy");
        public static final TagKey<EntityType<?>> UNDEAD = tag("undead");
        public static final TagKey<EntityType<?>> GHOST = tag("ghost");
        public static final TagKey<EntityType<?>> WITHER = tag("wither");
        public static final TagKey<EntityType<?>> FIRE = tag("fire");
        public static final TagKey<EntityType<?>> ILLAGER = tag("illager");
        public static final TagKey<EntityType<?>> BOT = tag("bot");
        public static final TagKey<EntityType<?>> WATER = tag("water");
        public static final TagKey<EntityType<?>> TURRET_BLACKLIST = tag("turret_blacklist");
        public static final TagKey<EntityType<?>> TURRET_ENEMY_WHITELIST = tag("turret_enemy_whitelist");
        public static final TagKey<EntityType<?>> GUNNER = tag("gunner");
        public static final TagKey<EntityType<?>> IGNORES_MINE_UNITS = tag("ignores_mine_units");

        public static TagKey<EntityType<?>> tag(String name)
        {
            return TagKey.create(Registries.ENTITY_TYPE,Reference.id(name));
        }
    }
}




