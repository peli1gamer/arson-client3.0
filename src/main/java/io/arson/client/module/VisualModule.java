package io.arson.client.module;

import com.arson.client.render.RenderColor;
import io.arson.client.render.RenderStyle;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Common presentation settings shared by all visual modules. */
public abstract class VisualModule extends Module {
    protected final BooleanSetting fill;
    protected final BooleanSetting outline;
    protected final DoubleSetting fillAlpha;
    protected final DoubleSetting outlineAlpha;
    protected final DoubleSetting lineWidth;
    protected final ColorSetting color;

    protected VisualModule(String id, String name) {
        this(id, name, false);
    }

    protected VisualModule(String id, String name, boolean defaultFill) {
        super(id, name, Category.RENDER);
        this.fill = setting(new BooleanSetting("fill", "Fill", defaultFill));
        this.outline = setting(new BooleanSetting("outline", "Outline", true));
        this.fillAlpha = setting(new DoubleSetting("fill-alpha", "Fill Alpha", 0.30, 0.0, 1.0, 0.05));
        this.outlineAlpha = setting(new DoubleSetting("outline-alpha", "Outline Alpha", 1.0, 0.0, 1.0, 0.05));
        this.lineWidth = setting(new DoubleSetting("line-width", "Line Width", 1.0, 0.5, 8.0, 0.5));
        this.color = setting(new ColorSetting("color", "Color", 0xDCEB5B5B));
    }

    public int colorArgb() { return color.get(); }
    public boolean filled() { return fill.enabled(); }
    public boolean outline() { return outline.enabled(); }
    public double fillAlpha() { return fillAlpha.get(); }
    public double outlineAlpha() { return outlineAlpha.get(); }
    public double lineWidth() { return lineWidth.get(); }

    /** Snapshot the module's current default visual settings. */
    public RenderStyle renderStyle() {
        return renderStyle(color.get());
    }

    /** Snapshot the common visual controls while using a per-target color. */
    protected final RenderStyle renderStyle(int argb) {
        return new RenderStyle(
                color(argb),
                fill.enabled(),
                outline.enabled(),
                fillAlpha.get().floatValue(),
                outlineAlpha.get().floatValue(),
                lineWidth.get().floatValue());
    }

    private static RenderColor color(int argb) {
        return new RenderColor(
                (argb >>> 16) & 0xFF,
                (argb >>> 8) & 0xFF,
                argb & 0xFF,
                (argb >>> 24) & 0xFF);
    }
}
