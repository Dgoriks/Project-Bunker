package dg.projectbunker.client.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TerminalButton extends AbstractButton {

    private final Runnable onPress;

    // Цветовые константы в формате ARGB (0xAARRGGBB)
    private static final int COLOR_BG = 0xFF000000;       // Чистый черный фон
    private static final int COLOR_BORDER = 0xFF00FF00;   // Ярко-зеленый (обычный)
    private static final int COLOR_HOVER = 0xFF55FF55;    // Светло-зеленый (при наведении)
    private static final int COLOR_TEXT = 0xFF00FF00;     // Зеленый текст
    private static final int COLOR_TEXT_HOVER = 0xFFFFFFFF; // Белый текст при наведении

    public TerminalButton(int x, int y, int width, int height, Component message, Runnable onPress) {
        super(x, y, width, height, message);
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        if (this.onPress != null) {
            this.onPress.run();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Проверяем, наведена ли мышь или активна ли кнопка клавиатурой
        boolean isHovered = this.isHoveredOrFocused();

        int currentBorderColor = isHovered ? COLOR_HOVER : COLOR_BORDER;
        int currentTextColor = isHovered ? COLOR_TEXT_HOVER : COLOR_TEXT;

        // 1. Отрисовка черной подложки кнопки
        // getX() и getY() — новые методы позиционирования вместо прямого доступа к полям x и y
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, COLOR_BG);

        // 2. Отрисовка тонкой рамки в 1 пиксель
        guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, currentBorderColor);

        // 3. Отрисовка текста строго по центру кнопки
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int textX = this.getX() + (this.width / 2);
        // Выравнивание по вертикали: (высота кнопки - высота шрифта(8)) / 2
        int textY = this.getY() + ((this.height - 8) / 2);

        // Передаем false в конце, чтобы у текста не было стандартной черной тени (стиль терминала)
        guiGraphics.drawCenteredString(font, this.getMessage(), textX, textY, currentTextColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}