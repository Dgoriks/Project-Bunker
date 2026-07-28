package dg.projectbunker.client.gui; // Ваш пакет

import dg.projectbunker.client.gui.TerminalButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = "project_bunker", value = Dist.CLIENT)
public class OptionsMenuTransformer {

    @SubscribeEvent
    public static void onSettingsMenuInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof OptionsScreen screen) {

            Runnable graphicsAction = null;
            Runnable audioAction = null;
            Runnable controlsAction = null;
            Runnable chatAction = null;
            Runnable languageAction = null;
            Runnable resourceAction = null;
            Runnable doneAction = null;

            for (var listener : event.getListenersList()) {
                if (listener instanceof AbstractWidget widget) {
                    widget.visible = false;
                    widget.active = false;

                    if (widget instanceof Button oldButton) {
                        String text = oldButton.getMessage().getString().toLowerCase();
                        Runnable pressAction = oldButton::onPress;

                        if (text.contains("график") || text.contains("video")) graphicsAction = pressAction;
                        else if (text.contains("звук") || text.contains("sound")) audioAction = pressAction;
                        else if (text.contains("управл") || text.contains("control")) controlsAction = pressAction;
                        else if (text.contains("чат") || text.contains("chat")) chatAction = pressAction;
                        else if (text.contains("язык") || text.contains("languag")) languageAction = pressAction;
                        else if (text.contains("набор") || text.contains("resource")) resourceAction = pressAction;
                        else if (text.contains("готово") || text.contains("done")) doneAction = pressAction;
                    }
                    else if (widget instanceof AbstractSliderButton oldSlider) {
                        oldSlider.visible = false;
                        oldSlider.active = false;
                    }
                }
            }

            int btnWidth = 160;
            int btnHeight = 24;
            int spacing = 10;
            int centerGap = 30;

            int leftX = screen.width / 2 - btnWidth - (centerGap / 2);
            int rightX = screen.width / 2 + (centerGap / 2);

            int totalBlockHeight = (btnHeight * 4) + (spacing * 3);
            int startY = (screen.height / 2) - (totalBlockHeight / 2) - 10;

            if (graphicsAction != null) {
                event.addListener(new TerminalButton(leftX, startY, btnWidth, btnHeight, Component.literal("Videoсистема Терминала"), graphicsAction));
            }
            if (controlsAction != null) {
                event.addListener(new TerminalButton(leftX, startY + btnHeight + spacing, btnWidth, btnHeight, Component.literal("Ввод Терминала"), controlsAction));
            }
            if (languageAction != null) {
                event.addListener(new TerminalButton(leftX, startY + (btnHeight + spacing) * 2, btnWidth, btnHeight, Component.literal("Системный язык Терминала"), languageAction));
            }
            event.addListener(new TerminalButton(leftX, startY + (btnHeight + spacing) * 3, btnWidth, btnHeight, Component.literal("СИСТЕМА: ГОТОВНОСТЬ"), () -> {}));

            if (audioAction != null) {
                event.addListener(new TerminalButton(rightX, startY, btnWidth, btnHeight, Component.literal("АУДИО-МОДУЛЬ"), audioAction));
            }
            if (chatAction != null) {
                event.addListener(new TerminalButton(rightX, startY + btnHeight + spacing, btnWidth, btnHeight, Component.literal("ПЕРЕДАЧА ДАННЫХ"), chatAction));
            }
            if (resourceAction != null) {
                event.addListener(new TerminalButton(rightX, startY + (btnHeight + spacing) * 2, btnWidth, btnHeight, Component.literal("БАЗА РЕСУРСОВ"), resourceAction));
            }
            event.addListener(new TerminalButton(rightX, startY + (btnHeight + spacing) * 3, btnWidth, btnHeight, Component.literal("ЯДРО: БЕЗОПАСНЫЙ РЕЖИМ"), () -> {}));

            if (doneAction != null) {
                int doneWidth = 200;
                int doneX = screen.width / 2 - doneWidth / 2;
                int doneY = startY + totalBlockHeight + 15;
                event.addListener(new TerminalButton(doneX, doneY, doneWidth, btnHeight, Component.literal("ПОДТВЕРДИТЬ ИЗМЕНЕНИЯ"), doneAction));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof OptionsScreen screen) {
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
        if (event.getScreen() instanceof OptionsScreen screen) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            guiGraphics.pose().pushPose();
            Component titleText = Component.literal("КОНФИГУРАЦИЯ СИСТЕМЫ");
            int titleWidth = Minecraft.getInstance().font.width(titleText);
            guiGraphics.drawString(Minecraft.getInstance().font, titleText, screen.width / 2 - titleWidth / 2, 20, 0xFF00FF00, false);
            guiGraphics.pose().popPose();
        }
    }
}