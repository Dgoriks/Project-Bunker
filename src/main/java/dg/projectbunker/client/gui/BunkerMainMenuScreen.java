package dg.projectbunker.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BunkerMainMenuScreen extends Screen {

    // ВНИМАНИЕ: Замените "ваши_ресурсы" на MODID вашего мода в нижнем регистре!
    private static final ResourceLocation GRID_TEXTURE = ResourceLocation.fromNamespaceAndPath("project_bunker", "textures/gui/grid.png");

    public BunkerMainMenuScreen() {
        // Передаем пустой заголовок экрана суперклассу
        super(Component.empty());
    }

    @Override
    protected void init() {
        // Размеры кнопок со скриншота (они вытянутые и крупные)
        int btnWidth = 180;
        int btnHeight = 25;
        int spacing = 12;
        int centerGap = 280;// Отступ между кнопками


        // === РАСЧЕТ КООРДИНАТ ПО ГОРИЗОНТАЛИ (X) ===
        // Левый столбец: от центра экрана отступаем влево на ширину кнопки и половину зазора
        int leftX = this.width / 2 - btnWidth - (centerGap / 2);
        // Правый столбец: от центра экрана отступаем вправо на половину зазора
        int rightX = this.width / 2 + (centerGap / 2);

        // === РАСЧЕТ НАЧАЛЬНОЙ ТОЧКИ ПО ВЕРТИКАЛИ (Y) ===
        // Опускаем кнопки ниже текста (приблизительно на середину экрана)
        int startY = this.height / 2 + 10;

        // === ЛЕВЫЙ СТОЛБЕЦ ===
        this.addRenderableWidget(new TerminalButton(leftX, startY, btnWidth, btnHeight,
                Component.literal("Настройки Терминала"), () -> {
            if (this.minecraft != null) this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }));

        // Ряд 2 (Лево): Аварийный Выход
        this.addRenderableWidget(new TerminalButton(leftX, startY + btnHeight + spacing, btnWidth, btnHeight,
                Component.literal("Аварийный Выход"), () -> {
            if (this.minecraft != null) this.minecraft.stop();
        }));

        // === ПРАВЫЙ СТОЛБЕЦ (Теперь вход и сеть) ===
        // Ряд 1 (Право): Войти в Бункер
        this.addRenderableWidget(new TerminalButton(rightX, startY, btnWidth, btnHeight,
                Component.literal("Войти в Бункер"), () -> {
            if (this.minecraft != null) this.minecraft.setScreen(new SelectWorldScreen(this));
        }));

        // Ряд 2 (Право): Подключиться к Сети
        this.addRenderableWidget(new TerminalButton(rightX, startY + btnHeight + spacing, btnWidth, btnHeight,
                Component.literal("Подключиться к Сети"), () -> {
            if (this.minecraft != null) this.minecraft.setScreen(new JoinMultiplayerScreen(this));
        }));
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Полностью убираем дефолтный вызов фона с размытием
    }

    @Override
    public boolean isPauseScreen() {
        // Гарантируем, что экран обрабатывается как чистое главное меню
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Полная заливка экрана абсолютно черным цветом
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);

        // 2. ОТРИСОВКА СЕТКИ КОДОМ (Картинка grid.png больше НЕ ТРЕБУЕТСЯ!)
        int gridSize = 35;          // Расстояние между линиями в пикселях (можно менять)
        int gridColor = 0x2200FF00; // Зеленый цвет. 22 в начале — это прозрачность, чтобы сетка была тусклой и приятной

        // Рисуем вертикальные линии терминала
        for (int x = 0; x < this.width; x += gridSize) {
            guiGraphics.fill(x, 0, x + 1, this.height, gridColor);
        }
        // Рисуем горизонтальные линии терминала
        for (int y = 0; y < this.height; y += gridSize) {
            guiGraphics.fill(0, y, this.width, y + 1, gridColor);
        }

        // 3. Зеленый заголовок через матрицы
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.width / 2.0f, 45.0f, 0.0f);
        guiGraphics.pose().scale(1.5f, 1.5f, 1.0f);

        // 1. Первая строка заголовка
        Component titleText = Component.literal("PROJECT BUNKER");
        int titleWidth = this.font.width(titleText);
// Рисуем строго по центру (минус половина ширины строки)
        guiGraphics.drawString(this.font, titleText, -titleWidth / 2, 0, 0xFF00FF00, false);

// 2. Вторая строка заголовка (Инициализирован.)
        Component titleSubText = Component.literal("Инициализирован.");
        int titleSubWidth = this.font.width(titleSubText);
// Рисуем на 12 пикселей ниже первой строки (с учетом масштаба 1.5х это будет отличный отступ)
        guiGraphics.drawString(this.font, titleSubText, -titleSubWidth / 2, 12, 0xFF00FF00, false);

        guiGraphics.pose().popPose();
// ========================================================

// === БЛОК ОПИСАНИЯ НИЖЕ (Стандартный шрифт) ===
// Смещаем координаты Y пониже, так как заголовок стал двухстрочным (было 75 и 90, делаем 85 и 100)
        Component subTitleText = Component.literal("Мира больше нет....");
        guiGraphics.drawCenteredString(this.font, subTitleText, this.width / 2, 85, 0xFFFFFF00);

        Component extraText = Component.literal("Остался только ты ИНЖЕНЕР......");
        guiGraphics.drawCenteredString(this.font, extraText, this.width / 2, 100, 0xFFCC0000); // Красный цвет

        // 5. Отрисовка кнопок
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Запрещаем закрывать главное меню при нажатии на клавишу ESC
        return false;
    }
}