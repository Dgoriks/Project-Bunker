package dg.projectbunker.client.gui; // Твой точный пакет

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import java.util.ArrayList;

@EventBusSubscriber(modid = "project_bunker", value = Dist.CLIENT)
public class LoadingScreenTransformer {

    // Проверяем исключительно три главных экрана входа
    private static boolean isLoadingScreen(Screen screen) {
        if (screen == null) return false;

        Minecraft mc = Minecraft.getInstance();

        // Если мир уже загружен, полностью пропускаем
        if (mc.level != null) {
            return false;
        }

        return screen instanceof ConnectScreen
                || screen instanceof ReceivingLevelScreen
                || screen instanceof LevelLoadingScreen;
    }

    // Публичный метод стадий загрузки (чтобы не было ошибок компиляции)
    public static String getCustomLoadingStage(Screen screen) {
        if (screen instanceof ConnectScreen) {
            return "УСТАНОВКА СОЕДИНЕНИЯ С СЕРВЕРОМ ЯДРА...";
        } else if (screen instanceof ReceivingLevelScreen) {
            return "СИНХРОНИЗАЦИЯ БАЗЫ ДАННЫХ БУНКЕРА...";
        } else if (screen instanceof LevelLoadingScreen) {
            return "ПОДГОТОВКА СЕКТОРОВ ОКРУЖАЮЩЕЙ СРЕДЫ...";
        }
        return "ЗАГРУЗКА ЦЕНТРАЛЬНОГО ПРОЦЕССОРА BUNKER OS...";
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Pre event) {
        Screen screen = event.getScreen();
        if (isLoadingScreen(screen)) {
            // ИСПРАВЛЕНО: Больше никакого .clear(), который вызывал ошибку UnsupportedOperationException!
            // Удаляем ванильные элементы безопасно по одному через итератор копии списка,
            // чтобы не трогать сам заблокированный unmodifiable контейнер напрямую.
            try {
                new ArrayList<>(event.getListenersList()).forEach(event::removeListener);
            } catch (Exception e) {
                // Если список совсем намертво заблокирован ваниллой — просто игнорируем,
                // на экранах загрузки кнопок всё равно обычно нет.
            }
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();

        // Полный пропуск для смены языка, ресурспаков и шейдеров
        if (!isLoadingScreen(screen)) return;

        event.setCanceled(true);

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();

        guiGraphics.pose().pushPose();

        // 1. Черный фон
        guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF000000);

        // 2. Сетка терминала
        int gridSize = 30;
        int gridColor = 0x1100FF00;
        for (int x = 0; x < screen.width; x += gridSize) {
            guiGraphics.fill(x, 0, x + 1, screen.height, gridColor);
        }
        for (int y = 0; y < screen.height; y += gridSize) {
            guiGraphics.fill(0, y, screen.width, y + 1, gridColor);
        }

        // 3. Главный текст
        Component stageText = Component.literal(getCustomLoadingStage(screen));
        int stageWidth = mc.font.width(stageText);
        guiGraphics.drawString(mc.font, stageText, screen.width / 2 - stageWidth / 2, screen.height / 2 - 20, 0xFF00FF00, false);

        // 4. Лог командной строки
        Component subText = Component.literal("Загрузка пакетов данных... [OK]");
        int subWidth = mc.font.width(subText);
        guiGraphics.drawString(mc.font, subText, screen.width / 2 - subWidth / 2, screen.height / 2, 0xFF00AA00, false);

        // 5. Прогресс мира
        if (screen instanceof LevelLoadingScreen) {
            Component pctText = Component.literal("СТАТУС СБОРКИ МАТРИЦЫ МИРА: В ПРОЦЕССЕ");
            int pctWidth = mc.font.width(pctText);
            guiGraphics.drawString(mc.font, pctText, screen.width / 2 - pctWidth / 2, screen.height / 2 + 30, 0xFF00FF00, false);
        }

        for (var listener : screen.children()) {
            if (listener instanceof AbstractWidget widget) {
                widget.render(guiGraphics, event.getMouseX(), event.getMouseY(), event.getPartialTick());
            }
        }

        guiGraphics.pose().popPose();
    }
}