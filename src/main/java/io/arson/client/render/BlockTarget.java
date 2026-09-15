package io.arson.client.render;

import com.arson.client.render.RenderStyle;

/** Cached block target handed from block discovery to the world renderer. */
public record BlockTarget(double minX, double minY, double minZ,
                          double maxX, double maxY, double maxZ,
                          RenderStyle style) {
    public double centerX() { return (minX + maxX) * 0.5; }
    public double centerY() { return (minY + maxY) * 0.5; }
    public double centerZ() { return (minZ + maxZ) * 0.5; }
}
