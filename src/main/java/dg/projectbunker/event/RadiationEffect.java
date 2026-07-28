package dg.projectbunker.event; // Убедись, что пакет совпадает с твоей папкой

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class RadiationEffect extends MobEffect {
    public RadiationEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Формула урона: базовый 1.0F + уровень эффекта
        float damage = 1.0F + amplifier;

        entity.hurt(entity.damageSources().magic(), damage);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Наносим урон раз в секунду (каждые 20 тиков)
        return duration % 20 == 0;
    }
}