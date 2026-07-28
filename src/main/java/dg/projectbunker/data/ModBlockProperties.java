package dg.projectbunker.data;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlockProperties {

    /**
     * ТЯЖЕЛЫЙ БУНКЕРНЫЙ БЕТОН
     * Сверхпрочный блок: долго ломается, полностью неуязвим для взрывов ТНТ.
     */
    public static final BlockBehaviour.Properties BUNKER_CONCRETE = BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .requiresCorrectToolForDrops()
            .strength(15.0F, 1200.0F) // Первая цифра — прочность (копание), вторая — устойчивость к взрывам
            .sound(SoundType.STONE);


    /**
     * СТАЛЬНАЯ ГЕРМОДВЕРЬ / ЛЮК
     * Очень прочный металлический блок, который не горит.
     */
    public static final BlockBehaviour.Properties REINFORCED_DOOR = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(10.0F, 40.0F)
            .ignitedByLava()
            .sound(SoundType.METAL); // Исправлено: HEAVY_METAL заменен на ванильный METAL
    /**
     * СВЕТЯЩАЯСЯ ЛАМПА БУНКЕРА (Аварийное освещение)
     * Излучает свет (уровень 15 — максимум, как факел) и имеет стеклянный звук.
     */
    public static final BlockBehaviour.Properties BUNKER_LAMP = BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            .strength(2.0F, 2.0F)
            .lightLevel(state -> 15) // Функция, задающая уровень светимости блоков
            .sound(SoundType.GLASS);

    /**
     * РЕЖИМ КРЕАТИВНОГО / ТЕХНИЧЕСКОГО БЛОКА
     * Блок, который нельзя сломать в выживании (как бедрок).
     */
    public static final BlockBehaviour.Properties UNBREAKABLE = BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(-1.0F, 3600000.0F) // Минусовая прочность делает блок неразрушимым
            .noLootTable(); // При поломке в креативе ничего не выпадает
}