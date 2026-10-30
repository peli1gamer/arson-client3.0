package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.StringSetting;

/** Controls the client-side custom sky shader and optional resource-pack textures. */
public final class CustomSkyModule extends Module {
    public enum SkyMode { GRADIENT, TEXTURE, PANORAMA }

    private final BooleanSetting customSky = setting(new BooleanSetting("custom-sky", "Custom Sky", true));
    private final EnumSetting<SkyMode> skyMode = setting(new EnumSetting<>("sky-mode", "Sky Mode", SkyMode.GRADIENT));
    private final ColorSetting skyTint = setting(new ColorSetting("sky-tint", "Sky Tint", 0xFFFFFFFF));
    private final StringSetting textureId = setting(new StringSetting(
            "sky-texture", "Sky Texture Resource", "arson:textures/sky/custom.png", 192));
    private final StringSetting panoramaId = setting(new StringSetting(
            "sky-panorama", "Panorama Resource", "arson:textures/sky/panorama.png", 192));

    public CustomSkyModule() {
        super("custom-sky", "Custom Sky", Category.RENDER,
                "Renders a local sky gradient, equirectangular texture, or equirectangular panorama. Texture files can be supplied by a resource pack.");
        customSky.description("Enable or disable the custom sky renderer.");
        skyMode.description("Gradient uses the built-in shader. Texture and Panorama use 2:1 equirectangular image resources.");
        skyTint.description("ARGB color multiplier applied to the selected sky.");
        textureId.description("Resource identifier for a 2:1 equirectangular sky texture, for example arson:textures/sky/custom.png.")
                .visibleWhen(() -> customSky.enabled() && skyMode.get() == SkyMode.TEXTURE);
        panoramaId.description("Resource identifier for a 2:1 equirectangular panorama, for example arson:textures/sky/panorama.png.")
                .visibleWhen(() -> customSky.enabled() && skyMode.get() == SkyMode.PANORAMA);
        skyTint.visibleWhen(customSky::enabled);
        skyMode.visibleWhen(customSky::enabled);
    }

    public boolean customSkyEnabled() { return customSky.enabled(); }
    public SkyMode skyMode() { return skyMode.get(); }
    public int skyTintArgb() { return skyTint.get(); }
    public String textureId() { return textureId.get(); }
    public String panoramaId() { return panoramaId.get(); }
}
