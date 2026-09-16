package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live client/render presentation state for HUDs and addon integrations. */
public final class RenderInfoModule extends Module {
    private int fps;
    private int width;
    private int height;
    private int guiScale;
    private boolean fullscreen;

    public RenderInfoModule() {
        super("render-info", "Render Info", Category.RENDER,
                "Tracks live FPS, GUI viewport size, GUI scale, and fullscreen state for visual HUD integrations; it does not change rendering settings.");
    }

    @Override
    protected void onTick(Minecraft client) {
        fps = client.getFps();
        if (client.getWindow() == null) {
            width = height = guiScale = 0;
            fullscreen = false;
            return;
        }
        width = client.getWindow().getGuiScaledWidth();
        height = client.getWindow().getGuiScaledHeight();
        guiScale = client.options.guiScale().get();
        fullscreen = client.getWindow().isFullscreen();
    }

    public int fps() { return fps; }
    public int width() { return width; }
    public int height() { return height; }
    public int guiScale() { return guiScale; }
    public boolean fullscreen() { return fullscreen; }
    public String formatted() {
        return String.format(java.util.Locale.ROOT, "Render %dx%d  Scale %d  FPS %d%s", width, height, guiScale, fps, fullscreen ? "  Fullscreen" : "");
    }
}
