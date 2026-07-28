package dg.projectbunker.client;

import dg.projectbunker.client.menu.ChestplateMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChestplateScreen extends AbstractContainerScreen<ChestplateMenu> {
    // Берём базовую текстуру инвентаря
    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    public ChestplateScreen(ChestplateMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 138;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 1. Отрисовываем шапку и инвентарь игрока из стандартного ассета
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, 18);
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y + 18, 0, 126, this.imageWidth, 120);

        // 2. Закрываем всю лишнюю сетку слотов дефолтным серым цветом интерфейса (0xC6C6C6)
        // Закрашиваем левую неиспользуемую часть верхнего контейнера
        guiGraphics.fill(x + 7, y + 18, x + 70, y + 48, 0xFFC6C6C6);
        // Закрашиваем правую неиспользуемую часть верхнего контейнера
        guiGraphics.fill(x + 106, y + 18, x + 169, y + 48, 0xFFC6C6C6);
        // Закрашиваем узкие зазоры снизу и сверху от наших двух слотов для идеального визуала
        guiGraphics.fill(x + 70, y + 18, x + 106, y + 23, 0xFFC6C6C6);
        guiGraphics.fill(x + 70, y + 41, x + 106, y + 48, 0xFFC6C6C6);

        // 3. Рисуем две кастомные рамки под наши слоты фильтров
        drawCustomSlotBounds(guiGraphics, x + 70, y + 23);
        drawCustomSlotBounds(guiGraphics, x + 88, y + 23);
    }

    // Вспомогательный метод для отрисовки объемной рамки ячейки в стиле Minecraft
    private void drawCustomSlotBounds(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 1, 0xFF373737);       // Верхняя тень
        guiGraphics.fill(x, y, x + 1, y + 18, 0xFF373737);       // Левая тень
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);  // Правый свет
        guiGraphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);  // Нижний свет
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B); // Тёмный фон ячейки
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}