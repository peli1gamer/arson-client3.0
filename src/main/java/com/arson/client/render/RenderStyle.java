package com.arson.client.render;

/** Shared render appearance compatibility type. New feature code should use io.arson.client.render.RenderStyle. */
public class RenderStyle {
    private final RenderColor color;
    private final boolean filled;
    private final boolean outline;
    private final float fillAlpha;
    private final float outlineAlpha;
    private final float lineWidth;

    public RenderStyle(RenderColor color, boolean filled, boolean outline, float fillAlpha, float outlineAlpha, float lineWidth) {
        this.color = color == null ? new RenderColor(255, 80, 80, 255) : color;
        this.filled = filled;
        this.outline = outline;
        this.fillAlpha = clamp(fillAlpha);
        this.outlineAlpha = clamp(outlineAlpha);
        this.lineWidth = Math.max(0.5f, Math.min(8.0f, lineWidth));
    }

    public RenderColor color() { return color; }
    public boolean filled() { return filled; }
    public boolean fill() { return filled; }
    public boolean outline() { return outline; }
    public float fillAlpha() { return fillAlpha; }
    public float outlineAlpha() { return outlineAlpha; }
    public float lineWidth() { return lineWidth; }
    public static RenderStyle defaults() { return new RenderStyle(new RenderColor(255, 80, 80, 255), false, true, 0.25f, 1.0f, 1.0f); }
    public RenderColor fillColor() { return withAlpha(color, fillAlpha); }
    public RenderColor outlineColor() { return withAlpha(color, outlineAlpha); }
    private static RenderColor withAlpha(RenderColor color, float alpha) { return new RenderColor(color.red(), color.green(), color.blue(), Math.round(alpha * 255.0f)); }
    private static float clamp(float value) { return Math.max(0.0f, Math.min(1.0f, value)); }
}
