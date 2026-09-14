package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Configurable HUD module. Drawing is kept separate from state/configuration. */
public final class HudModule extends Module {
    private final BooleanSetting watermark = setting(new BooleanSetting("watermark", "Watermark", true));
    private final BooleanSetting coordinates = setting(new BooleanSetting("coordinates", "Coordinates", true));
    private final BooleanSetting fps = setting(new BooleanSetting("fps", "FPS", true));
    private final BooleanSetting background = setting(new BooleanSetting("background", "Background", false));
    private final BooleanSetting shadow = setting(new BooleanSetting("shadow", "Text Shadow", true));
    private final DoubleSetting scale = setting(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting x = setting(new DoubleSetting("x", "X", 6.0, 0.0, 500.0, 1.0));
    private final DoubleSetting y = setting(new DoubleSetting("y", "Y", 6.0, 0.0, 500.0, 1.0));
    private final ColorSetting textColor = setting(new ColorSetting("text-color", "Text Color", 0xFFFFFFFF));
    private final ColorSetting secondaryColor = setting(new ColorSetting("secondary-color", "Secondary Color", 0xFFD0D0D0));
    private final ColorSetting backgroundColor = setting(new ColorSetting("background-color", "Background Color", 0x80000000));

    public HudModule() {
        super("hud", "HUD", Category.RENDER);
    }

    public boolean showWatermark() { return watermark.enabled(); }
    public boolean showCoordinates() { return coordinates.enabled(); }
    public boolean showFps() { return fps.enabled(); }
    public boolean showBackground() { return background.enabled(); }
    public boolean showShadow() { return shadow.enabled(); }
    public double scale() { return scale.get(); }
    public int x() { return (int) Math.round(x.get()); }
    public int y() { return (int) Math.round(y.get()); }
    public int textColor() { return textColor.get(); }
    public int secondaryColor() { return secondaryColor.get(); }
    public int backgroundColor() { return backgroundColor.get(); }
}
