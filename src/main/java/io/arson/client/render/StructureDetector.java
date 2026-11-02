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

    record StructureTarget(StructureType type,
                           int minX, int minY, int minZ,
                           int maxX, int maxY, int maxZ) {}

    static StructureTarget detectAt(BlockAccess blocks, int x, int y, int z) {
        if (isTunnelCell(blocks, x, y, z, Direction.EAST)) {
            StructureTarget tunnel = tunnelRun(blocks, x, y, z, Direction.EAST);
            if (tunnel != null) return tunnel;
        }
        if (isTunnelCell(blocks, x, y, z, Direction.SOUTH)) {
            StructureTarget tunnel = tunnelRun(blocks, x, y, z, Direction.SOUTH);
            if (tunnel != null) return tunnel;
        }
        if (isTunnelCell(blocks, x - 1, y, z, Direction.EAST)
                || isTunnelCell(blocks, x, y, z - 1, Direction.SOUTH)) {
            return null;
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
        int previousX = x - direction.getStepX();
        int previousZ = z - direction.getStepZ();
        if (isTunnelCell(blocks, previousX, y, previousZ, direction)) return null;

        int length = 0;
        while (length < MAX_STRUCTURE_LENGTH
                && isTunnelCell(blocks, x + direction.getStepX() * length,
                y, z + direction.getStepZ() * length, direction)) {
            length++;
        }
        if (length < MIN_TUNNEL_LENGTH) return null;

        int maxX = x + (direction.getAxis() == Direction.Axis.X ? length : 1);
        int maxZ = z + (direction.getAxis() == Direction.Axis.Z ? length : 1);
        return new StructureTarget(StructureType.TUNNEL, x, y, z, maxX, y + 2, maxZ);
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
        int previousX = x - direction.getStepX();
        int previousZ = z - direction.getStepZ();
        if (stairAndClear(blocks, previousX, y - verticalStep, previousZ)) return null;

        int length = 0;
        while (length < MAX_STRUCTURE_LENGTH
                && stairAndClear(blocks,
                x + direction.getStepX() * length,
                y + verticalStep * length,
                z + direction.getStepZ() * length)) {
            length++;
        }
        if (length < MIN_STAIR_COUNT) return null;

        int lastY = y + verticalStep * (length - 1);
        int minY = Math.min(y, lastY);
        int maxY = Math.max(y, lastY) + 3;
        int maxX = x + (direction.getAxis() == Direction.Axis.X ? length : 1);
        int maxZ = z + (direction.getAxis() == Direction.Axis.Z ? length : 1);
        return new StructureTarget(StructureType.STAIRCASE, x, minY, z, maxX, maxY, maxZ);
    }

    private static boolean stairAndClear(BlockAccess blocks, int x, int y, int z) {
        return blocks.isStair(x, y, z)
                && blocks.isAir(x, y + 1, z)
                && blocks.isAir(x, y + 2, z);
    }
}
