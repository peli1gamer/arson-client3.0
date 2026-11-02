package io.arson.client.render;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HoleDetectorTest {
    @Test
    void detectsFullyBlastResistantSingleHoleAsSafe() {
        Grid grid = new Grid();
        addSingleHole(grid, 0, 1, 0, true);

        HoleDetector.HoleTarget target = HoleDetector.detectAt(grid, 0, 1, 0);

        assertNotNull(target);
        assertEquals(1, target.width());
        assertEquals(1, target.depth());
        assertTrue(target.safe());
        assertFalse(target.isDouble());
    }

    @Test
    void detectsEnclosedStoneSingleHoleAsUnsafe() {
        Grid grid = new Grid();
        addSingleHole(grid, 0, 1, 0, false);

        HoleDetector.HoleTarget target = HoleDetector.detectAt(grid, 0, 1, 0);

        assertNotNull(target);
        assertFalse(target.safe());
    }

    @Test
    void detectsDoubleHoleOnceFromItsCanonicalOrigin() {
        Grid grid = new Grid();
        addDoubleHoleX(grid, 0, 1, 0, true);

        HoleDetector.HoleTarget first = HoleDetector.detectAt(grid, 0, 1, 0);
        HoleDetector.HoleTarget second = HoleDetector.detectAt(grid, 1, 1, 0);

        assertNotNull(first);
        assertEquals(2, first.width());
        assertEquals(1, first.depth());
        assertTrue(first.safe());
        assertNull(second);
    }

    @Test
    void rejectsAnOpenSideAndInsufficientHeadroom() {
        Grid openSide = new Grid();
        addSingleHole(openSide, 0, 1, 0, true);
        openSide.removeSolid(-1, 1, 0);
        openSide.removeSolid(-1, 2, 0);
        assertNull(HoleDetector.detectAt(openSide, 0, 1, 0));

        Grid blockedHead = new Grid();
        addSingleHole(blockedHead, 0, 1, 0, true);
        blockedHead.removeAir(0, 2, 0);
        blockedHead.addSolid(0, 2, 0, false);
        assertNull(HoleDetector.detectAt(blockedHead, 0, 1, 0));
    }

    private static void addSingleHole(Grid grid, int x, int y, int z, boolean safe) {
        grid.addAir(x, y, z);
        grid.addAir(x, y + 1, z);
        grid.addSolid(x, y - 1, z, safe);
        addWallColumn(grid, x - 1, y, z, safe);
        addWallColumn(grid, x + 1, y, z, safe);
        addWallColumn(grid, x, y, z - 1, safe);
        addWallColumn(grid, x, y, z + 1, safe);
    }

    private static void addDoubleHoleX(Grid grid, int x, int y, int z, boolean safe) {
        grid.addAir(x, y, z);
        grid.addAir(x, y + 1, z);
        grid.addAir(x + 1, y, z);
        grid.addAir(x + 1, y + 1, z);
        grid.addSolid(x, y - 1, z, safe);
        grid.addSolid(x + 1, y - 1, z, safe);
        addWallColumn(grid, x - 1, y, z, safe);
        addWallColumn(grid, x + 2, y, z, safe);
        addWallColumn(grid, x, y, z - 1, safe);
        addWallColumn(grid, x + 1, y, z - 1, safe);
        addWallColumn(grid, x, y, z + 1, safe);
        addWallColumn(grid, x + 1, y, z + 1, safe);
    }

    private static void addWallColumn(Grid grid, int x, int y, int z, boolean safe) {
        grid.addSolid(x, y, z, safe);
        grid.addSolid(x, y + 1, z, safe);
    }

    private static final class Grid implements HoleDetector.BlockAccess {
        private final Set<String> air = new HashSet<>();
        private final Set<String> solid = new HashSet<>();
        private final Set<String> resistant = new HashSet<>();

        void addAir(int x, int y, int z) { air.add(key(x, y, z)); }
        void removeAir(int x, int y, int z) { air.remove(key(x, y, z)); }
        void addSolid(int x, int y, int z, boolean blastResistant) {
            String key = key(x, y, z);
            solid.add(key);
            if (blastResistant) resistant.add(key);
        }
        void removeSolid(int x, int y, int z) {
            String key = key(x, y, z);
            solid.remove(key);
            resistant.remove(key);
        }
        @Override public boolean isAir(int x, int y, int z) { return air.contains(key(x, y, z)); }
        @Override public boolean isSolid(int x, int y, int z) { return solid.contains(key(x, y, z)); }
        @Override public boolean isBlastResistant(int x, int y, int z) {
            return resistant.contains(key(x, y, z));
        }
        private static String key(int x, int y, int z) { return x + "," + y + "," + z; }
    }
}
