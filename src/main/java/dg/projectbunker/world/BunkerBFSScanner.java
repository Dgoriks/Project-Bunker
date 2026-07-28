package dg.projectbunker.world;

import dg.projectbunker.data.BunkerBlastDoorBlock;
import dg.projectbunker.data.BunkerGateBlock;
import dg.projectbunker.data.BunkerInternalGateBlock;
import dg.projectbunker.data.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class BunkerBFSScanner {

    public static final int DEFAULT_MAX_VOLUME = 8000;

    public static class ScanResult {
        private final boolean sealed;
        private final BunkerZone zone;

        public ScanResult(boolean sealed, BunkerZone zone) {
            this.sealed = sealed;
            this.zone = zone;
        }

        public boolean isSealed() {
            return sealed;
        }

        public BunkerZone getZone() {
            return zone;
        }
    }

    public static ScanResult scan(Level level, BlockPos controllerPos, Direction facing) {
        return scan(level, controllerPos, facing, DEFAULT_MAX_VOLUME);
    }

    public static ScanResult scan(Level level, BlockPos controllerPos, Direction facing, int maxVolume) {
        BlockPos startPos = controllerPos.relative(facing);
        if (isSealedBlock(level, startPos)) {
            return new ScanResult(false, null);
        }

        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> interior = new HashSet<>();
        Set<BlockPos> walls = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visited.add(startPos);

        Direction[] directions = Direction.values();

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            if (isSealedBlock(level, current)) {
                walls.add(current);
                continue;
            }

            interior.add(current);

            if (interior.size() > maxVolume) {
                return new ScanResult(false, null);
            }

            for (Direction dir : directions) {
                BlockPos neighbor = current.relative(dir).immutable();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        if (interior.isEmpty()) {
            return new ScanResult(false, null);
        }

        return new ScanResult(true, new BunkerZone(controllerPos, interior, walls));
    }

    public static boolean isSealedBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        // Стандартные герметичные блоки и стены
        if (state.is(ModBlocks.REINFORCED_WALL.get()))
            return true;
        if (state.is(ModBlocks.REINFORCED_DOOR.get()))
            return true;
        if (state.is(ModBlocks.BUNKER_LAMP.get()))
            return true;

        // Внешние ворота 3х3 (закрыты -> герметично)
        if (state.is(ModBlocks.BUNKER_GATE.get())) {
            return !state.hasProperty(BunkerGateBlock.OPEN) || !state.getValue(BunkerGateBlock.OPEN);
        }

        // Двухблочная гермодверь (закрыта -> герметично)
        if (state.is(ModBlocks.BUNKER_BLAST_DOOR.get())) {
            return !state.hasProperty(BunkerBlastDoorBlock.OPEN) || !state.getValue(BunkerBlastDoorBlock.OPEN);
        }

        // Внутренний фильтрационный шлюз (закрыт -> работает как изолирующая стена)
        if (state.is(ModBlocks.BUNKER_INTERNAL_GATE.get())) {
            return !state.hasProperty(BunkerInternalGateBlock.OPEN) || !state.getValue(BunkerInternalGateBlock.OPEN);
        }

        return false;
    }
}