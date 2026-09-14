package com.arson.client.render;

/** Small adapter shared by world render stages so color/alpha/line-width handling stays consistent. */
public final class RenderStyleUtil {
    private RenderStyleUtil() {
    }

    public static RenderStyle of(RenderColor color, boolean filled, boolean outline,
                                 float fillAlpha, float outlineAlpha, float lineWidth) {
        return new RenderStyle(color, filled, outline, fillAlpha, outlineAlpha, lineWidth);
    }

    public static float[] rgba(RenderStyle style, boolean outline) {
        RenderColor color = outline ? style.outlineColor() : style.fillColor();
        return new float[]{
                color.red() / 255.0f,
                color.green() / 255.0f,
                color.blue() / 255.0f,
                color.alpha() / 255.0f
        };
    }
}
