package io.arson.client.render;

import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageClusterDetectorTest {
    @Test
    void groupsNearbyTargetsAndLeavesIsolatedStorageOutOfClusters() {
        List<StorageOverlay.StorageTarget> targets = List.of(
                target(0, 64, 0),
                target(4, 64, 0),
                target(40, 64, 0),
                target(41, 64, 1));

        List<StorageClusterDetector.Cluster> clusters = StorageClusterDetector.detect(targets, 6.0);

        assertEquals(2, clusters.size());
        assertEquals(2, clusters.get(0).count());
        assertEquals(2, clusters.get(1).count());
        assertEquals(2.5, clusters.get(0).centerX(), 0.001);
        assertEquals(41.0, clusters.get(1).centerX(), 0.001);
    }

    @Test
    void emptySingletonAndInvalidRadiusProduceNoClusters() {
        assertTrue(StorageClusterDetector.detect(List.of(), 8.0).isEmpty());
        assertTrue(StorageClusterDetector.detect(List.of(target(0, 0, 0)), 8.0).isEmpty());
        assertTrue(StorageClusterDetector.detect(List.of(target(0, 0, 0), target(1, 0, 0)), 0.0).isEmpty());
    }

    private static StorageOverlay.StorageTarget target(double x, double y, double z) {
        return new StorageOverlay.StorageTarget(StorageType.CHEST, x, y, z, 1.0, 1.0, 1.0);
    }
}
