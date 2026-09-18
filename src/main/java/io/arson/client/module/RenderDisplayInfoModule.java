package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live client display and camera presentation state for HUD integrations. */
public final class RenderDisplayInfoModule extends Module {
    private double fov;
    private double guiScale;
    private boolean bobbing;
    private boolean vsync;
    private boolean fullscreen;

    public RenderDisplayInfoModule() {
        super("render-display-info", "Render Display Info", Category.RENDER,
                "Tracks live FOV, GUI scale, view bobbing, VSync, and fullscreen state for visual integrations; it never changes display settings.");
    }

    @Override protected void onTick(Minecraft client) {
        fov = client.options.fov().get();
        guiScale = client.options.guiScale().get();
        bobbing = client.options.bobView().get();
        vsync = client.options.enableVsync().get();
        fullscreen = client.getWindow() != null && client.getWindow().isFullscreen();
    }

    public double fov() { return fov; }
    public double guiScale() { return guiScale; }
    public boolean bobbing() { return bobbing; }
    public boolean vsync() { return vsync; }
    public boolean fullscreen() { return fullscreen; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "Display FOV %.0f  GUI %.0f  Bob %s  VSync %s%s",
                fov, guiScale, bobbing ? "On" : "Off", vsync ? "On" : "Off", fullscreen ? "  Fullscreen" : "");
    }
}
