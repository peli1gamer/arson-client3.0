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
        if (window == null) {
            guiScale = 0;
            mouseX = mouseY = 0;
            screenOpen = client.screen != null;
            return;
        }
        guiScale = (float) window.getGuiScale();
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        mouseX = clampMouseCoordinate((int) Math.round(client.mouseHandler.getScaledXPos(window)), width);
        mouseY = clampMouseCoordinate((int) Math.round(client.mouseHandler.getScaledYPos(window)), height);
        screenOpen = client.screen != null;
    }

    static int clampMouseCoordinate(int value, int size) {
        if (size <= 0) return 0;
        return Math.max(0, Math.min(size - 1, value));
    }

    public float guiScale() { return guiScale; }
    public int mouseX() { return mouseX; }
    public int mouseY() { return mouseY; }
    public boolean screenOpen() { return screenOpen; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "GUI %.1fx  Mouse %d,%d%s", guiScale, mouseX, mouseY, screenOpen ? "  Screen" : "");
    }
}
