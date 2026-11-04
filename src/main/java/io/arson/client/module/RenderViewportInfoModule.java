package io.arson.client.module;

import net.minecraft.client.MinecraftClient;

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

    @Override protected void onTick(MinecraftClient client) {
        var window = client.getWindow();
        if (window == null) {
            guiWidth = guiHeight = windowWidth = windowHeight = 0;
            screenOpen = client.currentScreen != null;
            return;
        }
        guiWidth = Math.max(0, window.getScaledWidth());
        guiHeight = Math.max(0, window.getScaledHeight());
        windowWidth = Math.max(0, window.getFramebufferWidth());
        windowHeight = Math.max(0, window.getFramebufferHeight());
        screenOpen = client.currentScreen != null;
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