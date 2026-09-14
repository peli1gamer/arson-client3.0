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
        return outline ? style.outlineRgba() : style.fillRgba();
    }
}
