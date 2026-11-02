package io.arson.client.render;

import net.minecraft.core.BlockPos;

/** Allocation-free cursor over an inclusive 3D scan volume. */
final class BlockScanCursor {
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int width;
    private final int height;
    private final long total;
    private long index;

    BlockScanCursor(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("Invalid scan bounds");
        }
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.width = maxX - minX + 1;
        this.height = maxY - minY + 1;
        this.total = (long) width * height * (maxZ - minZ + 1L);
    }

    boolean hasNext() {
        return index < total;
    }

    void setPosition(BlockPos.MutableBlockPos target) {
        if (!hasNext()) throw new IllegalStateException("Scan cursor is exhausted");
        long current = index;
        int x = minX + (int) (current % width);
        int y = minY + (int) ((current / width) % height);
        int z = minZ + (int) (current / ((long) width * height));
        target.set(x, y, z);
    }

    void advance() {
        if (hasNext()) index++;
    }

    long processed() {
        return index;
    }

    long total() {
        return total;
    }
}
