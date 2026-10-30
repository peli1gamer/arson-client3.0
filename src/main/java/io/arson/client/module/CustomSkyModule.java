package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.StringSetting;

/** Controls the client-side custom sky shader and optional resource-pack textures. */
public final class CustomSkyModule extends Module {
    public enum SkyMode { GRADIENT, TEXTURE, PANORAMA }
    public enum GradientPreset { BLUE_HOUR, SUNSET, PURPLE, EMERALD, MONOCHROME }

    private final BooleanSetting customSky = setting(new BooleanSetting("custom-sky", "Custom Sky", true));
    private final EnumSetting<SkyMode> skyMode = setting(new EnumSetting<>("sky-mode", "Sky Mode", SkyMode.GRADIENT));
    private final EnumSetting<GradientPreset> gradientPreset = setting(new EnumSetting<>(
            "gradient-preset", "Gradient Preset", GradientPreset.BLUE_HOUR));
    private final ColorSetting skyTint = setting(new ColorSetting("sky-tint", "Sky Tint", 0xFFFFFFFF));
    private final DoubleSetting rotation = setting(new DoubleSetting("sky-rotation", "Sky Rotation", 0.0, -180.0, 180.0, 1.0));
    private final DoubleSetting verticalOffset = setting(new DoubleSetting("sky-vertical-offset", "Vertical Offset", 0.0, -45.0, 45.0, 1.0));
    private final StringSetting textureId = setting(new StringSetting(
            "sky-texture", "Sky Texture Resource", "arson:textures/sky/custom.png", 192));
    private final StringSetting panoramaId = setting(new StringSetting(
            "sky-panorama", "Panorama Resource", "arson:textures/sky/panorama.png", 192));

    public CustomSkyModule() {
        super("custom-sky", "Custom Sky", Category.RENDER,
                "Renders a local sky gradient, equirectangular texture, or equirectangular panorama. Texture files can be supplied by a resource pack.");
        customSky.description("Enable or disable the custom sky renderer.");
        skyMode.description("Gradient uses the built-in shader. Texture and Panorama use 2:1 equirectangular image resources.");
        gradientPreset.description("Selects a built-in color treatment for the gradient sky.")
                .visibleWhen(() -> customSky.enabled() && skyMode.get() == SkyMode.GRADIENT);
        skyTint.description("ARGB color multiplier applied to the selected sky.");
        rotation.description("Rotates the texture or panorama around the vertical axis.")
                .visibleWhen(() -> customSky.enabled() && skyMode.get() != SkyMode.GRADIENT);
        verticalOffset.description("Tilts the texture or panorama vertically.")
                .visibleWhen(() -> customSky.enabled() && skyMode.get() != SkyMode.GRADIENT);
        textureId.description("Resource identifier for a 2:1 equirectangular sky texture, for example arson:textures/sky/custom.png.")
                .visibleWhen(() -> customSky.enabled() && skyMode.get() == SkyMode.TEXTURE);
        panoramaId.description("Resource identifier for a 2:1 equirectangular panorama, for example arson:textures/sky/panorama.png.")
                .visibleWhen(() -> customSky.enabled() && skyMode.get() == SkyMode.PANORAMA);
        skyTint.visibleWhen(customSky::enabled);
        skyMode.visibleWhen(customSky::enabled);
    }

    public boolean customSkyEnabled() { return customSky.enabled(); }
    public SkyMode skyMode() { return skyMode.get(); }
    public GradientPreset gradientPreset() { return gradientPreset.get(); }
    public int skyTintArgb() { return skyTint.get(); }
    public double rotationDegrees() { return rotation.get(); }
    public double verticalOffsetDegrees() { return verticalOffset.get(); }
    public String textureId() { return textureId.get(); }
    public String panoramaId() { return panoramaId.get(); }

    /** Preset tint is combined with the user's tint instead of replacing it. */
    public int gradientPresetArgb() {
        return switch (gradientPreset.get()) {
            case BLUE_HOUR -> 0xFFFFFFFF;
            case SUNSET -> 0xFFFFB080;
            case PURPLE -> 0xFFD5B5FF;
            case EMERALD -> 0xFF9BE0C0;
            case MONOCHROME -> 0xFFD0D0D0;
        };
    }
}
