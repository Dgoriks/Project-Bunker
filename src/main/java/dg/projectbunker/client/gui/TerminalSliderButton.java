package dg.projectbunker.client.gui; // Ваш пакет

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TerminalSliderButton extends AbstractWidget {

    private final AbstractSliderButton vanillaSlider;
    private double value;
    private boolean isDragging = false;

    private static Field valueField = null;
    private static Method applyValueMethod = null; // Поле для скрытого метода применения настроек

    private static final int COLOR_BG = 0xFF000000;
    private static final int COLOR_BORDER = 0xFF00FF00;
    private static final int COLOR_HOVER = 0xFF55FF55;
    private static final int COLOR_TEXT = 0xFF00FF00;
    private static final int COLOR_TEXT_HOVER = 0xFFFFFFFF;
    private static final int COLOR_HANDLE = 0xFF00FF00;

    static {
        try {
            // Получаем доступ к приватному полю значения
            valueField = AbstractSliderButton.class.getDeclaredField("value");
            valueField.setAccessible(true);

            // Получаем доступ к защищенному методу применения настроек FOV
            applyValueMethod = AbstractSliderButton.class.getDeclaredMethod("applyValue");
            applyValueMethod.setAccessible(true);
        } catch (Exception e) {
            try {
                valueField = AbstractSliderButton.class.getDeclaredField("value");
                valueField.setAccessible(true);

                applyValueMethod = AbstractSliderButton.class.getDeclaredMethod("applyValue");
                applyValueMethod.setAccessible(true);
            } catch (Exception ignored) {}
        }
    }

    public TerminalSliderButton(int x, int y, int width, int height, double value, AbstractSliderButton vanillaSlider) {
        super(x, y, width, height, Component.empty());
        this.value = value;
        this.vanillaSlider = vanillaSlider;
        this.updateSliderText();
    }

    private void setVanillaSliderValue(double val) {
        if (this.vanillaSlider != null && valueField != null) {
            try {
                valueField.set(this.vanillaSlider, val);
            } catch (Exception ignored) {}
        }
    }

    private void updateSliderText() {
        if (this.vanillaSlider != null) {
            setVanillaSliderValue(this.value);
            this.vanillaSlider.visible = true;
            this.setMessage(this.vanillaSlider.getMessage());
            this.vanillaSlider.visible = false;
        }
    }

    // ИСПРАВЛЕНО: Теперь метод принудительно заставляет движок игры обновить FOV персонажа
    private void applySliderValue() {
        if (this.vanillaSlider != null && valueField != null && applyValueMethod != null) {
            try {
                // 1. Записываем новое значение в ванильный слайдер
                valueField.set(this.vanillaSlider, this.value);
                // 2. Вызываем метод применения настроек ядра игры
                applyValueMethod.invoke(this.vanillaSlider);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.active && this.visible) {
            if (mouseX >= (double)this.getX() && mouseX < (double)(this.getX() + this.width)
                    && mouseY >= (double)this.getY() && mouseY < (double)(this.getY() + this.height)) {

                this.playDownSound(Minecraft.getInstance().getSoundManager());
                this.isDragging = true;
                this.setValueFromMouse(mouseX);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.active && this.visible && this.isDragging) {
            this.setValueFromMouse(mouseX);
            return true;
        }
        return false;
    }

    private void setValueFromMouse(double mouseX) {
        double newValue = (mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8);
        this.value = Mth.clamp(newValue, 0.0, 1.0);
        this.applySliderValue(); // Активируем применение измененного FOV
        this.updateSliderText();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean isHovered = this.isHoveredOrFocused() || this.isDragging;
        int currentBorderColor = isHovered ? COLOR_HOVER : COLOR_BORDER;
        int currentTextColor = isHovered ? COLOR_TEXT_HOVER : COLOR_TEXT;

        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, COLOR_BG);
        guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, currentBorderColor);

        int handleWidth = 6;
        int handleX = this.getX() + (int)(this.value * (double)(this.width - handleWidth));
        guiGraphics.fill(handleX, this.getY() + 1, handleX + handleWidth, this.getY() + this.height - 1, COLOR_HANDLE);

        Font font = Minecraft.getInstance().font;
        int textX = this.getX() + (this.width / 2);
        int textY = this.getY() + ((this.height - 8) / 2);
        guiGraphics.drawCenteredString(font, this.getMessage(), textX, textY, currentTextColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}