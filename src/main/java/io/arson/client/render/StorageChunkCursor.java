package io.arson.client.render;

/** Allocation-free cursor over an inclusive 2D chunk rectangle. */
final class StorageChunkCursor {
    private final int minX;
    private final int minZ;
    private final int width;
    private final long total;
    private long index;

    StorageChunkCursor(int minX, int minZ, int maxX, int maxZ) {
        if (minX > maxX || minZ > maxZ) throw new IllegalArgumentException("Invalid chunk bounds");
        this.minX = minX;
        this.minZ = minZ;
        this.width = maxX - minX + 1;
        this.total = (long) width * (maxZ - minZ + 1L);
    }

    boolean hasNext() { return index < total; }
    int nextX() {
        ensureNext();
        return minX + (int) (index % width);
    }
    int nextZ() {
        ensureNext();
        return minZ + (int) (index / width);
    }
    void advance() { if (hasNext()) index++; }
    long processed() { return index; }
    long total() { return total; }
    private void ensureNext() {
        if (!hasNext()) throw new IllegalStateException("Chunk cursor is exhausted");
    }
}