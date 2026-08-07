package dg.projectbunker.event;

import dg.projectbunker.data.BunkerBlastDoorBlock;
import dg.projectbunker.data.BunkerGateBlock;
import dg.projectbunker.data.BunkerInternalGateBlock;
import dg.projectbunker.data.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Простая runtime-проверка: находится ли игрок внутри герметичного бункера.
 *
 * Логика: BFS от позиции игрока. Защитные блоки бункера — непроходимые стены.
 * Если BFS выходит за радиус 32 блока — значит путь наружу есть (радиация).
 * Если BFS застревает (все пути заблокированы стенами) — игрок в бункере (безопасно).
 *
 * Никаких зон. Никакого SavedData. Работает всегда актуально.
 */
public class BunkerRadiationChecker {

    private static final int MAX_RADIUS = 32;
    private static final int MAX_BLOCKS = 8000;

    public static boolean isPlayerSafe(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }

        BlockPos start = player.blockPosition();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        int checked = 0;

        while (!queue.isEmpty() && checked < MAX_BLOCKS) {
            BlockPos pos = queue.poll();
            checked++;

            // Вышли за радиус — путь наружу точно есть
            if (Math.abs(pos.getX() - start.getX()) > MAX_RADIUS ||
                Math.abs(pos.getY() - start.getY()) > MAX_RADIUS ||
                Math.abs(pos.getZ() - start.getZ()) > MAX_RADIUS) {
                return false;
            }

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.contains(neighbor)) continue;

                BlockState state = level.getBlockState(neighbor);

                // Защитный блок = стена. Радиация через него не проходит.
                if (isProtectiveBlock(state)) {
                    continue;
                }

                visited.add(neighbor);
                queue.add(neighbor);
            }
        }

        // Очередь опустела до лимита = все пути заблокированы стенами
        // Лимит достигнут = бункер слишком огромный, считаем незащищённым
        return queue.isEmpty();
    }

    private static boolean isProtectiveBlock(BlockState state) {
        // Базовые стены (всегда защищают)
        if (state.is(ModBlocks.REINFORCED_WALL.get())) return true;
        if (state.is(ModBlocks.REINFORCED_DOOR.get())) return true;
        if (state.is(ModBlocks.BUNKER_LAMP.get())) return true;

        // Шлюзы защищают ТОЛЬКО когда закрыты
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
}
