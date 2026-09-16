package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks the client-reported FPS for HUD and addon presentation. */
public final class FpsModule extends Module {
    private int fps;

    public FpsModule() {
        super("fps", "FPS", Category.MISC,
                "Tracks the client-reported frames per second for HUDs and addon integrations; it does not alter rendering.");
    }

    @Override
    protected void onTick(Minecraft client) { fps = client.getFps(); }

    public int fps() { return fps; }
    public String formatted() { return "FPS " + fps; }
}
