package dg.projectbunker.client.gui;
import dg.projectbunker.client.gui.BunkerMainMenuScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Привязываем к вашему статическому MODID и строго к клиентской стороне
@EventBusSubscriber(modid = "project_bunker", value = Dist.CLIENT)
public class BunkerMenuTransformer {

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        // Если игра пытается загрузить стандартный TitleScreen
        if (event.getScreen() instanceof TitleScreen) {
            // Проверка, что экран уже не заменен (исключаем зацикливание)
            if (!(event.getScreen() instanceof BunkerMainMenuScreen)) {
                // Перенаправляем на терминал бункера
                event.setNewScreen(new BunkerMainMenuScreen());
            }
        }
    }
}