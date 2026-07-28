package dg.projectbunker.data.items;

import dg.projectbunker.client.menu.ChestplateMenu;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TechChestplateItem extends ArmorItem {
    public TechChestplateItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack chestplate = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Экзо-интерфейс фильтрации");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                    // Передаем null вместо FriendlyByteBuf, так как на сервере он не нужен для создания
                    return new ChestplateMenu(id, inv, null);
                }
            });
        }

        return InteractionResultHolder.sidedSuccess(chestplate, level.isClientSide());
    }
}