package dg.projectbunker.client.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, "project_bunker");

    public static final DeferredHolder<MenuType<?>, MenuType<ChestplateMenu>> CHESTPLATE_MENU =
            MENUS.register("chestplate_menu", () -> IMenuTypeExtension.create(ChestplateMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}