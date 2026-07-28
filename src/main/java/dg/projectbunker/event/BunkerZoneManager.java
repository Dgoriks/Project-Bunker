package dg.projectbunker.event;

import dg.projectbunker.data.BunkerBlastDoorBlock;
import dg.projectbunker.data.BunkerGateBlock;
import dg.projectbunker.data.BunkerInternalGateBlock;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.List;

@EventBusSubscriber(modid = "project_bunker")
public class BunkerZoneManager {

    public static boolean isPlayerSafe(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            BunkerSavedData data = BunkerSavedData.get(serverLevel);
            return data.isProtected(player.blockPosition());
        }
        return false;
    }

    public static boolean checkSectorSealing(Level level, BlockPos controllerPos, Direction facing) {
        if (level instanceof ServerLevel serverLevel) {
            BunkerBFSScanner.ScanResult result = BunkerBFSScanner.scan(serverLevel, controllerPos, facing);
            if (result.isSealed()) {
                BunkerSavedData data = BunkerSavedData.get(serverLevel);
                data.addZone(result.getZone());
                return true;
            }
        }
        return false;
    }

    public static void clearSector(Level level, BlockPos controllerPos) {
        if (level instanceof ServerLevel serverLevel) {
            BunkerSavedData data = BunkerSavedData.get(serverLevel);
            // Удаляем только ту зону, которая непосредственно была привязана к открываемому контроллеру/воротам.
            // Если между шлюзом и жилым отсеком закрыт внутренний шлюз (BunkerInternalGateBlock),
            // зона жилого отсека останется нетронутой в NBT-данных мира!
            data.removeZone(controllerPos);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BunkerSavedData data = BunkerSavedData.get(serverLevel);
            List<BunkerZone> invalidated = data.invalidateWall(event.getPos());

            if (!invalidated.isEmpty() && event.getPlayer() != null) {
                event.getPlayer().displayClientMessage(
                        Component.literal("ВНИМАНИЕ! Герметичность сектора нарушена из-за разрушения блока!")
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                        true
                );
            }
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BunkerSavedData data = BunkerSavedData.get(serverLevel);
            boolean breach = false;

            for (BlockPos pos : event.getAffectedBlocks()) {
                List<BunkerZone> invalidated = data.invalidateWall(pos);
                if (!invalidated.isEmpty()) {
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
        }
    }

    public static void handleInternalGateOpen(Level level, BlockPos gatePos, Direction facing, Player actor) {
        if (level instanceof ServerLevel serverLevel) {
            BunkerSavedData data = BunkerSavedData.get(serverLevel);
            // При открытии внутреннего шлюза сбрасывается только локальная защита изолированного сектора
            data.invalidateWall(gatePos);
            actor.displayClientMessage(
                    Component.literal("Внутренний шлюз открыт: сектора объединены.")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
        }
    }

    public static void handleInternalGateBreach(Level level, BlockPos gatePos) {
        if (level instanceof ServerLevel serverLevel) {
            BunkerSavedData data = BunkerSavedData.get(serverLevel);
            data.invalidateWall(gatePos);
        }
    }
}