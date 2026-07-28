package dg.projectbunker.data.items;

import dg.projectbunker.client.gui.ThirstTracker;
import dg.projectbunker.data.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ArmyCanteen extends Item {

    public ArmyCanteen() {
        // Настройка предмета: 4 использования (глотка). Не стакается (stacksTo(1))
        super(new Item.Properties().stacksTo(1).durability(4));
    }

    // Включаем ванильную анимацию поднесения фляги ко рту и глотков
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    // Длительность питья (32 тика = 1.6 секунды)
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    // Срабатывает при правом клике мыши
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Проверяем уровень жажды в NBT-базе данных сервера/клиента
        if (ThirstTracker.getServerThirst(player) < 20) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else {
            // Если игрок уже полностью гидратирован, выводим ошибку на панель OS
            if (!level.isClientSide()) {
                ThirstTracker.sendNotification("ОГРАНИЧЕНИЕ: ВОДНЫЙ БАЛАНС В НОРМЕ");
            }
            return InteractionResultHolder.fail(stack);
        }
    }

    // Срабатывает в момент, когда полоса использования предмета полностью заполнилась
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide()) {
                // Изменяем уровень жажды в NBT-базе данных этого игрока и отправляем пакет синхронизации
                int currentThirst = ThirstTracker.getServerThirst(player);
                ThirstTracker.setServerThirst(player, currentThirst + 5);

                // Выводим системный лог в правом нижнем углу над рамкой
                ThirstTracker.sendNotification("ОЧИЩЕННАЯ ЖИДКОСТЬ УСВОЕНА. ГИДРАТАЦИЯ +5");
            }

            // Воспроизводим звук глотания воды
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);

            // Если игрок не в креативе — тратим прочность фляги
            if (!player.isCreative()) {
                int currentDamage = stack.getDamageValue();
                int maxDamage = stack.getMaxDamage();

                // Если это был последний глоток и прочность иссякла (currentDamage дошел до 3)
                if (currentDamage >= maxDamage - 1) {
                    // Превращаем полную флягу в пустую (заменяем айтем в руке)
                    return new ItemStack(ModItems.EMPTY_CANTEEN.get());
                } else {
                    // Иначе просто увеличиваем износ на 1 единицу
                    stack.setDamageValue(currentDamage + 1);
                }
            }
        }
        return stack;
    }
}