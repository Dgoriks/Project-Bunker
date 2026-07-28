package dg.projectbunker.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "project_bunker", value = Dist.CLIENT)
public class SuitShaderManager {

    public static final ResourceLocation SUIT_SHADER = ResourceLocation.fromNamespaceAndPath("project_bunker", "shaders/post/bunker_suit.json");
    private static final ResourceLocation SUIT_OVERLAY = ResourceLocation.fromNamespaceAndPath("project_bunker", "textures/gui/bunker_suit.png");

    private static boolean forceShaderInMenu = false;

    public static void setForceShaderInMenu(boolean active) {
        forceShaderInMenu = active;
    }

    /**
     * ПЕРЕХВАТ НАЖАТИЯ F4
     * Так как событие Key нельзя отменить через setCanceled,
     * мы перехватываем момент клика (когда кнопка только нажимается - GLFW_PRESS)
     * и мгновенно перезагружаем наш шейдер обратно, нейтрализуя ванильный сброс.
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getKey() == GLFW.GLFW_KEY_F4 && event.getAction() == GLFW.GLFW_PRESS) {
            Minecraft mc = Minecraft.getInstance();
            // Если игрок в мире или активен принудительный режим меню
            if (mc.level != null || forceShaderInMenu) {
                // Запускаем отложенную задачу в главном потоке отрисовки,
                // чтобы она выполнилась СРАЗУ ЖЕ после того, как Майнкрафт обработает ванильное нажатие F4
                mc.tell(() -> {
                    if (mc.gameRenderer != null) {
                        try {
                            mc.gameRenderer.loadEffect(SUIT_SHADER);
                        } catch (Exception e) {
                            System.err.println("[Bunker OS] Ошибка восстановления шейдера после F4: " + e.getMessage());
                        }
                    }
                });
            }
        }
    }

    /**
     * БЕЗОТКАЗНАЯ СТАБИЛИЗАЦИЯ ШЕЙДЕРА
     * Проверяет и удерживает шейдер включенным каждый игровой тик.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if ((mc.level != null || forceShaderInMenu) && mc.gameRenderer != null) {
            // Проверка каждую долю секунды. Если ванилла или другой мод отключили его — возвращаем на место.
            if (mc.gameRenderer.currentEffect() == null || !mc.gameRenderer.currentEffect().getName().equals(SUIT_SHADER.toString())) {
                try {
                    mc.gameRenderer.loadEffect(SUIT_SHADER);
                } catch (Exception e) {
                    System.err.println("[Bunker OS] Ошибка принудительной стабилизации шейдера: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Рендеринг интерфейсной маски шлема
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.screen == null) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();

            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

            guiGraphics.blit(
                    SUIT_OVERLAY,
                    0, 0,
                    0, 0,
                    screenWidth, screenHeight,
                    screenWidth, screenHeight
            );

            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }
    }
}