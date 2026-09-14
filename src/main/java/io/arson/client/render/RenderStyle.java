package io.arson.client.render;

import com.arson.client.render.RenderColor;

/** Shared immutable render presentation used by ESP, storage overlays, tracers, and future visuals. */
public record RenderStyle(
        RenderColor color,
        boolean fill,
        boolean outline,
        float fillAlpha,
        float outlineAlpha,
        float lineWidth) {

    public RenderStyle {
        if (color == null) throw new IllegalArgumentException("RenderStyle color cannot be null");
        fillAlpha = clamp(fillAlpha);
        outlineAlpha = clamp(outlineAlpha);
        lineWidth = Math.max(1.0f, Math.min(8.0f, lineWidth));
    }

    public static RenderStyle fromArgb(int argb, boolean fill, boolean outline,
                                       double fillAlpha, double lineWidth) {
        RenderColor color = new RenderColor(
                (argb >>> 16) & 0xFF,
                (argb >>> 8) & 0xFF,
                argb & 0xFF,
                (argb >>> 24) & 0xFF);
        return new RenderStyle(color, fill, outline,
                (float) fillAlpha, 1.0f, (float) lineWidth);
    }

    public float[] fillRgba() {
        return rgba(fillAlpha);
    }

    public float[] outlineRgba() {
        return rgba(outlineAlpha);
    }

    private float[] rgba(float multiplier) {
        return new float[]{
                color.red() / 255.0f,
                color.green() / 255.0f,
                color.blue() / 255.0f,
                (color.alpha() / 255.0f) * multiplier
        };
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
