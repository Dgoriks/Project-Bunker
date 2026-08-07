package dg.projectbunker.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * Упрощённый менеджер. Больше не хранит зоны.
 * Вся проверка — runtime через BunkerRadiationChecker.
 */
@EventBusSubscriber(modid = "project_bunker")
public class BunkerZoneManager {

    public static boolean isPlayerSafe(Player player) {
        return BunkerRadiationChecker.isPlayerSafe(player);
    }

    // События больше не нужны для логики зон, но оставим на случай расширений
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        // Уведомления можно добавить позже если нужно
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        // Уведомления можно добавить позже если нужно
    }
}