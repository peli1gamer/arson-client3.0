package io.arson.client.mixin;

import io.arson.client.render.CustomSkyShader;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces only the vanilla overworld sky-disc draw call. Vanilla celestial bodies,
 * sunrise/sunset, fog and dimension-specific sky handling remain in the normal pass.
 */
@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Redirect(
            method = "addSkyPass",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSkyDisc(I)V")
    )
    private void arson$renderCustomSkyDisc(SkyRenderer skyRenderer, int skyColor) {
        if (!CustomSkyShader.render(skyColor)) {
            skyRenderer.renderSkyDisc(skyColor);
        }
    }
}
