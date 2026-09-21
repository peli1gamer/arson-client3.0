package io.arson.client.ui;

/** Deterministic keyboard navigation math for responsive screens. */
public final class ResponsiveNavigation {
    private ResponsiveNavigation() {}

    public static int moveIndex(int current, int delta, int size) {
        if (size <= 0) return -1;
        int base = current < 0 ? (delta >= 0 ? -1 : 0) : current;
        return Math.floorMod(base + delta, size);
    }

    public static int moveCategory(int current, int delta, int count) {
        if (count <= 0) return -1;
        return Math.floorMod(current + delta, count);
    }
}
