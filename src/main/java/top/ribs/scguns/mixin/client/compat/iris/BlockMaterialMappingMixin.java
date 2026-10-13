package top.ribs.scguns.mixin.client.compat.iris;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.compat.IrisBlockIdCompat;

/**
 * Iris turns the shader pack's block.properties into a BlockState -> material id map here; we append our blocks
 * (see {@link IrisBlockIdCompat}). Optional: only applied when Iris is installed, and skipped if the method changes.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.shaderpack.materialmap.BlockMaterialMapping", remap = false)
public abstract class BlockMaterialMappingMixin {
    @Inject(method = "createBlockStateIdMap", at = @At("RETURN"), require = 0, remap = false)
    private static void scguns$addBlockIds(Int2ObjectLinkedOpenHashMap<?> blockPropertiesMap, Int2ObjectLinkedOpenHashMap<?> tagPropertiesMap,
                                           CallbackInfoReturnable<Object2IntMap<BlockState>> cir) {
        IrisBlockIdCompat.apply(blockPropertiesMap, cir.getReturnValue());
    }
}
