package dg.projectbunker.client;

import dg.projectbunker.component.ModDataComponents;
import dg.projectbunker.data.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BunkerHudOverlay {
    private static int bootProgress = 0;
    private static boolean wasWearingFullSet = false;

    public static void render(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        Player player = mc.player;

        boolean hasFullSet =
                player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HAZMAT_HELMET.get()) &&
                        player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HAZMAT_CHESTPLATE.get()) &&
                        player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.HAZMAT_LEGGINGS.get()) &&
                        player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.HAZMAT_BOOTS.get());

        if (hasFullSet) {
            if (!wasWearingFullSet) {
                bootProgress = 0;
                wasWearingFullSet = true;
            }

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            // Новый основной цвет интерфейса: Ярко-голубой неоновый (0x00E5FF)
            int mainColor = 0x00E5FF;

            if (bootProgress < 100) {
                if (player.tickCount % 2 == 0) { bootProgress += 2; }
                String bootText = "ПОДКЛЮЧЕНИЕ К ЭКЗОСКЕЛЕТУ: " + bootProgress + "%";
                guiGraphics.drawCenteredString(mc.font, bootText, screenWidth / 2, screenHeight / 2 - 10, mainColor);
                guiGraphics.fill(screenWidth / 2 - 50, screenHeight / 2 + 5, screenWidth / 2 - 50 + bootProgress, screenHeight / 2 + 8, 0xFF00E5FF);
            } else {
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                ItemContainerContents contents = chestplate.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

                java.util.List<ItemStack> items = contents.stream().toList();
                ItemStack slot1 = items.size() > 0 ? items.get(0) : ItemStack.EMPTY;
                ItemStack slot2 = items.size() > 1 ? items.get(1) : ItemStack.EMPTY;

                int currentDisplayCharge = 0;
                String slotStatus = "НЕТ КАРТРИДЖЕЙ";
                boolean systemActive = false;

                // Логика отображения на HUD
                if (!slot1.isEmpty() && slot1.is(ModItems.FILLED_FILTER.get())) {
                    currentDisplayCharge = slot1.getOrDefault(ModDataComponents.FILTER_CHARGE.get(), 100);
                    slotStatus = "ОСНОВНОЙ СЛОТ";
                    systemActive = true;
                } else if (!slot2.isEmpty() && slot2.is(ModItems.FILLED_FILTER.get())) {
                    currentDisplayCharge = slot2.getOrDefault(ModDataComponents.FILTER_CHARGE.get(), 100);
                    slotStatus = "РЕЗЕРВНЫЙ СЛОТ";
                    systemActive = true;
                }

                int color = systemActive ? mainColor : 0xFF0000; // Красный при угрозе
                String statusText = systemActive ? "[ СИСТЕМА ЖИЗНЕОБЕСПЕЧЕНИЯ: АКТИВНА ]" : "[ КРИТИЧЕСКАЯ УГРОЗА: ОБОИ СЛОТА ПУСТЫ ]";

                guiGraphics.drawString(mc.font, statusText, 10, 10, color);
                guiGraphics.drawString(mc.font, "[ АКТИВНЫЙ МОДУЛЬ: " + slotStatus + " ]", 10, 22, color);
                guiGraphics.drawString(mc.font, "[ РЕСУРС СИСТЕМЫ: " + currentDisplayCharge + "% ]", 10, 34, color);
            }
        } else {
            wasWearingFullSet = false;
            bootProgress = 0;
        }
    }
}