package io.arson.client.module;

import com.arson.client.render.RenderColor;
import io.arson.client.render.RenderStyle;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Common presentation settings shared by all visual modules. */
public abstract class VisualModule extends Module {
    protected final BooleanSetting fill = setting(
            new BooleanSetting("fill", "Fill", false));
    protected final BooleanSetting outline = setting(
            new BooleanSetting("outline", "Outline", true));
    protected final DoubleSetting fillAlpha = setting(
            new DoubleSetting("fill-alpha", "Fill Alpha", 0.30, 0.0, 1.0, 0.05));
    protected final DoubleSetting outlineAlpha = setting(
            new DoubleSetting("outline-alpha", "Outline Alpha", 1.0, 0.0, 1.0, 0.05));
    protected final DoubleSetting lineWidth = setting(
            new DoubleSetting("line-width", "Line Width", 1.0, 0.5, 8.0, 0.5));
    protected final ColorSetting color = setting(
            new ColorSetting("color", "Color", 0xDCEB5B5B));

    protected VisualModule(String id, String name) {
        super(id, name, Category.RENDER);
    }

    public int colorArgb() { return color.get(); }
    public boolean filled() { return fill.enabled(); }
    public boolean outline() { return outline.enabled(); }
    public double fillAlpha() { return fillAlpha.get(); }
    public double outlineAlpha() { return outlineAlpha.get(); }
    public double lineWidth() { return lineWidth.get(); }

    /** Snapshot the mutable GUI settings into immutable render state. */
    public RenderStyle renderStyle() {
        int argb = color.get();
        RenderColor renderColor = new RenderColor(
                (argb >>> 16) & 0xFF,
                (argb >>> 8) & 0xFF,
                argb & 0xFF,
                (argb >>> 24) & 0xFF);
        return new RenderStyle(
                renderColor,
                fill.enabled(),
                outline.enabled(),
                fillAlpha.get().floatValue(),
                outlineAlpha.get().floatValue(),
                lineWidth.get().floatValue());
    }
}
