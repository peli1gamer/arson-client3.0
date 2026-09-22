package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live GUI and framebuffer viewport dimensions for HUD and render integrations. */
public final class RenderViewportInfoModule extends Module {
    private int guiWidth;
    private int guiHeight;
    private int windowWidth;
    private int windowHeight;
    private boolean screenOpen;

    public RenderViewportInfoModule() {
        super("render-viewport-info", "Render Viewport Info", Category.RENDER,
                "Reports live GUI and framebuffer viewport dimensions plus whether a client screen is open.");
    }

    @Override protected void onTick(Minecraft client) {
        var window = client.getWindow();
        if (window == null) {
            guiWidth = guiHeight = windowWidth = windowHeight = 0;
            screenOpen = client.screen != null;
            return;
        }
        guiWidth = Math.max(0, window.getGuiScaledWidth());
        guiHeight = Math.max(0, window.getGuiScaledHeight());
        windowWidth = Math.max(0, window.getWidth());
        windowHeight = Math.max(0, window.getHeight());
        screenOpen = client.screen != null;
    }

    public int guiWidth() { return guiWidth; }
    public int guiHeight() { return guiHeight; }
    public int windowWidth() { return windowWidth; }
    public int windowHeight() { return windowHeight; }
    public boolean screenOpen() { return screenOpen; }

    public String formatted() {
        return "Viewport GUI " + guiWidth + "x" + guiHeight + "  Window " + windowWidth + "x" + windowHeight
                + (screenOpen ? "  Screen" : "  Gameplay");
    }
}