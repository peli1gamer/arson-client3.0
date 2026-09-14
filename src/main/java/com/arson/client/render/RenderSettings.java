package com.arson.client.render;

/** Common settings shared by configurable visual modules. */
public final class RenderSettings {
    private RenderColor color = new RenderColor(255, 80, 80, 220);
    private boolean filled = false;
    private boolean outline = true;
    private double opacity = 0.85;
    private double outlineAlpha = 1.0;
    private double lineWidth = 1.0;

    public RenderColor color() { return color; }
    public void color(RenderColor color) { this.color = color; }

    public boolean filled() { return filled; }
    public void filled(boolean filled) { this.filled = filled; }

    public boolean outline() { return outline; }
    public void outline(boolean outline) { this.outline = outline; }

    public double opacity() { return opacity; }
    public void opacity(double opacity) {
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
    }

    public double outlineAlpha() { return outlineAlpha; }
    public void outlineAlpha(double outlineAlpha) {
        this.outlineAlpha = Math.max(0.0, Math.min(1.0, outlineAlpha));
    }

    public double lineWidth() { return lineWidth; }
    public void lineWidth(double lineWidth) {
        this.lineWidth = Math.max(0.5, Math.min(8.0, lineWidth));
    }

    /** Snapshot the mutable GUI settings into an immutable render value. */
    public RenderStyle style() {
        return new RenderStyle(
                color,
                filled,
                outline,
                (float) opacity,
                (float) outlineAlpha,
                (float) lineWidth
        );
    }
}
