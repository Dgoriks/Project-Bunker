package dg.projectbunker.client.gui;

import dg.projectbunker.data.ThirstSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.HashMap;
import java.util.UUID;

@EventBusSubscriber(modid = "project_bunker")
public class ThirstTracker {

    private static final String NBT_KEY = "BunkerThirst";
    private static final HashMap<UUID, Integer> playerTicks = new HashMap<>();

    // Переменная только для рендеринга на КЛИЕНТЕ
    private static int clientThirstLevel = 20;

    // Уведомления OS
    private static String currentNotification = "";
    private static int notificationDisplayTicks = 0;

    public static int getThirst() {
        return clientThirstLevel;
    }

    public static void setClientThirst(int value) {
        clientThirstLevel = value;
    }

    // Изменение жажды на СЕРВЕРЕ с безопасной отправкой пакета
    public static void setServerThirst(Player player, int value) {
        if (player.level().isClientSide()) return;

        int newValue = Math.max(0, Math.min(20, value));

        // Записываем в системный NBT Майнкрафта
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putInt(NBT_KEY, newValue);

        // БЕЗОПАСНАЯ ОТПРАВКА: Использование рекомендуемого API NeoForge
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(serverPlayer, new ThirstSyncPacket(newValue));
        }
    }

    // Чтение жажды из NBT (без сетевой отправки)
    public static int getServerThirst(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(NBT_KEY)) {
            persistentData.putInt(NBT_KEY, 20);
        }
        return persistentData.getInt(NBT_KEY);
    }

    public static void sendNotification(String text) {
        currentNotification = text;
        notificationDisplayTicks = 100; // 5 секунд
    }

    public static String getCurrentNotification() {
        return currentNotification;
    }

    // =========================================================================
    // ИСПРАВЛЕНО: РАЗДЕЛЕНИЕ ЗАГРУЗКИ И СИНХРОНИЗАЦИИ (ЗАЩИТА ОТ КИКА)
    // =========================================================================

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        // Здесь мы ТОЛЬКО читаем данные из файла. Никаких сетевых пакетов!
        Player player = event.getEntity();
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(NBT_KEY)) {
            persistentData.putInt(NBT_KEY, 20);
        }
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        Player player = event.getEntity();
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putInt(NBT_KEY, getServerThirst(player));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            playerTicks.put(player.getUUID(), 0);

            // Получаем то, что загрузилось из файла
            int loadedThirst = getServerThirst(serverPlayer);

            // Теперь отправлять пакет БЕЗОПАСНО, так как игрок зашел в мир
            setServerThirst(serverPlayer, loadedThirst);

            // Выводим ваше сообщение
            sendNotification("БИО-ИНТЕРФЕЙС СИНХРОНИЗИРОВАН");
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            setServerThirst(player, 20);
            sendNotification("БИО-ИНТЕРФЕЙС ПЕРЕЗАПУЩЕН ПОСЛЕ СБОЯ");
        }
    }

    // =========================================================================
    // ТИКИ И РАСХОД
    // =========================================================================
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (notificationDisplayTicks > 0) {
            notificationDisplayTicks--;
            if (notificationDisplayTicks <= 0) currentNotification = "";
        }

        if (player.isCreative() || player.isSpectator()) return;

        UUID uuid = player.getUUID();
        int ticks = playerTicks.getOrDefault(uuid, 0) + 1;

        int depletionRate = 400;
        if (player.isSprinting()) depletionRate = 200;

        if (ticks >= depletionRate) {
            ticks = 0;
            int currentThirst = getServerThirst(player);
            setServerThirst(player, currentThirst - 1);

            if (currentThirst - 1 <= 0) {
                player.hurt(player.damageSources().starve(), 1.0F);
                sendNotification("КРИТИЧЕСКАЯ ДЕГИДРАТАЦИЯ! СИСТЕМА ПОВРЕЖДЕНА!");
            }
        }
        playerTicks.put(uuid, ticks);
    }

    @SubscribeEvent
    public static void onVanillaDrink(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            net.minecraft.world.item.ItemStack stack = event.getItem();

            if (stack.is(net.minecraft.world.item.Items.POTION)) {
                net.minecraft.world.item.alchemy.PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
                if (contents != null && contents.is(net.minecraft.world.item.alchemy.Potions.WATER)) {
                    player.hurt(player.damageSources().magic(), 4.0F);
                    setServerThirst(player, getServerThirst(player) - 2);
                    sendNotification("ОТРАВЛЕНИЕ! ОБНАРУЖЕНЫ ТЯЖЕЛЫЕ РАДИОНУКЛИДЫ!");
                }
            }
        }
    }
}