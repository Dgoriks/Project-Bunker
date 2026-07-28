package dg.projectbunker.client;

import dg.projectbunker.client.menu.ModMenuTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = "project_bunker",value = Dist.CLIENT)
public class ClientModEvents {


    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.CHESTPLATE_MENU.get(), ChestplateScreen::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        // Передаем метод отрисовки напрямую. Это обходит любые изменения в классах Mojang!
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath("project_bunker", "bunker_hud"),
                BunkerHudOverlay::render
        );
    }
}