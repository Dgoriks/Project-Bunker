package dg.projectbunker.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BunkerHudConfigScreen extends Screen {
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public BunkerHudConfigScreen() {
        super(Component.literal("HUD Configuration"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Отрисовываем задний фон (слегка затемняем игру, чтобы видеть интерфейс)
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // Показываем подсказку сверху
        guiGraphics.drawCenteredString(this.font, "Зажмите ЛКМ на панели, чтобы перетащить её", this.width / 2, 10, 0xFFFFFF);

        // Рендерим саму панель прямо на этом экране, чтобы видеть куда двигаем
        BunkerStatusRenderer.render(guiGraphics, this.minecraft, this.font);

        // Дополнительно подсвечиваем рамку панели в режиме редактирования
        int x = BunkerStatusRenderer.panelX;
        int y = BunkerStatusRenderer.panelY;
        int w = BunkerStatusRenderer.PANEL_WIDTH;
        int h = BunkerStatusRenderer.PANEL_HEIGHT;
        guiGraphics.renderOutline(x - 1, y - 1, w + 2, h + 2, 0xFFFF5555); // Красная рамка редактирования

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Левая кнопка мыши
            int x = BunkerStatusRenderer.panelX;
            int y = BunkerStatusRenderer.panelY;
            int w = BunkerStatusRenderer.PANEL_WIDTH;
            int h = BunkerStatusRenderer.PANEL_HEIGHT;

            // Проверяем, кликнул ли игрок внутрь плашки
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                this.isDragging = true;
                this.dragOffsetX = (int) mouseX - x;
                this.dragOffsetY = (int) mouseY - y;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDragging && button == 0) {
            // Обновляем глобальные координаты панели во время движения мыши
            BunkerStatusRenderer.panelX = (int) mouseX - this.dragOffsetX;
            BunkerStatusRenderer.panelY = (int) mouseY - this.dragOffsetY;

            // Ограничиваем, чтобы панель не улетала за границы экрана
            int maxW = this.minecraft.getWindow().getGuiScaledWidth() - BunkerStatusRenderer.PANEL_WIDTH;
            int maxH = this.minecraft.getWindow().getGuiScaledHeight() - BunkerStatusRenderer.PANEL_HEIGHT;
            BunkerStatusRenderer.panelX = Math.max(0, Math.min(BunkerStatusRenderer.panelX, maxW));
            BunkerStatusRenderer.panelY = Math.max(0, Math.min(BunkerStatusRenderer.panelY, maxH));

            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Игра не ставится на паузу во время настройки
    }
}