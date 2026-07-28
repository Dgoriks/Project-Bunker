package dg.projectbunker;

import dg.projectbunker.client.ChestplateScreen;
import dg.projectbunker.client.gui.GreetingManager;
import dg.projectbunker.client.menu.ModMenuTypes;
import dg.projectbunker.component.ModDataComponents;
import dg.projectbunker.data.*;
import dg.projectbunker.event.DimensionBoundaryManager;
import dg.projectbunker.event.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IModFile;

/**
 * Главный класс мода Project Bunker
 * Архитектура мода для Minecraft 1.21.1 на NeoForge
 * 
 * Содержит:
 * - Регистрацию кастомных блоков, предметов, брони
 * - Регистрацию враждебных мобов (Mutant Zombie, Radiation Ghoul)
 * - Систему радиации с проверкой Hazmat-костюма
 * - Кастомное измерение Dead Earth с биомом "Выжженная пустошь"
 * - Кастомное главное меню с индустриальным дизайном
 * - Интеграцию с GregTech Modern (жидкости, блокировка руд)
 */
@Mod(ProjectBunker.MODID)
public class ProjectBunker {

    public static final String MODID = "project_bunker";
    private static final Logger LOGGER = LogManager.getLogger();

    public ProjectBunker(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModCreativeTabs.register(modEventBus);
        ModStructures.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);

        // Регистрируем реестры через DeferredRegister
        // Метод register(IEventBus) автоматически добавляет слушателей событий

        // ===== ЛОГИРОВАНИЕ =====
        LOGGER.info("Мод успешно инициализирован!");
        LOGGER.info("=".repeat(60));
    }






    @EventBusSubscriber(modid = "project_bunker")
    public static class NetworkRegistry {

        @SubscribeEvent
        public static void registerPackets(final RegisterPayloadHandlersEvent event) {
            // Присваиваем версию сетевого протокола "1", чтобы избежать несоответствий
            final PayloadRegistrar registrar = event.registrar("project_bunker").versioned("1");

            // Направляем пакет строго на КЛИЕНТСКУЮ сторону
            registrar.playToClient(
                    ThirstSyncPacket.TYPE,
                    ThirstSyncPacket.CODEC,
                    ThirstSyncPacket::handle
            );
        }
    }
}


