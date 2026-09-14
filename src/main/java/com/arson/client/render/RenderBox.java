package com.arson.client.render;

/** Renderer-agnostic axis-aligned box used as the hand-off between detection and drawing. */
public record RenderBox(double minX, double minY, double minZ,
                        double maxX, double maxY, double maxZ,
                        RenderStyle style) {
    public RenderBox {
        if (maxX < minX || maxY < minY || maxZ < minZ) {
            throw new IllegalArgumentException("RenderBox maximum must not be smaller than minimum");
        }
        if (style == null) {
            throw new IllegalArgumentException("RenderBox style cannot be null");
        }
    }

    /** Compatibility constructor for discovery code that only has a color. */
    public RenderBox(double minX, double minY, double minZ,
                     double maxX, double maxY, double maxZ,
                     RenderColor color) {
        this(minX, minY, minZ, maxX, maxY, maxZ,
                new RenderStyle(color, true, true, 0.25f, 1.0f, 1.0f));
    }

    /** Backwards-compatible color accessor for older discovery code. */
    public RenderColor color() {
        return style.color();
    }

    public double centerX() { return (minX + maxX) * 0.5; }
    public double centerY() { return (minY + maxY) * 0.5; }
    public double centerZ() { return (minZ + maxZ) * 0.5; }
}
