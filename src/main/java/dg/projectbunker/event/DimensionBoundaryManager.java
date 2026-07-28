package dg.projectbunker.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = "project_bunker")
public class DimensionBoundaryManager {

    // Ссылка на твоё кастомное измерение (убедись, что ID совпадает с твоим регистром)
    private static final ResourceKey<Level> INFECTED_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("project_bunker", "infected_dimension"));

    // Максимальная высота, на которой ЕЩЁ МОЖНО строить (55-я высота)
    private static final int MAX_BUILD_HEIGHT = 55;

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        // Проверяем, что действие происходит на сервере, чтобы избежать десинхронизации
        if (event.getLevel() != null && !event.getLevel().isClientSide()) {

            // Получаем ванильный уровень
            if (event.getLevel() instanceof Level level) {

                // Проверяем, находится ли игрок в твоём измерении
                if (level.dimension().equals(INFECTED_DIMENSION)) {

                    // Получаем координату Y блока, который пытаются поставить
                    int placeY = event.getPos().getY();

                    // Если высота строго выше 55
                    if (placeY > MAX_BUILD_HEIGHT) {
                        // Отменяем установку блока
                        event.setCanceled(true);

                        // Если блок ставил именно игрок, выводим ему предупреждение в экшн-бар (над хотбаром)
                        if (event.getEntity() instanceof Player player) {
                            player.displayClientMessage(
                                    Component.literal("Строительство выше 55 высоты заблокировано: нестабильная атмосфера!")
                                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                                    true // true выводит текст в экшн-бар, чтобы не забивать чат
                            );
                        }
                    }
                }
            }
        }
    }
}