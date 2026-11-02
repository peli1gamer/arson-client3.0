package io.arson.client.render;

/**
 * Pure geometry classifier for standard 1x1 and 2x1 / 1x2 player holes.
 * The supplied access object must treat unloaded positions as unavailable.
 */
final class HoleDetector {
    private HoleDetector() {}

    interface BlockAccess {
        boolean isAir(int x, int y, int z);
        boolean isSolid(int x, int y, int z);
        boolean isBlastResistant(int x, int y, int z);
    }

    record HoleTarget(int x, int y, int z, int width, int depth, boolean safe) {
        boolean isDouble() { return width * depth == 2; }
    }

    static HoleTarget detectAt(BlockAccess blocks, int x, int y, int z) {
        HoleTarget doubleX = doubleX(blocks, x, y, z);
        if (doubleX != null) return doubleX;

        HoleTarget doubleZ = doubleZ(blocks, x, y, z);
        if (doubleZ != null) return doubleZ;

        // A pair is emitted only from its negative-side origin.
        if (doubleX(blocks, x - 1, y, z) != null || doubleZ(blocks, x, y, z - 1) != null) {
            return null;
        }

        return single(blocks, x, y, z);
    }

    private static HoleTarget single(BlockAccess blocks, int x, int y, int z) {
        if (!openColumn(blocks, x, y, z)) return null;

        int safety = floorSafety(blocks, x, y - 1, z);
        safety = combine(safety, wallColumnSafety(blocks, x - 1, y, z));
        safety = combine(safety, wallColumnSafety(blocks, x + 1, y, z));
        safety = combine(safety, wallColumnSafety(blocks, x, y, z - 1));
        safety = combine(safety, wallColumnSafety(blocks, x, y, z + 1));
        if (safety == 0) return null;

        return new HoleTarget(x, y, z, 1, 1, safety == 2);
    }

    private static HoleTarget doubleX(BlockAccess blocks, int x, int y, int z) {
        if (!openColumn(blocks, x, y, z) || !openColumn(blocks, x + 1, y, z)) return null;

        int safety = floorSafety(blocks, x, y - 1, z);
        safety = combine(safety, floorSafety(blocks, x + 1, y - 1, z));
        safety = combine(safety, wallColumnSafety(blocks, x - 1, y, z));
        safety = combine(safety, wallColumnSafety(blocks, x + 2, y, z));
        safety = combine(safety, wallColumnSafety(blocks, x, y, z - 1));
        safety = combine(safety, wallColumnSafety(blocks, x + 1, y, z - 1));
        safety = combine(safety, wallColumnSafety(blocks, x, y, z + 1));
        safety = combine(safety, wallColumnSafety(blocks, x + 1, y, z + 1));
        if (safety == 0) return null;

        return new HoleTarget(x, y, z, 2, 1, safety == 2);
    }

    private static HoleTarget doubleZ(BlockAccess blocks, int x, int y, int z) {
        if (!openColumn(blocks, x, y, z) || !openColumn(blocks, x, y, z + 1)) return null;

        int safety = floorSafety(blocks, x, y - 1, z);
        safety = combine(safety, floorSafety(blocks, x, y - 1, z + 1));
        safety = combine(safety, wallColumnSafety(blocks, x, y, z - 1));
        safety = combine(safety, wallColumnSafety(blocks, x, y, z + 2));
        safety = combine(safety, wallColumnSafety(blocks, x - 1, y, z));
        safety = combine(safety, wallColumnSafety(blocks, x - 1, y, z + 1));
        safety = combine(safety, wallColumnSafety(blocks, x + 1, y, z));
        safety = combine(safety, wallColumnSafety(blocks, x + 1, y, z + 1));
        if (safety == 0) return null;

        return new HoleTarget(x, y, z, 1, 2, safety == 2);
    }

    private static boolean openColumn(BlockAccess blocks, int x, int y, int z) {
        return blocks.isAir(x, y, z) && blocks.isAir(x, y + 1, z);
    }

    private static int floorSafety(BlockAccess blocks, int x, int y, int z) {
        if (!blocks.isSolid(x, y, z)) return 0;
        return blocks.isBlastResistant(x, y, z) ? 2 : 1;
    }

    private static int wallColumnSafety(BlockAccess blocks, int x, int y, int z) {
        if (!blocks.isSolid(x, y, z) || !blocks.isSolid(x, y + 1, z)) return 0;
        return blocks.isBlastResistant(x, y, z) && blocks.isBlastResistant(x, y + 1, z) ? 2 : 1;
    }

    /** 0 = invalid geometry, 1 = enclosed but unsafe, 2 = blast-resistant safe hole. */
    private static int combine(int current, int next) {
        if (current == 0 || next == 0) return 0;
        return current == 1 || next == 1 ? 1 : 2;
    }
}
