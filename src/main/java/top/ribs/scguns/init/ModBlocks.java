package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import top.ribs.scguns.Reference;
import top.ribs.scguns.block.*;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModBlocks {

    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(Reference.MOD_ID);
    public static final DeferredBlock<Block> FAKE_SOUL_FIRE = REGISTER.register("fake_soul_fire",
            () -> new FakeSoulFireBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_FIRE), 2.0F));
    public static final DeferredBlock<Block> TEMPORARY_LIGHT = REGISTER.register("temporary_light",
            TemporaryLightBlock::new);
    public static final DeferredBlock<Block> GUN_SHELF = register("gun_shelf",
            () -> new GunShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .strength(0.5F)));






    public static final DeferredBlock<Block> ANTHRALITE_ORE = register("anthralite_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final DeferredBlock<Block> DEEPSLATE_ANTHRALITE_ORE = register("deepslate_anthralite_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 0), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)));



    public static final DeferredBlock<Block> VEHEMENT_COAL_ORE = register("vehement_coal_ore",
            () -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.NETHER_GOLD_ORE)));





















    private static <T extends Block> DeferredBlock<T> register(String id, Supplier<T> blockSupplier) {
        return register(id, blockSupplier, block1 -> new BlockItem(block1, new Item.Properties()));
    }

    private static <T extends Block> DeferredBlock<T> register(String id, Supplier<T> blockSupplier, @Nullable Function<T, BlockItem> supplier) {
        DeferredBlock<T> registryObject = REGISTER.register(id, blockSupplier);
        if (supplier != null) {
            ModItems.REGISTER.register(id, () -> supplier.apply(registryObject.get()));
        }
        return registryObject;
    }

    private static <T extends Block> DeferredBlock<T> registerBurnable(String id, Supplier<T> blockSupplier, int burnTime) {
        return register(id, blockSupplier, block -> new BlockItem(block, new Item.Properties()) {
            @Override
            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                return burnTime;
            }
        });
    }
}





