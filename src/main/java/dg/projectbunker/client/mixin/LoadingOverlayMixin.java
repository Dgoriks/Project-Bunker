package dg.projectbunker.client.mixin;

import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Кастомизация экранов загрузки производится в LoadingScreenTransformer через ScreenEvent.Render.Pre.
 */
@Mixin(value = LoadingOverlay.class, remap = false)
public class LoadingOverlayMixin {

}