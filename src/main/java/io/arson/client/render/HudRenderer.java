package io.arson.client.render;

import io.arson.client.ArsonClient;
import io.arson.client.module.HudModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Lightweight client HUD renderer driven by HudModule settings. */
public final class HudRenderer {
    private HudRenderer() {}

    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        HudModule hud = (HudModule) ArsonClient.getInstance().modules().get("hud");
        if (hud == null || !hud.enabled()) return;

        float scale = (float) hud.scale();
        int drawX = 6;
        int drawY = 6;
        int line = 11;
        int y = drawY;

        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);

        if (hud.showWatermark()) {
            graphics.drawString(client.font, "Arson V3", drawX, y, 0xFFFFFFFF, true);
            y += line;
        }

        if (hud.showCoordinates()) {
            String text = String.format(java.util.Locale.ROOT, "XYZ %d %d %d",
                    client.player.blockPosition().getX(),
                    client.player.blockPosition().getY(),
                    client.player.blockPosition().getZ());
            graphics.drawString(client.font, text, drawX, y, 0xFFD0D0D0, true);
            y += line;
        }

        if (hud.showFps()) {
            graphics.drawString(client.font, "FPS " + client.getFps(), drawX, y, 0xFFD0D0D0, true);
        }

        graphics.pose().popMatrix();
    }
}
