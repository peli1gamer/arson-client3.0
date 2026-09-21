package io.arson.client.ui;

/** Pure responsive layout helpers shared by HUD and ClickGUI. */
public final class ResponsiveUiLayout {
    private ResponsiveUiLayout() {}

    public record Rect(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }
        public boolean contains(int px, int py) { return px >= x && px < right() && py >= y && py < bottom(); }
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Scales a logical design against both axes without becoming unusably tiny or oversized. */
    public static double viewportScale(int viewportWidth, int viewportHeight, int designWidth, int designHeight, double min, double max) {
        if (viewportWidth <= 0 || viewportHeight <= 0) return min;
        double sx = viewportWidth / (double) designWidth;
        double sy = viewportHeight / (double) designHeight;
        return clamp(Math.min(sx, sy), min, max);
    }

    /** Centers a preferred panel, shrinking it to the viewport with a safe inset. */
    public static Rect fitPanel(int viewportWidth, int viewportHeight, int preferredWidth, int preferredHeight, int inset) {
        int safeInset = clamp(inset, 0, Math.max(0, Math.min(viewportWidth, viewportHeight) / 2));
        int width = Math.max(1, Math.min(preferredWidth, Math.max(1, viewportWidth - safeInset * 2)));
        int height = Math.max(1, Math.min(preferredHeight, Math.max(1, viewportHeight - safeInset * 2)));
        int x = Math.max(safeInset, (viewportWidth - width) / 2);
        int y = Math.max(safeInset, (viewportHeight - height) / 2);
        return new Rect(x, y, width, height);
    }

    /** Clamps a box into a viewport while preserving as much of its requested position as possible. */
    public static Rect clampRect(double x, double y, double width, double height, int viewportWidth, int viewportHeight, int inset) {
        int w = Math.max(1, (int) Math.round(width));
        int h = Math.max(1, (int) Math.round(height));
        int maxX = Math.max(inset, viewportWidth - inset - w);
        int maxY = Math.max(inset, viewportHeight - inset - h);
        int ix = clamp((int) Math.round(x), inset, maxX);
        int iy = clamp((int) Math.round(y), inset, maxY);
        return new Rect(ix, iy, Math.min(w, Math.max(1, viewportWidth - inset * 2)), Math.min(h, Math.max(1, viewportHeight - inset * 2)));
    }

    /** Returns a deterministic vertical slot sequence, wrapping to a second column when needed. */
    public static Rect deterministicSlot(int index, int itemWidth, int itemHeight, int gap, int columns, int viewportWidth, int viewportHeight, int inset) {
        int safeColumns = Math.max(1, columns);
        int col = Math.max(0, index) % safeColumns;
        int row = Math.max(0, index) / safeColumns;
        int x = inset + col * (itemWidth + gap);
        int y = inset + row * (itemHeight + gap);
        return clampRect(x, y, itemWidth, itemHeight, viewportWidth, viewportHeight, inset);
    }
}
