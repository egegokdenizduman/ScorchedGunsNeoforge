package top.ribs.scguns.client.compat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.init.ModBlocks;

/**
 * Gives our blocks shader-pack material ids when Iris builds its block id map (called from
 * {@code BlockMaterialMappingMixin}; does nothing without Iris).
 * <p>
 * Shader packs only know blocks that are listed in their own {@code block.properties}, which a mod cannot edit.
 * We therefore add our blocks to the map Iris built from that file, but never override an id the pack already
 * assigned to one of our blocks:
 * <ul>
 *     <li>Complementary Reimagined / Euphoria Patches: {@code block.10024} is "modded ores in stone" (the
 *     "Glowing Modded Ores" option detects ore pixels against the texture's border colour) and Euphoria adds
 *     {@code block.10026} for netherrack ores. Our ores join those lists, so they glow exactly when the user has
 *     glowing ores enabled and look like normal ores otherwise.</li>
 *     <li>Any pack: the scorched block copies whatever id the pack gives {@code minecraft:magma_block}, so its
 *     glowing cracks get the pack's magma emission.</li>
 * </ul>
 */
public final class IrisBlockIdCompat {
    private static final int COMPLEMENTARY_NETHER_QUARTZ_ORE = 10368;
    private static final int COMPLEMENTARY_MODDED_ORE_STONE = 10024;
    private static final int EUPHORIA_MODDED_ORE_NETHERRACK = 10026;

    private IrisBlockIdCompat() {
    }

    public static void apply(Int2ObjectMap<?> blockProperties, Object2IntMap<BlockState> ids) {
        if (ids == null) {
            return;
        }
        try {
            BlockState quartzOre = Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
            boolean complementary = ids.containsKey(quartzOre) && ids.getInt(quartzOre) == COMPLEMENTARY_NETHER_QUARTZ_ORE;
            if (complementary) {
                int netherrackOres = blockProperties != null && blockProperties.containsKey(EUPHORIA_MODDED_ORE_NETHERRACK)
                        ? EUPHORIA_MODDED_ORE_NETHERRACK : COMPLEMENTARY_MODDED_ORE_STONE;
                put(ids, ModBlocks.ANTHRALITE_ORE.get(), COMPLEMENTARY_MODDED_ORE_STONE);
                put(ids, ModBlocks.DEEPSLATE_ANTHRALITE_ORE.get(), COMPLEMENTARY_MODDED_ORE_STONE);
                put(ids, ModBlocks.VEHEMENT_COAL_ORE.get(), netherrackOres);
            }
            BlockState magma = Blocks.MAGMA_BLOCK.defaultBlockState();
            if (ids.containsKey(magma)) {
                put(ids, ModBlocks.SCORCHED_BLOCK.get(), ids.getInt(magma));
            }
        } catch (RuntimeException | LinkageError e) {
            ScorchedGuns.LOGGER.debug("Could not add scguns blocks to the shader pack block id map", e);
        }
    }

    private static void put(Object2IntMap<BlockState> ids, Block block, int id) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            if (!ids.containsKey(state)) {
                ids.put(state, id);
            }
        }
    }
}
