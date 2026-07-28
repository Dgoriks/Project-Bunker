package dg.projectbunker.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BunkerStatusRenderer {

    // Размеры панели
    public static final int PANEL_WIDTH = 135;
    public static final int PANEL_HEIGHT = 56;

    // Начальная позиция (изменится при инициализации или движении)
    public static int panelX = 0;
    public static int panelY = 0;
    private static boolean isInitialized = false;

    public static void render(GuiGraphics guiGraphics, Minecraft mc, Font font) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Если позиция еще не настраивалась, ставим по умолчанию справа по центру
        if (!isInitialized) {
            panelX = screenWidth - PANEL_WIDTH - 10;
            panelY = (screenHeight - PANEL_HEIGHT) / 2;
            isInitialized = true;
        }

        // Фирменная палитра интерфейса
        int transparentBg = 0x771A1A17;
        int neonGreen = 0xFF00D147;
        int systemWhite = 0xFFAAAAAA;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Отрисовка фона плашки по динамическим координатам panelX и panelY
        guiGraphics.fill(panelX + 2, panelY, panelX + PANEL_WIDTH - 2, panelY + PANEL_HEIGHT, transparentBg);
        guiGraphics.fill(panelX, panelY + 2, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT - 2, transparentBg);
        guiGraphics.fill(panelX + 1, panelY + 1, panelX + PANEL_WIDTH - 1, panelY + PANEL_HEIGHT - 1, transparentBg);

        int currentRowY = panelY + 5;

        // 1. СТРОКА: Координаты
        int posX = mc.player != null ? mc.player.getBlockX() : 0;
        int posZ = mc.player != null ? mc.player.getBlockZ() : 0;
        String coordText = "X: " + posX + "   Z: " + posZ;

        drawPixelIconPlaceholder(guiGraphics, panelX + 8, currentRowY, neonGreen);
        guiGraphics.drawString(font, coordText, panelX + 22, currentRowY, neonGreen, false);

        // 2. СТРОКА: FPS
        currentRowY += 12;
        drawPixelIconPlaceholder(guiGraphics, panelX + 8, currentRowY, neonGreen);
        guiGraphics.drawString(font, "FPS", panelX + 22, currentRowY, systemWhite, false);
        String fpsVal = mc.fpsString.split(" ")[0];
        guiGraphics.drawString(font, fpsVal, panelX + PANEL_WIDTH - font.width(fpsVal) - 10, currentRowY, neonGreen, false);

        // 3. СТРОКА: Время
        currentRowY += 12;
        LocalDateTime now = LocalDateTime.now();
        drawPixelIconPlaceholder(guiGraphics, panelX + 8, currentRowY, neonGreen);
        guiGraphics.drawString(font, "Время", panelX + 22, currentRowY, systemWhite, false);
        String timeVal = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        guiGraphics.drawString(font, timeVal, panelX + PANEL_WIDTH - font.width(timeVal) - 10, currentRowY, neonGreen, false);

        // 4. СТРОКА: Дата
        currentRowY += 12;
        drawPixelIconPlaceholder(guiGraphics, panelX + 8, currentRowY, neonGreen);
        guiGraphics.drawString(font, "Дата", panelX + 22, currentRowY, systemWhite, false);
        String dateVal = now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        guiGraphics.drawString(font, dateVal, panelX + PANEL_WIDTH - font.width(dateVal) - 10, currentRowY, neonGreen, false);

        RenderSystem.disableBlend();
    }

    private static void drawPixelIconPlaceholder(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y + 1, x + 6, y + 5, color);
        g.fill(x + 1, y, x + 5, y + 6, color);
    }
}