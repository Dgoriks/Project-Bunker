package dg.projectbunker.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashSet;
import java.util.Set;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "project_bunker");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("project_bunker_tab", () -> CreativeModeTab.builder()
                    // Иконка вкладки (твоя армейская фляга)
                    .icon(() -> new ItemStack(ModItems.ARMY_CANTEEN.get()))
                    .title(Component.literal("Проект: Бункер"))
                    .displayItems((parameters, output) -> {

                        // Используем Set, чтобы гарантировать 100% защиту от дубликатов и крашей стака
                        Set<net.minecraft.world.item.Item> addedItems = new HashSet<>();

                        // 1. Автоматически регистрируем ВСЕ предметы блоков (двери, стены, ворота)
                        if (ModBlocks.ITEMS != null) {
                            ModBlocks.ITEMS.getEntries().forEach(entry -> {
                                net.minecraft.world.item.Item item = entry.get();
                                if (addedItems.add(item)) {
                                    output.accept(item);
                                }
                            });
                        }

                        // 2. Автоматически регистрируем ВСЕ остальные предметы мода (фляги, кастомные вещи)
                        if (ModItems.ITEMS != null) {
                            ModItems.ITEMS.getEntries().forEach(entry -> {
                                net.minecraft.world.item.Item item = entry.get();
                                if (addedItems.add(item)) {
                                    output.accept(item);
                                }
                            });
                        }
                    })
                    .build()
            );

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}