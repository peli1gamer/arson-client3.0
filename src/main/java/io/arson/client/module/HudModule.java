package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;

/** Configurable HUD module. Drawing is kept separate from state/configuration. */
public final class HudModule extends Module {
    private final BooleanSetting watermark = setting(new BooleanSetting("watermark", "Watermark", true));
    private final BooleanSetting coordinates = setting(new BooleanSetting("coordinates", "Coordinates", true));
    private final BooleanSetting fps = setting(new BooleanSetting("fps", "FPS", true));
    private final DoubleSetting scale = setting(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05));

    public HudModule() {
        super("hud", "HUD", Category.RENDER);
    }

    public boolean showWatermark() { return watermark.enabled(); }
    public boolean showCoordinates() { return coordinates.enabled(); }
    public boolean showFps() { return fps.enabled(); }
    public double scale() { return scale.get(); }
}
