package top.ribs.scguns.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Short-lived soul fire left behind by soul-fuel fire sprayers. It never spreads, and it always burns out:
 * like vanilla fire it re-schedules its own tick and ages every tick, removing itself at max age. A random
 * tick re-arms the scheduled tick if it was ever lost, so a placed block can never sit there forever.
 */
public class FakeSoulFireBlock extends BaseFireBlock {
    public static final MapCodec<FakeSoulFireBlock> CODEC = simpleCodec(properties -> new FakeSoulFireBlock(properties, 2.0F));
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public FakeSoulFireBlock(BlockBehaviour.Properties properties, float fireDamage) {
        super(properties, fireDamage);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return false;
    }

    private static int getTickDelay(RandomSource random) {
        return 12 + random.nextInt(8);
    }

    /**
     * Needs a real floor: something with a collision shape that is not itself fire. Fire has no collision,
     * so this also forbids sitting on top of any other fire block. It also gives way if fire sits above it.
     */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.getBlock() instanceof BaseFireBlock || level.getBlockState(pos.above()).getBlock() instanceof BaseFireBlock) {
            return false;
        }
        return belowState.isFaceSturdy(level, belowPos, Direction.UP) || !belowState.getCollisionShape(level, belowPos).isEmpty();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return this.canSurvive(state, level, currentPos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        return this.canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.getBlockState(pos).is(this)) {
            level.scheduleTick(pos, this, getTickDelay(level.random));
        }
    }

    /** Resets the burn timer, used when the same fire is sprayed again. */
    public static void refresh(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof FakeSoulFireBlock block) {
            if (state.getValue(AGE) != 0) {
                level.setBlock(pos, state.setValue(AGE, 0), Block.UPDATE_CLIENTS);
            }
            level.scheduleTick(pos, block, getTickDelay(level.random));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos) || level.isRainingAt(pos) || level.isRainingAt(pos.above())) {
            level.removeBlock(pos, false);
            return;
        }
        int age = state.getValue(AGE);
        if (age >= MAX_AGE) {
            level.removeBlock(pos, false);
            return;
        }
        level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.scheduleTick(pos, this, getTickDelay(random));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            this.tick(state, level, pos, random);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.fireImmune()) {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0) {
                entity.igniteForSeconds(8.0F);
            }

            entity.hurt(level.damageSources().inFire(), 2.0F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    1.0F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.3F,
                    false
            );
        }

        for (int i = 0; i < random.nextInt(2) + 2; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5 + 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        }

        if (random.nextInt(5) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.5 + 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SOUL, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
