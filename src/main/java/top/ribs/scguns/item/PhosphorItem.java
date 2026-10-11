package top.ribs.scguns.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.ribs.scguns.init.ModBlocks;

public class PhosphorItem extends Item {
    private static final int FERTILIZER_PASSES = 3;
    private static final float PASS_SUCCESS_CHANCE = 0.6F;

    public PhosphorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof BonemealableBlock && applyPhosphorFertilizer(stack, level, pos)) {
            if (!level.isClientSide) {
                level.levelEvent(1505, pos, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    public static boolean applyPhosphorFertilizer(ItemStack stack, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BonemealableBlock bonemealableBlock) || !bonemealableBlock.isValidBonemealTarget(level, pos, state)) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = level.getRandom();
            for (int i = 0; i < FERTILIZER_PASSES; i++) {
                BlockState currentState = level.getBlockState(pos);
                if (!(currentState.getBlock() instanceof BonemealableBlock currentBonemealable)) {
                    break;
                }
                if (!currentBonemealable.isValidBonemealTarget(level, pos, currentState)) {
                    break;
                }
                if (random.nextFloat() >= PASS_SUCCESS_CHANCE) {
                    break;
                }
                if (currentBonemealable.isBonemealSuccess(level, random, pos, currentState)) {
                    currentBonemealable.performBonemeal(serverLevel, random, pos, currentState);
                    if (i > 0) {
                        level.levelEvent(1505, pos, 0);
                    }
                }
            }
            stack.shrink(1);
        }
        return true;
    }
}
