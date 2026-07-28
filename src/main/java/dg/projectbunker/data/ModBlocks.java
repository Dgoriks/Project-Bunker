package dg.projectbunker.data;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("project_bunker");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("project_bunker");

    // Стены и двери бункера
    public static final DeferredBlock<Block> REINFORCED_WALL = registerBlock("reinforced_wall",
            () -> new Block(ModBlockProperties.BUNKER_CONCRETE));

    public static final DeferredBlock<Block> REINFORCED_DOOR = registerBlock("reinforced_door",
            () -> new Block(ModBlockProperties.REINFORCED_DOOR));

    public static final DeferredBlock<Block> BUNKER_LAMP = registerBlock("bunker_lamp",
            () -> new Block(ModBlockProperties.BUNKER_LAMP));

    // Заражённые блоки для кастомного измерения
    public static final DeferredBlock<Block> INFECTED_DIRT = registerBlock("infected_dirt",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));

    public static final DeferredBlock<Block> INFECTED_STONE = registerBlock("infected_stone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    // ТВОИ СУПЕР-ВОРОТА (Теперь все используют метод registerBlock!)

    // Внешний шлюз бункера 3х3
    public static final DeferredBlock<Block> BUNKER_GATE = registerBlock("bunker_gate",
            () -> new BunkerGateBlock(ModBlockProperties.REINFORCED_DOOR));

    // Двухблочная гермодверь
    public static final DeferredBlock<Block> BUNKER_BLAST_DOOR = registerBlock("bunker_blast_door",
            () -> new BunkerBlastDoorBlock(ModBlockProperties.REINFORCED_DOOR.noOcclusion()));

    // Внутренний шлюз фильтрации 3х3
    public static final DeferredBlock<Block> BUNKER_INTERNAL_GATE = registerBlock("bunker_internal_gate",
            () -> new BunkerInternalGateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F, 8.0F)
                    .noOcclusion()));

    // Вспомогательный метод: автоматически регистрирует блок и создаёт для него предмет (BlockItem)
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> blockSupplier) {
        DeferredBlock<T> block = BLOCKS.register(name, blockSupplier);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}