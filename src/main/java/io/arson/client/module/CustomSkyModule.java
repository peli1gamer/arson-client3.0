package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;

/** Controls the client-side custom sky shader. */
public final class CustomSkyModule extends Module {
    private final BooleanSetting customSky = setting(new BooleanSetting("custom-sky", "Custom Sky", true));
    private final ColorSetting skyTint = setting(new ColorSetting("sky-tint", "Sky Tint", 0xFF6D9FFF));

    public CustomSkyModule() {
        super("custom-sky", "Custom Sky", Category.RENDER,
                "Applies a local shader tint to the overworld sky disc. Does not alter world or server state.");
        customSky.description("Enable or disable the custom sky shader.");
        skyTint.description("ARGB tint applied to the shader gradient.");
    }

    public boolean customSkyEnabled() { return customSky.enabled(); }
    public int skyTintArgb() { return skyTint.get(); }
}
