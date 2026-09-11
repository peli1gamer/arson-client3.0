package com.arson.client.render;

/** Base for visual modules. Rendering implementations can consume these settings without duplicating configuration. */
public abstract class RenderModule {
    private final RenderSettings renderSettings = new RenderSettings();

    public final RenderSettings renderSettings() {
        return renderSettings;
    }

    /** Called by the render dispatcher when this module is enabled. */
    public void render(float tickDelta) {
        // Intentionally empty: concrete modules opt into the render stage they need.
    }
}
