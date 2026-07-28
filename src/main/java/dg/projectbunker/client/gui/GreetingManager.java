package dg.projectbunker.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import com.mojang.blaze3d.systems.RenderSystem;

@EventBusSubscriber(modid = "project_bunker", value = Dist.CLIENT)
public class GreetingManager {

    private static final int MAX_TICKS = 120; // 6 секунд
    private static int displayTicks = 0;

    public static void startGreeting() {
        displayTicks = MAX_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (displayTicks > 0) {
            displayTicks--;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (displayTicks > 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui || mc.player == null) return;

            Font font = mc.font;
            GuiGraphics graphics = event.getGuiGraphics();

            // Динамическое центрирование на экране
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            // Компактные размеры окна под новый контент с прогресс-баром
            int width = 180;
            int height = 56;

            int x = (screenWidth - width) / 2;
            int y = (screenHeight - height) / 2 - 30; // Чуть выше прицела

            // Цветовая палитра (четкие цвета без лишнего размытия)
            int panelBg = 0x991A1A1A;       // Затемненный полупрозрачный фон
            int neonGreen = 0xFF00D147;     // Четкий ярко-зеленый
            int mutedGreen = 0xFF3FA653;    // Четкий приглушенный зеленый
            int whiteText = 0xFFE3E3E3;     // Четкий белый
            int barBgColor = 0x55555555;    // Серый фон для пустой шкалы загрузки

            // Настройки рендера: включаем блендинг, но отключаем сглаживание для пиксельной четкости
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            // Рисуем скругленную подложку плашки
            graphics.fill(x + 2, y, x + width - 2, y + height, panelBg);
            graphics.fill(x, y + 2, x + width, y + height - 2, panelBg);
            graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, panelBg);

            int currentY = y + 6;
            int paddingX = x + 10;

            // 1 строка: NeoForge
            graphics.drawString(font, "Bunker OS Core", paddingX, currentY, neonGreen, false);

            // 2 строка: Приветствуем вас, Инженер
            currentY += 11;
            String part1 = "Приветствуем вас, ";
            String part2 = "Инженер";
            graphics.drawString(font, part1, paddingX, currentY, mutedGreen, false);
            graphics.drawString(font, part2, paddingX + font.width(part1), currentY, neonGreen, false);

            // 3 строка: Bunker_05 инициализирован
            currentY += 11;
            String part3 = "Bunker 05 ";
            String part4 = "инициализирован";
            graphics.drawString(font, part3, paddingX, currentY, neonGreen, false);
            graphics.drawString(font, part4, paddingX + font.width(part3), currentY, whiteText, false);

            // ==========================================
            // ОТРЕСОВКА ЛИНЕЙНОГО ПРОГРЕСС-БАРА
            // ==========================================
            currentY += 14;
            int barWidth = width - 20; // Ширина полоски с отступами по бокам
            int barHeight = 4;         // Высота полоски в пикселях

            // Вычисляем процент заполнения (от 0 до 100%)
            int elapsedTicks = MAX_TICKS - displayTicks;
            float progress = (float) elapsedTicks / MAX_TICKS;
            int currentBarWidth = (int) (barWidth * progress);

            // Фон прогресс-бара (пустая серая шкала)
            graphics.fill(paddingX, currentY, paddingX + barWidth, currentY + barHeight, barBgColor);

            // Заполнение прогресс-бара (активная ярко-зеленая шкала)
            if (currentBarWidth > 0) {
                graphics.fill(paddingX, currentY, paddingX + currentBarWidth, currentY + barHeight, neonGreen);
            }

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    @SubscribeEvent
    public static void onClientJoin(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
        startGreeting();
    }
}
