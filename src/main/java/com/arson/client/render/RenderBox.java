package com.arson.client.render;

/** Renderer-agnostic axis-aligned box used as the hand-off between detection and drawing. */
public record RenderBox(double minX, double minY, double minZ,
                        double maxX, double maxY, double maxZ,
                        RenderColor color) {
    public RenderBox {
        if (maxX < minX || maxY < minY || maxZ < minZ) {
            throw new IllegalArgumentException("RenderBox maximum must not be smaller than minimum");
        }
        if (color == null) {
            throw new IllegalArgumentException("RenderBox color cannot be null");
        }
    }

    public double centerX() { return (minX + maxX) * 0.5; }
    public double centerY() { return (minY + maxY) * 0.5; }
    public double centerZ() { return (minZ + maxZ) * 0.5; }
}
