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
        return rgba(outline ? style.outlineColor() : style.fillColor());
    }

    public static float[] rgba(RenderStyle style, boolean outline, float alphaOverride) {
        RenderColor base = style.color();
        float alpha = Math.max(0.0f, Math.min(1.0f, alphaOverride));
        return rgba(new RenderColor(base.red(), base.green(), base.blue(), Math.round(alpha * 255.0f)));
    }

    private static float[] rgba(RenderColor color) {
        return new float[]{
                color.red() / 255.0f,
                color.green() / 255.0f,
                color.blue() / 255.0f,
                color.alpha() / 255.0f
        };
    }
}
