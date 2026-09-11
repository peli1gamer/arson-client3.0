package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Common configuration for visual modules. Rendering is kept separate from module logic. */
public abstract class VisualModule extends Module {
    protected final BooleanSetting filled = setting(
            new BooleanSetting("filled", "Filled", false));
    protected final BooleanSetting outline = setting(
            new BooleanSetting("outline", "Outline", true));
    protected final DoubleSetting opacity = setting(
            new DoubleSetting("opacity", "Opacity", 0.85, 0.0, 1.0, 0.05));
    protected final ColorSetting color = setting(
            new ColorSetting("color", "Color", 0xDCEB5B5B));

    protected VisualModule(String id, String name) {
        super(id, name, Category.RENDER);
    }

    public int colorArgb() { return color.get(); }
    public double opacity() { return opacity.get(); }
    public boolean filled() { return filled.enabled(); }
    public boolean outline() { return outline.enabled(); }
}
