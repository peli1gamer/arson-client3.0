package io.arson.client.render;

import com.arson.client.render.RenderBox;

import java.util.ArrayList;
import java.util.List;

/** Converts discovered entities into camera-relative-independent render boxes. */
public final class EntityOverlay {
    private double range = 64.0;

    public void range(double range) {
        this.range = Math.max(1.0, range);
    }

    public double range() {
        return range;
    }

    public List<RenderBox> build(double cameraX, double cameraY, double cameraZ,
                                 List<EntityTarget> targets) {
        double maxDistanceSq = range * range;
        List<RenderBox> result = new ArrayList<>(targets.size());

        for (EntityTarget target : targets) {
            double dx = target.centerX() - cameraX;
            double dy = target.centerY() - cameraY;
            double dz = target.centerZ() - cameraZ;
            if (dx * dx + dy * dy + dz * dz > maxDistanceSq) {
                continue;
            }

            result.add(new RenderBox(
                    target.minX(), target.minY(), target.minZ(),
                    target.maxX(), target.maxY(), target.maxZ(),
                    target.color()));
        }
        return List.copyOf(result);
    }
}
