package io.arson.client.render;

import com.arson.client.render.RenderColor;

/** Canonical render presentation shared by ESP, storage overlays, tracers, and HUD visuals. */
public final class RenderStyle extends com.arson.client.render.RenderStyle {
    public RenderStyle(RenderColor color, boolean fill, boolean outline, float fillAlpha, float outlineAlpha, float lineWidth) {
        super(color, fill, outline, fillAlpha, outlineAlpha, lineWidth);
    }

    public static RenderStyle fromArgb(int argb, boolean fill, boolean outline, double fillAlpha, double lineWidth) {
        RenderColor color = new RenderColor((argb >>> 16) & 0xFF, (argb >>> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
        return new RenderStyle(color, fill, outline, (float) fillAlpha, 1.0f, (float) lineWidth);
    }

    public float[] fillRgba() { return rgba(fillAlpha()); }
    public float[] outlineRgba() { return rgba(outlineAlpha()); }

    private float[] rgba(float multiplier) {
        return new float[]{color().red() / 255.0f, color().green() / 255.0f, color().blue() / 255.0f, (color().alpha() / 255.0f) * multiplier};
    }
}
