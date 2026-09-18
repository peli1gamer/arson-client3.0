package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live window and GUI resolution information for render/HUD integrations. */
public final class RenderResolutionInfoModule extends Module {
    private int framebufferWidth;
    private int framebufferHeight;
    private int guiWidth;
    private int guiHeight;
    private double aspectRatio;

    public RenderResolutionInfoModule() {
        super("render-resolution-info", "Render Resolution Info", Category.RENDER,
                "Reports live framebuffer and scaled GUI dimensions plus aspect ratio for HUD layout diagnostics.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.getWindow() == null) {
            framebufferWidth = framebufferHeight = guiWidth = guiHeight = 0;
            aspectRatio = 0;
            return;
        }
        var window = client.getWindow();
        framebufferWidth = window.getWidth();
        framebufferHeight = window.getHeight();
        guiWidth = window.getGuiScaledWidth();
        guiHeight = window.getGuiScaledHeight();
        aspectRatio = framebufferHeight == 0 ? 0 : framebufferWidth / (double) framebufferHeight;
    }

    public int framebufferWidth() { return framebufferWidth; }
    public int framebufferHeight() { return framebufferHeight; }
    public int guiWidth() { return guiWidth; }
    public int guiHeight() { return guiHeight; }
    public double aspectRatio() { return aspectRatio; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "Resolution %dx%d  GUI %dx%d  Aspect %.2f",
                framebufferWidth, framebufferHeight, guiWidth, guiHeight, aspectRatio);
    }
}
