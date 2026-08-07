package dg.projectbunker.data;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BunkerBlastDoorBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<BunkerBlastDoorBlock> CODEC = simpleCodec(BunkerBlastDoorBlock::new);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public BunkerBlastDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1
                && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite())
                    .setValue(OPEN, false)
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        if (!level.isClientSide) {
            BlockPos abovePos = pos.above();
            if (level.getBlockState(abovePos).canBeReplaced()) {
                level.setBlock(abovePos, state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
            }

            if (placer instanceof Player player) {
                player.displayClientMessage(
                        Component.literal("Гермодверь установлена.")
                                .withStyle(ChatFormatting.GREEN), true);
            }
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(OPEN)) {
            Direction facing = state.getValue(FACING);
            return (facing == Direction.NORTH || facing == Direction.SOUTH)
                    ? Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D)
                    : Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);
        }
        return Shapes.block();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherHalfPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            if (level.getBlockState(otherHalfPos).is(this)) {
                level.setBlock(otherHalfPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide) {
            boolean isCurrentlyOpen = state.getValue(OPEN);
            Direction facing = state.getValue(FACING);
            DoubleBlockHalf half = state.getValue(HALF);
            boolean nextOpenState = !isCurrentlyOpen;

            BlockPos masterPos = half == DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockPos slavePos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();

            BlockState masterState = level.getBlockState(masterPos);
            if (masterState.is(this)) {
                level.setBlock(masterPos, masterState.setValue(OPEN, nextOpenState), 3);
            }
            BlockState slaveState = level.getBlockState(slavePos);
            if (slaveState.is(this)) {
                level.setBlock(slavePos, slaveState.setValue(OPEN, nextOpenState), 3);
            }

            float pitch = level.getRandom().nextFloat() * 0.1F + 0.9F;
            var sound = nextOpenState
                    ? net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN
                    : net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE;
            level.playSound(null, pos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, pitch);

            if (!nextOpenState) {
                player.displayClientMessage(
                        Component.literal("Гермодверь закрыта.")
                                .withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(
                        Component.literal("Гермодверь открыта.")
                                .withStyle(ChatFormatting.YELLOW), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}