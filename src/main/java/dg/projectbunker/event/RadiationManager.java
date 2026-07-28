package dg.projectbunker.event;

import dg.projectbunker.component.ModDataComponents;
import dg.projectbunker.data.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = "project_bunker")
public class RadiationManager {

    private static final ResourceKey<Level> INFECTED_DIMENSION = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("project_bunker", "infected_dimension"));

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide()) return;

        // Если игрок НЕ в заражённом измерении — радиация не действует вообще
        if (!level.dimension().equals(INFECTED_DIMENSION)) {
            if (player.hasEffect(ModEffects.RADIATION)) {
                player.removeEffect(ModEffects.RADIATION);
            }
            return;
        }

        // 1. ПРОВЕРКА БУНКЕРА: если внутри герметичного сектора — полная защита
        if (BunkerZoneManager.isPlayerSafe(player)) {
            if (player.hasEffect(ModEffects.RADIATION)) {
                player.removeEffect(ModEffects.RADIATION);
            }
            return;
        }

        // 2. ПРОВЕРКА HAZMAT
        boolean hasHelmet = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HAZMAT_HELMET.get());
        boolean hasChest = player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HAZMAT_CHESTPLATE.get());
        boolean hasLegs = player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HAZMAT_LEGGINGS.get());
        boolean hasBoots = player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HAZMAT_BOOTS.get());
        boolean hasFullHazmat = hasHelmet && hasChest && hasLegs && hasBoots;

        if (hasFullHazmat) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemContainerContents contents = chestplate.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

            List<ItemStack> items = new ArrayList<>();
            contents.nonEmptyItems().forEach(items::add);

            for (int i = 0; i < items.size(); i++) {
                ItemStack filterStack = items.get(i);
                if (filterStack.is(ModItems.FILLED_FILTER.get())) {
                    int currentCharge = filterStack.getOrDefault(ModDataComponents.FILTER_CHARGE.get(), 100);

                    if (currentCharge > 0) {
                        if (player.tickCount % 20 == 0) {
                            int newCharge = currentCharge - 1;
                            if (newCharge <= 0) {
                                items.set(i, new ItemStack(ModItems.EMPTY_FILTER.get()));
                            } else {
                                filterStack.set(ModDataComponents.FILTER_CHARGE.get(), newCharge);
                            }
                            chestplate.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
                        }

                        if (player.hasEffect(ModEffects.RADIATION)) {
                            player.removeEffect(ModEffects.RADIATION);
                        }
                        return;
                    }
                }
            }
        }

        // 3. НЕТ ЗАЩИТЫ — накладываем/усиливаем радиацию
        if (!player.hasEffect(ModEffects.RADIATION)) {
            player.addEffect(new MobEffectInstance(ModEffects.RADIATION, 40, 0, false, false, true));
        } else {
            MobEffectInstance currentEffect = player.getEffect(ModEffects.RADIATION);
            if (currentEffect != null && currentEffect.getDuration() <= 20) {
                int nextAmplifier = Math.min(currentEffect.getAmplifier() + 1, 4);
                player.addEffect(new MobEffectInstance(ModEffects.RADIATION, 40, nextAmplifier, false, false, true));
            }
        }
    }
}