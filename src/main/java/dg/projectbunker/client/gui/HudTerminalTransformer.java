package dg.projectbunker.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = "project_bunker", value = Dist.CLIENT)
public class HudTerminalTransformer {

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        // Блокировка ванильных элементов HUD
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH) ||
                event.getName().equals(VanillaGuiLayers.FOOD_LEVEL) ||
                event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL) ||
                event.getName().equals(VanillaGuiLayers.AIR_LEVEL)) {
            event.setCanceled(true);
            return;
        }

        if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = mc.font;
            Player player = mc.player;

            // Сбор актуальных внутриигровых показателей
            int hp = (int) player.getHealth();
            int maxHp = (int) player.getMaxHealth();
            int food = player.getFoodData().getFoodLevel();
            int armor = player.getArmorValue();

            // Чтение жажды из твоего трекера
            int thirst = ThirstTracker.getThirst();

            int armorPercent = (armor * 100) / 20;

            // Габариты и позиционирование (Правый нижний угол)
            int boxWidth = 190;
            int boxHeight = 78;

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            int startX = screenWidth - boxWidth - 8;
            int startY = screenHeight - boxHeight - 8;

            int transparentBg = 0x771A1A17;
            int neonGreen = 0xAA00D147;
            int mutedGreen = 0xAA2A8C43;
            int systemWhite = 0xAAFFFFFF;

            guiGraphics.pose().pushPose();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            // Фон контейнера био-интерфейса
            guiGraphics.fill(startX + 2, startY, startX + boxWidth - 2, startY + boxHeight, transparentBg);
            guiGraphics.fill(startX, startY + 2, startX + boxWidth, startY + boxHeight - 2, transparentBg);
            guiGraphics.fill(startX + 1, startY + 1, startX + boxWidth - 1, startY + boxHeight - 1, transparentBg);

            // Шапка интерфейса
            guiGraphics.drawString(font, "NH", startX + 10, startY + 6, mutedGreen, false);
            guiGraphics.drawString(font, "BUNKER 05: БИО-ИНТЕРФЕЙС", startX + 25, startY + 6, neonGreen, false);

            // Разделительная линия
            guiGraphics.fill(startX + 10, startY + 16, startX + boxWidth - 10, startY + 17, neonGreen);

            int iconX = startX + 11;
            int textX = startX + 26;
            int valueX = startX + 138;
            int rowY = startY + 21;
            int rowSpacing = 11;

            // Ряд 1: ЖИЗНЕСПОСОБНОСТЬ
            drawHeartIcon(guiGraphics, iconX, rowY, neonGreen);
            guiGraphics.drawString(font, "Жизнеспособность", textX, rowY, systemWhite, false);
            guiGraphics.drawString(font, "» " + hp + "/" + maxHp, valueX, rowY, neonGreen, false);

            // Ряд 2: СТРУКТУРА БРОНИ
            rowY += rowSpacing;
            drawShieldIcon(guiGraphics, iconX, rowY, neonGreen);
            guiGraphics.drawString(font, "Структура Брони", textX, rowY, systemWhite, false);
            guiGraphics.drawString(font, "» " + armorPercent + "%", valueX, rowY, neonGreen, false);

            // Ряд 3: ГОЛОД
            rowY += rowSpacing;
            drawFoodIcon(guiGraphics, iconX, rowY, systemWhite);
            guiGraphics.drawString(font, "Голод", textX, rowY, systemWhite, false);
            guiGraphics.drawString(font, "» " + food + "/20", valueX, rowY, neonGreen, false);

            // Ряд 4: ЖАЖДА
            rowY += rowSpacing;
            drawWaterIcon(guiGraphics, iconX, rowY, 0xAA00AAFF);
            guiGraphics.drawString(font, "Жажда", textX, rowY, systemWhite, false);
            guiGraphics.drawString(font, "» " + thirst + "/20", valueX, rowY, neonGreen, false);

            // Прогресс-бар
            int barX = startX + 10;
            int barY = startY + 69;
            int barWidth = boxWidth - 20;
            int barHeight = 2;

            int totalStatusPercent = ((thirst + food) * 100) / 40;
            int activeBarWidth = (barWidth * totalStatusPercent) / 100;

            guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0x22000000);
            guiGraphics.fill(barX, barY, barX + activeBarWidth, barY + barHeight, neonGreen);

            RenderSystem.disableBlend();
            guiGraphics.pose().popPose();

            // Вызовы рендереров интерфейса (Рендерим поверх хотбара)
            BunkerStatusRenderer.render(guiGraphics, mc, font);
            BunkerAlertRenderer.render(guiGraphics, mc, font);
        }
    }

    private static void drawHeartIcon(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 1, y, x + 3, y + 1, color); g.fill(x + 4, y, x + 6, y + 1, color);
        g.fill(x, y + 1, x + 7, y + 3, color); g.fill(x + 1, y + 3, x + 6, y + 4, color);
        g.fill(x + 2, y + 4, x + 5, y + 5, color); g.fill(x + 3, y + 5, x + 4, y + 6, color);
    }

    private static void drawShieldIcon(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y, x + 7, y + 2, color); g.fill(x, y + 2, x + 7, y + 4, color);
        g.fill(x + 1, y + 4, x + 6, y + 5, color); g.fill(x + 2, y + 5, x + 5, y + 6, color);
        g.fill(x + 3, y + 6, x + 4, y + 7, color);
    }

    private static void drawFoodIcon(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 4, y, x + 6, y + 1, color); g.fill(x + 2, y + 1, x + 7, y + 3, color);
        g.fill(x + 1, y + 3, x + 6, y + 5, color); g.fill(x, y + 5, x + 3, y + 6, color);
    }

    private static void drawWaterIcon(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 3, y, x + 4, y + 1, color); g.fill(x + 2, y + 1, x + 5, y + 3, color);
        g.fill(x + 1, y + 3, x + 6, y + 5, color); g.fill(x, y + 5, x + 7, y + 7, color);
    }
}