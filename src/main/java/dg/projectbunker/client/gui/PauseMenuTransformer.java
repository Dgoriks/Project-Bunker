package dg.projectbunker.client.gui; // Ваш точный пакет

import dg.projectbunker.client.gui.TerminalButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "project_bunker", value = Dist.CLIENT)
public class PauseMenuTransformer {

    // =========================================================================
    // СЕКЦИЯ НАСТРОЕК ТЕРМИНАЛА (МЕНЯЙТЕ НАЗВАНИЯ КНОПОК ИЗ ЭТОГО БЛОКА)
    // =========================================================================
    private static final String TXT_HEADER       = "РЕЖИМ ОЖИДАНИЯ ТЕРМИНАЛА";
    private static final String TXT_RESUME       = "ВЕРНУТЬСЯ В СИСТЕМУ";
    private static final String TXT_ADVANCEMENTS = "ЖУРНАЛ ЗАДАЧ";
    private static final String TXT_OPTIONS      = "КОНФИГУРАЦИЯ";
    private static final String TXT_STATS        = "АРХИВ ДАННЫХ";
    private static final String TXT_LAN          = "ОТКРЫТЬ ЛОКАЛЬНЫЙ ДОСТУП (LAN)";
    private static final String TXT_DISCONNECT   = "ОТКЛЮЧИТЬ ПИТАНИЕ";
    // =========================================================================

    @SubscribeEvent
    public static void onPauseMenuInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof PauseScreen screen) {
            Minecraft mc = Minecraft.getInstance();

            // --- ДЕФОЛТНЫЕ ДЕЙСТВИЯ ПОД 1.21.1 (БЕЗОПАСНЫЙ И СТАБИЛЬНЫЙ ВАРИАНТ) ---
            Runnable resumeAction = screen::onClose;

            Runnable optionsAction = () -> mc.setScreen(new net.minecraft.client.gui.screens.options.OptionsScreen(screen, mc.options));

            // ИСПРАВЛЕНО: Безопасный вызов отключения через общую систему отсоединения,
            // которая автоматически определяет тип сессии и корректно сохраняет мир
            Runnable disconnectAction = () -> {
                if (mc.level != null) {
                    mc.level.disconnect();
                    mc.setScreen(new TitleScreen());
                }
            };

            Runnable statsAction = () -> mc.setScreen(new net.minecraft.client.gui.screens.achievement.StatsScreen(screen, mc.player.getStats()));

            Runnable advancementsAction = () -> mc.setScreen(new net.minecraft.client.gui.screens.advancements.AdvancementsScreen(mc.player.connection.getAdvancements()));

            Runnable lanAction = () -> mc.setScreen(new ShareToLanScreen(screen));

            // 1. Сканируем ванильные кнопки. Если они есть — забираем их точные действия
            for (var listener : event.getListenersList()) {
                if (listener instanceof AbstractWidget widget) {
                    widget.visible = false;
                    widget.active = false;

                    if (widget instanceof Button oldButton) {
                        String text = oldButton.getMessage().getString().toLowerCase();
                        Runnable pressAction = oldButton::onPress;

                        if (text.contains("вернуть") || text.contains("resume")) resumeAction = pressAction;
                        if (text.contains("настро") || text.contains("options")) optionsAction = pressAction;
                        if (text.contains("сохран") || text.contains("disconnect") || text.contains("quit")) disconnectAction = pressAction;
                        if (text.contains("стат") || text.contains("stats")) statsAction = pressAction;
                        if (text.contains("достиг") || text.contains("advancement")) advancementsAction = pressAction;
                        if (text.contains("мир") || text.contains("lan") || text.contains("network") || text.contains("share")) lanAction = pressAction;
                    }
                }
            }

            // 2. ГЕОМЕТРИЯ СЕТКИ ТЕРМИНАЛА
            int btnWidth = 160;
            int btnHeight = 24;
            int spacing = 10;
            int centerGap = 30;

            int leftX = screen.width / 2 - btnWidth - (centerGap / 2);
            int rightX = screen.width / 2 + (centerGap / 2);

            int totalBlockHeight = (btnHeight * 3) + (spacing * 2);
            int startY = (screen.height / 2) - (totalBlockHeight / 2) - 10;

            // --- РЯД 1 ---
            event.addListener(new TerminalButton(leftX, startY, btnWidth, btnHeight,
                    Component.literal(TXT_RESUME), resumeAction));

            event.addListener(new TerminalButton(rightX, startY, btnWidth, btnHeight,
                    Component.literal(TXT_ADVANCEMENTS), advancementsAction));

            // --- РЯД 2 ---
            int row2Y = startY + btnHeight + spacing;

            event.addListener(new TerminalButton(leftX, row2Y, btnWidth, btnHeight,
                    Component.literal(TXT_OPTIONS), optionsAction));

            event.addListener(new TerminalButton(rightX, row2Y, btnWidth, btnHeight,
                    Component.literal(TXT_STATS), statsAction));

            // --- РЯД 3 (КНОПКА LAN) ---
            int lanWidth = (btnWidth * 2) + centerGap;
            int lanX = screen.width / 2 - lanWidth / 2;
            int lanY = row2Y + btnHeight + spacing;

            TerminalButton lanButton = new TerminalButton(lanX, lanY, lanWidth, btnHeight,
                    Component.literal(TXT_LAN), lanAction);

            // Если мы на одиночном сервере — узел связи доступен, если на стороннем сервере — блокируем
            if (mc.hasSingleplayerServer()) {
                lanButton.active = true;
            } else {
                lanButton.active = false;
            }
            event.addListener(lanButton);

            // --- РЯД 4: СИСТЕМНЫЙ ВЫХОД ---
            int exitWidth = 200;
            int doneX = screen.width / 2 - exitWidth / 2;
            int doneY = startY + totalBlockHeight + 15;

            event.addListener(new TerminalButton(doneX, doneY, exitWidth, btnHeight,
                    Component.literal(TXT_DISCONNECT), disconnectAction));
        }
    }

    @SubscribeEvent
    public static void onRenderPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof PauseScreen screen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            guiGraphics.fill(0, 0, screen.width, screen.height, 0xFF000000);

            int gridSize = 35;
            int gridColor = 0x2200FF00;
            for (int x = 0; x < screen.width; x += gridSize) {
                guiGraphics.fill(x, 0, x + 1, screen.height, gridColor);
            }
            for (int y = 0; y < screen.height; y += gridSize) {
                guiGraphics.fill(0, y, screen.width, y + 1, gridColor);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof PauseScreen screen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            guiGraphics.pose().pushPose();
            Component titleText = Component.literal(TXT_HEADER);
            int titleWidth = Minecraft.getInstance().font.width(titleText);
            guiGraphics.drawString(Minecraft.getInstance().font, titleText, screen.width / 2 - titleWidth / 2, 20, 0xFF00FF00, false);
            guiGraphics.pose().popPose();
        }
    }
}