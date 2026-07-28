package dg.projectbunker.client.menu;

import dg.projectbunker.data.ModItems;
import dg.projectbunker.data.items.TechChestplateItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class ChestplateMenu extends AbstractContainerMenu {
    private final ItemStack chestplate;

    // Внутренний инвентарь строго на 2 слота
    private final ItemStackHandler filterInventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.FILLED_FILTER.get()) || stack.is(ModItems.EMPTY_FILTER.get());
        }

        // Жестко ограничиваем вместимость ячейки до 1 фильтра
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            List<ItemStack> list = new ArrayList<>();
            list.add(getStackInSlot(0));
            list.add(getStackInSlot(1));
            chestplate.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));
        }
    };

    public ChestplateMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.CHESTPLATE_MENU.get(), containerId);
        this.chestplate = findChestplate(playerInventory.player);

        if (!this.chestplate.isEmpty()) {
            ItemContainerContents contents = chestplate.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            int index = 0;
            for (ItemStack item : contents.stream().toList()) {
                if (index < filterInventory.getSlots()) {
                    filterInventory.setStackInSlot(index++, item);
                }
            }
        }

        initSlots(playerInventory);
    }

    public static ItemStack findChestplate(Player player) {
        ItemStack chest = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        if (chest.getItem() instanceof TechChestplateItem) return chest;
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (main.getItem() instanceof TechChestplateItem) return main;
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (off.getItem() instanceof TechChestplateItem) return off;
        return ItemStack.EMPTY;
    }

    private void initSlots(Inventory playerInventory) {
        // Добавляем ровно 2 слота под фильтры. Координаты выровнены под чистый прямоугольник фона.
        this.addSlot(new SlotItemHandler(filterInventory, 0, 71, 24)); // Левый слот
        this.addSlot(new SlotItemHandler(filterInventory, 1, 89, 24)); // Правый слот

        // Инвентарь игрока (3 ряда)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 56 + row * 18));
            }
        }

        // Хотбар (нижняя панель быстрого доступа)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 114));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 2) { // Извлекаем из слотов фильтра в инвентарь игрока
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Быстрое перемещение из инвентаря в систему фильтрации
                if (itemstack1.is(ModItems.FILLED_FILTER.get()) || itemstack1.is(ModItems.EMPTY_FILTER.get())) {
                    // Проверяем первый слот
                    Slot targetSlot0 = this.slots.get(0);
                    Slot targetSlot1 = this.slots.get(1);

                    if (!targetSlot0.hasItem()) {
                        ItemStack insertStack = itemstack1.split(1);
                        targetSlot0.set(insertStack);
                    } else if (!targetSlot1.hasItem()) {
                        ItemStack insertStack = itemstack1.split(1);
                        targetSlot1.set(insertStack);
                    } else {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return !chestplate.isEmpty() && (
            player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST) == chestplate ||
            player.getItemInHand(InteractionHand.MAIN_HAND) == chestplate ||
            player.getItemInHand(InteractionHand.OFF_HAND) == chestplate
        );
    }
}