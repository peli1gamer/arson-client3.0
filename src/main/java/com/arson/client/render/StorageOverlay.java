package com.arson.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Builds lightweight render commands from already-discovered storage positions.
 * Range filtering uses the supplied reference point and each target's centre.
 */
public final class StorageOverlay {
    private final StorageRenderProfile profile;
    private double range = 64.0;

    public StorageOverlay(StorageRenderProfile profile) {
        this.profile = profile;
    }

    public StorageRenderProfile profile() {
        return profile;
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

    public List<RenderBox> build(double referenceX, double referenceY, double referenceZ,
                                 List<StorageTarget> targets) {
        return build(referenceX, referenceY, referenceZ, targets,
                type -> new RenderStyle(profile.color(type), true, true, 0.25f, 1.0f, 1.0f));
    }

    public List<RenderBox> build(double referenceX, double referenceY, double referenceZ,
                                 List<StorageTarget> targets,
                                 Function<StorageType, RenderStyle> styleProvider) {
        List<RenderBox> boxes = new ArrayList<>();
        double rangeSquared = range * range;

        for (StorageTarget target : targets) {
            if (!profile.enabled(target.type())) {
                continue;
            }
            double dx = target.x() + target.width() * 0.5 - referenceX;
            double dy = target.y() + target.height() * 0.5 - referenceY;
            double dz = target.z() + target.depth() * 0.5 - referenceZ;
            if (dx * dx + dy * dy + dz * dz > rangeSquared) {
                continue;
            }

            RenderStyle style = styleProvider.apply(target.type());
            if (style == null) {
                continue;
            }
            boxes.add(new RenderBox(
                    target.x(), target.y(), target.z(),
                    target.x() + target.width(),
                    target.y() + target.height(),
                    target.z() + target.depth(),
                    style));
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