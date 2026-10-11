package top.ribs.scguns.init;

import net.minecraft.core.registries.Registries;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.ribs.scguns.blockentity.*;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "scguns");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GunShelfBlockEntity>> GUN_SHELF_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("gun_shelf", () ->
                    BlockEntityType.Builder.of(GunShelfBlockEntity::new,
                            ModBlocks.GUN_SHELF.get()).build(null));










    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}






