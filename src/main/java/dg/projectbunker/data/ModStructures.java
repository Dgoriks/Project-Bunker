package dg.projectbunker.data; // Укажи свой правильный пакет, если он отличается

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModStructures {
    // Создаем регистратор для типов структур
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, "project_bunker");

    // Регистрируем тип нашей структуры. Так как это Jigsaw (пазлы), мы используем ванильный кодек JigsawStructure
    public static final DeferredHolder<StructureType<?>, StructureType<JigsawStructure>> ABANDONED_CITY =
            STRUCTURE_TYPES.register("abandoned_city", () -> () -> JigsawStructure.CODEC);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}