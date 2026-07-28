package dg.projectbunker.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, "project_bunker");

    // Создаем эффект радиации. Категория HARMFUL (вредный), цвет — радиоактивный зеленый (0x39FF14)
    public static final DeferredHolder<MobEffect, MobEffect> RADIATION =
            MOB_EFFECTS.register("radiation", () -> new RadiationEffect(MobEffectCategory.HARMFUL, 0x39FF14));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}