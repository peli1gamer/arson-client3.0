package com.arson.client.render;

/** Common settings shared by configurable visual modules. */
public final class RenderSettings {
    private RenderColor color = new RenderColor(255, 80, 80, 220);
    private boolean filled = false;
    private boolean outline = true;
    private double opacity = 0.85;

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
}
