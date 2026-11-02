package io.arson.client.render;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StructureDetectorTest {
    @Test
    void detectsAThreeBlockEnclosedTunnel() {
        Grid grid = new Grid();
        addTunnelX(grid, 0, 1, 0, 3);

        StructureDetector.StructureTarget target = StructureDetector.detectAt(grid, 0, 1, 0);

        assertNotNull(target);
        assertEquals(StructureDetector.StructureType.TUNNEL, target.type());
        assertEquals(3, target.length());
        assertTrue(target.alongX());
        assertEquals(3, target.maxX());
        assertEquals(2, target.maxY() - target.minY());
    }

    @Test
    void rejectsTunnelGeometryWithAnOpenSide() {
        Grid grid = new Grid();
        addTunnelX(grid, 0, 1, 0, 3);
        grid.removeSolid(1, 1, -1);
        grid.removeSolid(1, 2, -1);

        assertNull(StructureDetector.detectAt(grid, 0, 1, 0));
    }

    @Test
    void detectsConnectedAscendingStaircase() {
        Grid grid = new Grid();
        addStair(grid, 0, 1, 0);
        addStair(grid, 1, 2, 0);
        addStair(grid, 2, 3, 0);

        StructureDetector.StructureTarget target = StructureDetector.detectAt(grid, 0, 1, 0);

        assertNotNull(target);
        assertEquals(StructureDetector.StructureType.STAIRCASE, target.type());
        assertEquals(3, target.length());
        assertEquals(1, target.stepY());
        assertTrue(target.alongX());
        assertEquals(3, target.maxX());
        assertEquals(6, target.maxY());
    }

    @Test
    void rejectsShortStairRuns() {
        Grid grid = new Grid();
        addStair(grid, 0, 1, 0);
        addStair(grid, 1, 2, 0);

        assertNull(StructureDetector.detectAt(grid, 0, 1, 0));
    }

    private static void addTunnelX(Grid grid, int x, int y, int z, int length) {
        for (int offset = 0; offset < length; offset++) {
            int currentX = x + offset;
            grid.addAir(currentX, y, z);
            grid.addAir(currentX, y + 1, z);
            grid.addSolid(currentX, y - 1, z);
            grid.addSolid(currentX, y + 2, z);
            grid.addSolid(currentX, y, z - 1);
            grid.addSolid(currentX, y + 1, z - 1);
            grid.addSolid(currentX, y, z + 1);
            grid.addSolid(currentX, y + 1, z + 1);
        }
    }

    private static void addStair(Grid grid, int x, int y, int z) {
        grid.addStair(x, y, z);
        grid.addAir(x, y + 1, z);
        grid.addAir(x, y + 2, z);
    }

    private static final class Grid implements StructureDetector.BlockAccess {
        private final Set<String> air = new HashSet<>();
        private final Set<String> solid = new HashSet<>();
        private final Set<String> stairs = new HashSet<>();

        void addAir(int x, int y, int z) { air.add(key(x, y, z)); }
        void addSolid(int x, int y, int z) { solid.add(key(x, y, z)); }
        void removeSolid(int x, int y, int z) { solid.remove(key(x, y, z)); }
        void addStair(int x, int y, int z) { stairs.add(key(x, y, z)); }
        @Override public boolean isAir(int x, int y, int z) { return air.contains(key(x, y, z)); }
        @Override public boolean isSolid(int x, int y, int z) { return solid.contains(key(x, y, z)); }
        @Override public boolean isStair(int x, int y, int z) { return stairs.contains(key(x, y, z)); }
        private static String key(int x, int y, int z) { return x + "," + y + "," + z; }
    }
}
