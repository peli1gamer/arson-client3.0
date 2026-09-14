package io.arson.client.render;

import com.arson.client.render.RenderColor;

/** Immutable render target produced by the entity discovery layer. */
public record EntityTarget(
        EntityType type,
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ,
        float health,
        float maxHealth,
        RenderColor color,
        String displayName,
        double distance) {

    public double centerX() { return (minX + maxX) * 0.5; }
    public double centerY() { return (minY + maxY) * 0.5; }
    public double centerZ() { return (minZ + maxZ) * 0.5; }
}
