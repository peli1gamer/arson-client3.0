package io.arson.client.render;

import com.arson.client.render.StorageOverlay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Groups nearby storage targets into connected spatial clusters for base-level summaries. */
public final class StorageClusterDetector {
    private StorageClusterDetector() {}

    public static List<Cluster> detect(List<StorageOverlay.StorageTarget> targets, double radius) {
        if (targets == null || targets.size() < 2 || !Double.isFinite(radius) || radius <= 0.0) return List.of();
        int count = targets.size();
        UnionFind groups = new UnionFind(count);
        Map<Cell, List<Integer>> cells = new HashMap<>();
        double radiusSquared = radius * radius;

        for (int i = 0; i < count; i++) {
            StorageOverlay.StorageTarget target = targets.get(i);
            double cx = target.x() + target.width() * 0.5;
            double cy = target.y() + target.height() * 0.5;
            double cz = target.z() + target.depth() * 0.5;
            Cell cell = Cell.at(cx, cy, cz, radius);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        List<Integer> neighbors = cells.get(new Cell(cell.x + dx, cell.y + dy, cell.z + dz));
                        if (neighbors == null) continue;
                        for (int other : neighbors) {
                            StorageOverlay.StorageTarget candidate = targets.get(other);
                            double ox = candidate.x() + candidate.width() * 0.5;
                            double oy = candidate.y() + candidate.height() * 0.5;
                            double oz = candidate.z() + candidate.depth() * 0.5;
                            double rx = cx - ox, ry = cy - oy, rz = cz - oz;
                            if (rx * rx + ry * ry + rz * rz <= radiusSquared) groups.union(i, other);
                        }
                    }
                }
            }
            cells.computeIfAbsent(cell, ignored -> new ArrayList<>()).add(i);
        }

        Map<Integer, Bounds> bounds = new HashMap<>();
        for (int i = 0; i < count; i++) {
            StorageOverlay.StorageTarget target = targets.get(i);
            bounds.computeIfAbsent(groups.find(i), ignored -> new Bounds()).include(target);
        }
        return bounds.values().stream()
                .filter(bound -> bound.count >= 2)
                .map(Bounds::toCluster)
                .sorted(Comparator.comparingInt(Cluster::count).reversed()
                        .thenComparingDouble(Cluster::minX)
                        .thenComparingDouble(Cluster::minY)
                        .thenComparingDouble(Cluster::minZ))
                .toList();
    }

    public record Cluster(double minX, double minY, double minZ,
                          double maxX, double maxY, double maxZ, int count) {
        public double centerX() { return (minX + maxX) * 0.5; }
        public double centerY() { return (minY + maxY) * 0.5; }
        public double centerZ() { return (minZ + maxZ) * 0.5; }
    }

    private record Cell(int x, int y, int z) {
        static Cell at(double x, double y, double z, double size) {
            return new Cell((int) Math.floor(x / size), (int) Math.floor(y / size), (int) Math.floor(z / size));
        }
    }

    private static final class Bounds {
        private double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        private int count;
        void include(StorageOverlay.StorageTarget target) {
            minX = Math.min(minX, target.x()); minY = Math.min(minY, target.y()); minZ = Math.min(minZ, target.z());
            maxX = Math.max(maxX, target.x() + target.width());
            maxY = Math.max(maxY, target.y() + target.height());
            maxZ = Math.max(maxZ, target.z() + target.depth());
            count++;
        }
        Cluster toCluster() { return new Cluster(minX, minY, minZ, maxX, maxY, maxZ, count); }
    }

    private static final class UnionFind {
        private final int[] parent;
        private final byte[] rank;
        UnionFind(int size) {
            parent = new int[size]; rank = new byte[size];
            for (int i = 0; i < size; i++) parent[i] = i;
        }
        int find(int value) {
            if (parent[value] != value) parent[value] = find(parent[value]);
            return parent[value];
        }
        void union(int left, int right) {
            int rootLeft = find(left), rootRight = find(right);
            if (rootLeft == rootRight) return;
            if (rank[rootLeft] < rank[rootRight]) parent[rootLeft] = rootRight;
            else if (rank[rootLeft] > rank[rootRight]) parent[rootRight] = rootLeft;
            else { parent[rootRight] = rootLeft; rank[rootLeft]++; }
        }
    }
}
