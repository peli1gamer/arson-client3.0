package io.arson.client.render;

import net.minecraft.core.Direction;

/** Pure geometry recognition for narrow enclosed tunnels and connected stair runs. */
final class StructureDetector {
    private static final int MIN_TUNNEL_LENGTH = 3;
    private static final int MIN_STAIR_COUNT = 3;
    private static final int MAX_STRUCTURE_LENGTH = 16;

    private StructureDetector() {}

    interface BlockAccess {
        boolean isAir(int x, int y, int z);
        boolean isSolid(int x, int y, int z);
        boolean isStair(int x, int y, int z);
    }

    enum StructureType {
        TUNNEL,
        STAIRCASE
    }

    record StructureTarget(StructureType type, int x, int y, int z,
                           int length, int stepY, boolean alongX) {
        int minX() { return x; }
        int maxX() { return x + (alongX ? length : 1); }
        int minY() { return Math.min(y, y + stepY * (length - 1)); }
        int maxY() {
            return Math.max(y, y + stepY * (length - 1))
                    + (type == StructureType.TUNNEL ? 2 : 3);
        }
        int minZ() { return z; }
        int maxZ() { return z + (alongX ? 1 : length); }
    }

    static StructureTarget detectAt(BlockAccess blocks, int x, int y, int z) {
        if (isTunnelCell(blocks, x, y, z, Direction.EAST)) {
            StructureTarget tunnel = tunnelRun(blocks, x, y, z, Direction.EAST);
            if (tunnel != null) return tunnel;
        }
        if (isTunnelCell(blocks, x, y, z, Direction.SOUTH)) {
            StructureTarget tunnel = tunnelRun(blocks, x, y, z, Direction.SOUTH);
            if (tunnel != null) return tunnel;
        }

        if (!stairAndClear(blocks, x, y, z)) return null;
        StructureTarget staircase = stairRun(blocks, x, y, z, Direction.EAST, 1);
        if (staircase != null) return staircase;
        staircase = stairRun(blocks, x, y, z, Direction.EAST, -1);
        if (staircase != null) return staircase;
        staircase = stairRun(blocks, x, y, z, Direction.SOUTH, 1);
        if (staircase != null) return staircase;
        return stairRun(blocks, x, y, z, Direction.SOUTH, -1);
    }

    private static StructureTarget tunnelRun(BlockAccess blocks, int x, int y, int z, Direction direction) {
        int length = 0;
        while (length < MAX_STRUCTURE_LENGTH
                && isTunnelCell(blocks, x + direction.getStepX() * length,
                y, z + direction.getStepZ() * length, direction)) {
            length++;
        }
        if (length < MIN_TUNNEL_LENGTH) return null;

        return new StructureTarget(StructureType.TUNNEL, x, y, z, length, 0,
                direction.getAxis() == Direction.Axis.X);
    }

    private static boolean isTunnelCell(BlockAccess blocks, int x, int y, int z, Direction direction) {
        if (!blocks.isAir(x, y, z) || !blocks.isAir(x, y + 1, z)) return false;
        if (!blocks.isSolid(x, y - 1, z) || !blocks.isSolid(x, y + 2, z)) return false;

        if (direction.getAxis() == Direction.Axis.X) {
            return wallColumn(blocks, x, y, z - 1) && wallColumn(blocks, x, y, z + 1);
        }
        return wallColumn(blocks, x - 1, y, z) && wallColumn(blocks, x + 1, y, z);
    }

    private static boolean wallColumn(BlockAccess blocks, int x, int y, int z) {
        return blocks.isSolid(x, y, z) && blocks.isSolid(x, y + 1, z);
    }

    private static StructureTarget stairRun(BlockAccess blocks, int x, int y, int z,
                                            Direction direction, int verticalStep) {
        int length = 0;
        while (length < MAX_STRUCTURE_LENGTH
                && stairAndClear(blocks,
                x + direction.getStepX() * length,
                y + verticalStep * length,
                z + direction.getStepZ() * length)) {
            length++;
        }
        if (length < MIN_STAIR_COUNT) return null;

        return new StructureTarget(StructureType.STAIRCASE, x, y, z, length, verticalStep,
                direction.getAxis() == Direction.Axis.X);
    }

    private static boolean stairAndClear(BlockAccess blocks, int x, int y, int z) {
        return blocks.isStair(x, y, z)
                && blocks.isAir(x, y + 1, z)
                && blocks.isAir(x, y + 2, z);
    }
}
