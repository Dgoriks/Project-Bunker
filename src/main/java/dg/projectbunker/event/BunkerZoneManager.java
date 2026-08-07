package dg.projectbunker.event;

import dg.projectbunker.data.BunkerBlastDoorBlock;
import dg.projectbunker.data.BunkerGateBlock;
import dg.projectbunker.data.BunkerInternalGateBlock;
import dg.projectbunker.data.ModBlocks;
import dg.projectbunker.world.BunkerBFSScanner;
import dg.projectbunker.world.BunkerSavedData;
import dg.projectbunker.world.BunkerZone;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.*;

@EventBusSubscriber(modid = "project_bunker")
public class BunkerZoneManager {

    private static final int RESCAN_RADIUS = 64;

    /** Проверка безопасности игрока. Вызывается из RadiationManager. */
    public static boolean isPlayerSafe(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            return BunkerSavedData.get(serverLevel).isProtected(player.blockPosition());
        }
        return false;
    }

    /**
     * Вызывай из блока шлюза ПРИ ЗАКРЫТИИ.
     * Удаляет старую зону этого шлюза и создаёт новую через BFS-скан.
     */
    public static void onGateClosed(Level level, BlockPos controllerPos, Direction facing) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        BunkerSavedData data = BunkerSavedData.get(serverLevel);
        data.removeZone(controllerPos);

        BunkerBFSScanner.ScanResult result = BunkerBFSScanner.scan(serverLevel, controllerPos, facing);
        if (result.isSealed()) {
            data.addZone(result.getZone());
        }
    }

    /**
     * Вызывай из блока шлюза ПРИ ОТКРЫТИИ.
     * Удаляет зону этого шлюза и пересканирует соседние закрытые шлюзы.
     */
    public static void onGateOpened(Level level, BlockPos openedGatePos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        BunkerSavedData.get(serverLevel).removeZone(openedGatePos);
        rescanNearbyGates(serverLevel, openedGatePos);
    }

    /** При ломании блока — инвалидируем затронутые зоны и пересканируем шлюзы. */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockPos brokenPos = event.getPos();
        List<BunkerZone> invalidated = BunkerSavedData.get(serverLevel).invalidateZonesAt(brokenPos);

        if (!invalidated.isEmpty() && event.getPlayer() != null) {
            event.getPlayer().displayClientMessage(
                    Component.literal("ВНИМАНИЕ! Герметичность сектора нарушена из-за разрушения блока!")
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    true
            );
        }

        rescanNearbyGates(serverLevel, brokenPos);
    }

    /** При установке блока — если это закрытый шлюз, сканируем; в любом случае пересканируем соседей. */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockPos placedPos = event.getPos();
        BlockState placedState = event.getState();

        if (isGateBlock(placedState) && isGateClosed(placedState)) {
            Direction facing = getGateFacing(placedState);
            if (facing != null) {
                onGateClosed(serverLevel, placedPos, facing);
            }
        }

        rescanNearbyGates(serverLevel, placedPos);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BunkerSavedData data = BunkerSavedData.get(serverLevel);
        boolean breach = false;

        for (BlockPos pos : event.getAffectedBlocks()) {
            if (!data.invalidateZonesAt(pos).isEmpty()) {
                breach = true;
            }
        }

        if (breach) {
            serverLevel.players().forEach(player -> {
                if (data.isProtected(player.blockPosition())) {
                    player.displayClientMessage(
                            Component.literal("КРИТИЧЕСКАЯ УГРОЗА! Взрыв пробил периметр бункера!")
                                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                            false
                    );
                }
            });
        }

        for (BlockPos pos : event.getAffectedBlocks()) {
            rescanNearbyGates(serverLevel, pos);
        }
    }

    /** Ищет все закрытые шлюзы в радиусе 64 и пересканирует их зоны. */
    private static void rescanNearbyGates(ServerLevel level, BlockPos center) {
        BunkerSavedData data = BunkerSavedData.get(level);
        Set<BlockPos> toRescan = new HashSet<>();

        for (int dx = -RESCAN_RADIUS; dx <= RESCAN_RADIUS; dx++) {
            for (int dy = -RESCAN_RADIUS; dy <= RESCAN_RADIUS; dy++) {
                for (int dz = -RESCAN_RADIUS; dz <= RESCAN_RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > RESCAN_RADIUS * RESCAN_RADIUS) continue;

                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (isGateBlock(state) && isGateClosed(state)) {
                        toRescan.add(pos);
                    }
                }
            }
        }

        for (BlockPos gatePos : toRescan) {
            data.removeZone(gatePos);
            Direction facing = getGateFacing(level.getBlockState(gatePos));
            if (facing != null) {
                BunkerBFSScanner.ScanResult result = BunkerBFSScanner.scan(level, gatePos, facing);
                if (result.isSealed()) {
                    data.addZone(result.getZone());
                }
            }
        }
    }

    private static boolean isGateBlock(BlockState state) {
        return state.is(ModBlocks.BUNKER_GATE.get())
                || state.is(ModBlocks.BUNKER_BLAST_DOOR.get())
                || state.is(ModBlocks.BUNKER_INTERNAL_GATE.get());
    }

    private static boolean isGateClosed(BlockState state) {
        if (state.is(ModBlocks.BUNKER_GATE.get())) {
            return !state.getValue(BunkerGateBlock.OPEN);
        }
        if (state.is(ModBlocks.BUNKER_BLAST_DOOR.get())) {
            return !state.getValue(BunkerBlastDoorBlock.OPEN);
        }
        if (state.is(ModBlocks.BUNKER_INTERNAL_GATE.get())) {
            return !state.getValue(BunkerInternalGateBlock.OPEN);
        }
        return false;
    }

    private static Direction getGateFacing(BlockState state) {
        if (state.hasProperty(BunkerGateBlock.FACING)) {
            return state.getValue(BunkerGateBlock.FACING);
        }
        if (state.hasProperty(BunkerBlastDoorBlock.FACING)) {
            return state.getValue(BunkerBlastDoorBlock.FACING);
        }
        if (state.hasProperty(BunkerInternalGateBlock.FACING)) {
            return state.getValue(BunkerInternalGateBlock.FACING);
        }
        return null;
    }
}