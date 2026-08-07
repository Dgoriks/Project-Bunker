package dg.projectbunker.data;

import com.mojang.serialization.MapCodec;
import dg.projectbunker.event.BunkerZoneManager;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BunkerInternalGateBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<BunkerInternalGateBlock> CODEC = simpleCodec(BunkerInternalGateBlock::new);
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    private static final VoxelShape NORTH_SOUTH_OPEN_SHAPE = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);
    private static final VoxelShape EAST_WEST_OPEN_SHAPE = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);

    public BunkerInternalGateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(OPEN, false);
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
                    ? NORTH_SOUTH_OPEN_SHAPE
                    : EAST_WEST_OPEN_SHAPE;
        }
        return Shapes.block();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            Direction sideDir = (facing == Direction.NORTH || facing == Direction.SOUTH)
                    ? Direction.EAST
                    : Direction.NORTH;

            for (int h = -1; h <= 1; h++) {
                for (int v = -1; v <= 1; v++) {
                    if (h == 0 && v == 0) continue;
                    BlockPos targetPos = pos.relative(sideDir, h).above(v);
                    if (level.getBlockState(targetPos).canBeReplaced()) {
                        level.setBlock(targetPos, state, 3);
                    }
                }
            }

            // Закрытый шлюз установлен — сканируем герметичность
            BunkerZoneManager.onGateClosed(level, pos, facing);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            Direction facing = state.getValue(FACING);
            Direction sideDir = (facing == Direction.NORTH || facing == Direction.SOUTH)
                    ? Direction.EAST
                    : Direction.NORTH;

            for (int h = -2; h <= 2; h++) {
                for (int v = -2; v <= 2; v++) {
                    BlockPos checkPos = pos.relative(sideDir, h).above(v);
                    BlockState targetState = level.getBlockState(checkPos);
                    if (targetState.is(this) && targetState.getValue(FACING) == facing) {
                        level.removeBlock(checkPos, false);
                    }
                }
            }
            // Удаление шлюза = открытие зоны
            BunkerZoneManager.onGateOpened(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide) {
            boolean isCurrentlyOpen = state.getValue(OPEN);
            Direction facing = state.getValue(FACING);
            Direction sideDir = (facing == Direction.NORTH || facing == Direction.SOUTH)
                    ? Direction.EAST
                    : Direction.NORTH;
            boolean nextOpenState = !isCurrentlyOpen;

            for (int h = -2; h <= 2; h++) {
                for (int v = -2; v <= 2; v++) {
                    BlockPos checkPos = pos.relative(sideDir, h).above(v);
                    BlockState neighborState = level.getBlockState(checkPos);
                    if (neighborState.is(this) && neighborState.getValue(FACING) == facing) {
                        level.setBlock(checkPos, neighborState.setValue(OPEN, nextOpenState), 3);
                    }
                }
            }

            float pitch = level.getRandom().nextFloat() * 0.1F + 0.9F;
            var sound = nextOpenState
                    ? net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN
                    : net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE;
            level.playSound(null, pos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, pitch);

            if (!nextOpenState) {
                // Закрываем — создаём/обновляем зону
                BunkerZoneManager.onGateClosed(level, pos, facing);
                player.displayClientMessage(
                        Component.literal("Внутренний шлюз заблокирован. Сектора изолированы.")
                                .withStyle(ChatFormatting.AQUA), true);
            } else {
                // Открываем — удаляем зону и пересканируем соседей
                BunkerZoneManager.onGateOpened(level, pos);
                player.displayClientMessage(
                        Component.literal("Внутренний шлюз открыт: сектора объединены.")
                                .withStyle(ChatFormatting.YELLOW), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}