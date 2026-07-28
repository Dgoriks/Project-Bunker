package dg.projectbunker.data;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, "project_bunker");

    // Технологичный костюм хим-био-радиационной защиты (CBRN Hazmat)
    public static final Holder<ArmorMaterial> HAZMAT = ARMOR_MATERIALS.register("hazmat", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);       // Прочность сапог
                map.put(ArmorItem.Type.LEGGINGS, 4);    // Прочность штанов
                map.put(ArmorItem.Type.CHESTPLATE, 5);  // Прочность нагрудника (как алмазный)
                map.put(ArmorItem.Type.HELMET, 2);      // Прочность шлема
            }),
            10, // Зачаровываемость (технологичные вещи чаруются хуже магии)
            SoundEvents.ARMOR_EQUIP_NETHERITE, // Звук тяжелой экипировки будущего!
            () -> Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), // Чем чинить (например, железом или кастомным слитком)
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("project_bunker", "hazmat"))),
            2.0F, // Toughness (Броня прочная к мощным ударам)
            0.1F  // Knockback Resistance (Слегка защищает от отбрасывания)
    ));
}