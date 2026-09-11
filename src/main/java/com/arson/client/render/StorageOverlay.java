package com.arson.client.render;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds lightweight render commands from already-discovered storage positions.
 * World scanning and Minecraft-specific drawing stay outside this class.
 */
public final class StorageOverlay {
    private final StorageRenderProfile profile;
    private double range = 64.0;

    public StorageOverlay(StorageRenderProfile profile) {
        this.profile = profile;
    }

    public double range() {
        return range;
    }

    public void range(double range) {
        if (Double.isNaN(range) || Double.isInfinite(range)) {
            throw new IllegalArgumentException("range must be finite");
        }
        this.range = Math.max(1.0, Math.min(256.0, range));
    }

    public List<RenderBox> build(double cameraX, double cameraY, double cameraZ,
                                 List<StorageTarget> targets) {
        List<RenderBox> boxes = new ArrayList<>();
        double rangeSquared = range * range;

        for (StorageTarget target : targets) {
            if (!profile.enabled(target.type())) {
                continue;
            }
            double dx = target.x() - cameraX;
            double dy = target.y() - cameraY;
            double dz = target.z() - cameraZ;
            if (dx * dx + dy * dy + dz * dz > rangeSquared) {
                continue;
            }

            boxes.add(new RenderBox(
                    target.x(), target.y(), target.z(),
                    target.x() + target.width(),
                    target.y() + target.height(),
                    target.z() + target.depth(),
                    profile.color(target.type())));
        }
        return boxes;
    }

    public record StorageTarget(StorageType type, double x, double y, double z,
                                double width, double height, double depth) {
        public StorageTarget {
            if (type == null) throw new IllegalArgumentException("type cannot be null");
            if (width < 0 || height < 0 || depth < 0) {
                throw new IllegalArgumentException("dimensions cannot be negative");
            }
        }
    }
}
