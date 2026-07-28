package dg.projectbunker.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import java.util.ArrayList;
import java.util.List;

public class BunkerAlertRenderer {

    private static class AlertRow {
        String label;
        String value;
        int iconType; // 0 - сердце, 1 - голод, 2 - жажда, 3 - кислород

        AlertRow(String label, String value, int iconType) {
            this.label = label;
            this.value = value;
            this.iconType = iconType;
        }
    }

    public static void render(GuiGraphics guiGraphics, Minecraft mc, Font font) {
        Player player = mc.player;
        if (player == null) return;

        List<AlertRow> activeAlerts = new ArrayList<>();

        // 1. ПРОВЕРКА ЗДОРОВЬЯ (Критическое состояние <= 5 HP)
        float health = player.getHealth();
        if (health <= 5.0F) {
            activeAlerts.add(new AlertRow("МАЛО ЗДОРОВЬЯ", (int)health + "/20", 0));
        }

        // 2. ПРОВЕРКА ГОЛОДА (Срабатывает четко при 6 и ниже)
        int food = player.getFoodData().getFoodLevel();
        if (food <= 6) {
            activeAlerts.add(new AlertRow("МАЛО ГОЛОДА", food + "/20", 1));
        }

        // 3. ПРОВЕРКА ЖАЖДЫ (ИСПРАВЛЕНО: Прямое чтение из ThirstTracker)
        int thirst = ThirstTracker.getThirst();
        if (thirst <= 6) {
            activeAlerts.add(new AlertRow("МАЛО ЖАЖДЫ", thirst + "/20", 2));
        }

        // 4. ПРОВЕРКА КИСЛОРОДА (Под водой, воздуха меньше 45%)
        int air = player.getAirSupply();
        int maxAir = player.getMaxAirSupply();
        if (air < maxAir) {
            int airPercentage = (int) (((float) air / maxAir) * 100);
            if (airPercentage <= 45) {
                activeAlerts.add(new AlertRow("НИЗКИЙ КИСЛОРОД", airPercentage + "%", 3));
            }
        }

        // Если угроз нет — панель скрыта
        if (activeAlerts.isEmpty()) return;

        // Размеры и позиционирование (Слева по центру экрана, над чатом)
        int panelWidth = 140;
        int rowHeight = 22;
        int headerHeight = 20;
        int panelHeight = headerHeight + (activeAlerts.size() * rowHeight) + 6;

        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int x = 10;
        int y = (screenHeight - panelHeight) / 2 - 20;

        int transparentBg = 0x771A1A17;
        int neonGreen = 0xFF00D147;
        int alertRed = 0xFFFF3F20;
        int systemWhite = 0xFFAAAAAA;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Фон панели
        guiGraphics.fill(x + 2, y, x + panelWidth - 2, y + panelHeight, transparentBg);
        guiGraphics.fill(x, y + 2, x + panelWidth, y + panelHeight - 2, transparentBg);
        guiGraphics.fill(x + 1, y + 1, x + panelWidth - 1, y + panelHeight - 1, transparentBg);

        // Заголовок панели
        guiGraphics.drawString(font, "BUNKER OS", x + 10, y + 6, neonGreen, false);
        guiGraphics.fill(x + 8, y + 16, x + panelWidth - 8, y + 17, 0x3300D147);

        // Динамический рендер строк предупреждений
        int currentRowY = y + headerHeight + 2;
        for (AlertRow alert : activeAlerts) {
            drawAlertIcon(guiGraphics, x + 10, currentRowY + 2, alert.iconType);
            guiGraphics.drawString(font, alert.label, x + 24, currentRowY, systemWhite, false);

            currentRowY += 9;
            guiGraphics.drawString(font, alert.value, x + 24, currentRowY, alertRed, false);

            currentRowY += 13;
        }

        RenderSystem.disableBlend();
    }

    // Пиксельные матрицы иконок
    private static void drawAlertIcon(GuiGraphics g, int x, int y, int type) {
        switch (type) {
            case 0 -> { // Сердце
                int red = 0xFFFF2222;
                g.fill(x, y, x + 2, y + 2, red); g.fill(x + 3, y, x + 5, y + 2, red);
                g.fill(x, y + 2, x + 5, y + 4, red); g.fill(x + 1, y + 4, x + 4, y + 5, red);
                g.fill(x + 2, y + 5, x + 3, y + 6, red);
            }
            case 1 -> { // Голод
                int orange = 0xFFD17000;
                g.fill(x + 1, y, x + 4, y + 2, orange); g.fill(x, y + 2, x + 5, y + 5, orange);
                g.fill(x + 2, y + 5, x + 4, y + 6, orange);
            }
            case 2 -> { // Жажда
                int blue = 0xFF00AAFF;
                g.fill(x + 2, y, x + 3, y + 1, blue); g.fill(x + 1, y + 1, x + 4, y + 3, blue);
                g.fill(x, y + 3, x + 5, y + 6, blue);
            }
            case 3 -> { // Кислород
                int cyan = 0xFF00E5FF;
                g.fill(x + 1, y, x + 4, y + 1, cyan); g.fill(x, y + 1, x + 1, y + 4, cyan);
                g.fill(x + 4, y + 1, x + 5, y + 4, cyan); g.fill(x + 1, y + 4, x + 4, y + 5, cyan);
            }
        }
    }
}