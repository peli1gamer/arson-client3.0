package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Live GUI presentation telemetry for local HUD/render diagnostics. */
public final class RenderGuiInfoModule extends Module {
    private float guiScale;
    private int mouseX;
    private int mouseY;
    private boolean screenOpen;

    public RenderGuiInfoModule() {
        super("render-gui-info", "Render GUI Info", Category.RENDER,
                "Reports local GUI scale, mouse position, and screen state for render diagnostics.");
    }

    @Override protected void onTick(Minecraft client) {
        var window = client.getWindow();
        guiScale = (float) window.getGuiScale();
        mouseX = (int) Math.round(client.mouseHandler.xpos() * window.getGuiScaledWidth() / Math.max(1, window.getWidth()));
        mouseY = (int) Math.round(client.mouseHandler.ypos() * window.getGuiScaledHeight() / Math.max(1, window.getHeight()));
        screenOpen = client.screen != null;
    }

    public float guiScale() { return guiScale; }
    public int mouseX() { return mouseX; }
    public int mouseY() { return mouseY; }
    public boolean screenOpen() { return screenOpen; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "GUI %.1fx  Mouse %d,%d%s", guiScale, mouseX, mouseY, screenOpen ? "  Screen" : "");
    }
}
