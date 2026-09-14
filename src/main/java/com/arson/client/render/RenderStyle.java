package com.arson.client.render;

/** Immutable render appearance shared by world-space visual stages. */
public record RenderStyle(
        RenderColor color,
        boolean filled,
        boolean outline,
        float fillAlpha,
        float outlineAlpha,
        float lineWidth
) {
    public RenderStyle {
        color = color == null ? new RenderColor(255, 80, 80, 255) : color;
        fillAlpha = clamp(fillAlpha);
        outlineAlpha = clamp(outlineAlpha);
        lineWidth = Math.max(0.5f, Math.min(8.0f, lineWidth));
    }

    public static RenderStyle defaults() {
        return new RenderStyle(new RenderColor(255, 80, 80, 255), false, true, 0.25f, 1.0f, 1.0f);
    }

    public RenderColor fillColor() {
        return withAlpha(color, fillAlpha);
    }

    public RenderColor outlineColor() {
        return withAlpha(color, outlineAlpha);
    }

    private static RenderColor withAlpha(RenderColor color, float alpha) {
        return new RenderColor(color.red(), color.green(), color.blue(), Math.round(alpha * 255.0f));
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
