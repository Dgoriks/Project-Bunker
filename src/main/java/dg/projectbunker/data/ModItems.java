package dg.projectbunker.data;

import dg.projectbunker.data.items.ArmyCanteen;
import dg.projectbunker.data.items.EmptyCanteen;
import dg.projectbunker.data.items.TechChestplateItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // Создаем регистр предметов для нашего ID мода
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("project_bunker");

    // Регистрируем нашу армейскую флягу
    public static final DeferredItem<EmptyCanteen> EMPTY_CANTEEN = ITEMS.register("empty_canteen", EmptyCanteen::new);
    public static final DeferredItem<ArmyCanteen> ARMY_CANTEEN = ITEMS.register("army_canteen", ArmyCanteen::new);

    public static final DeferredHolder<Item, Item> HAZMAT_HELMET = ITEMS.register("hazmat_helmet",
            () -> new ArmorItem(ModArmorMaterials.HAZMAT, ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1).component(DataComponents.UNBREAKABLE, new Unbreakable(true))));

    public static final DeferredHolder<Item, Item> HAZMAT_CHESTPLATE = ITEMS.register("hazmat_chestplate",
            () -> new TechChestplateItem(ModArmorMaterials.HAZMAT, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1).component(DataComponents.UNBREAKABLE, new Unbreakable(true))));

    public static final DeferredHolder<Item, Item> HAZMAT_LEGGINGS = ITEMS.register("hazmat_leggings",
            () -> new ArmorItem(ModArmorMaterials.HAZMAT, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1).component(DataComponents.UNBREAKABLE, new Unbreakable(true))));

    public static final DeferredHolder<Item, Item> HAZMAT_BOOTS = ITEMS.register("hazmat_boots",
            () -> new ArmorItem(ModArmorMaterials.HAZMAT, ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1).component(DataComponents.UNBREAKABLE, new Unbreakable(true))));

    public static final DeferredHolder<Item, Item> FILLED_FILTER = ITEMS.register("filled_filter",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, Item> EMPTY_FILTER = ITEMS.register("empty_filter",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // Метод, который вы вызываете в главном конструкторе мода
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

}