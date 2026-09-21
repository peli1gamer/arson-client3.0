package io.arson.client.ui;

import java.util.List;

/** Pure geometry/navigation model for the responsive Arson ClickGUI. */
public final class ClickGuiLayoutModel {
    public enum Mode { FULL, COMPACT, NARROW }
    public enum Focus { SEARCH, CATEGORY, MODULE, ACTION }

    public record Rect(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }
        public boolean contains(double px, double py) { return px >= x && px < right() && py >= y && py < bottom(); }
    }

    public record Geometry(
            Mode mode, int panelX, int panelY, int panelWidth, int panelHeight,
            Rect rail, Rect search, Rect content, Rect footer,
            int columns, int cardWidth, int cardHeight, int cardGap,
            int safeMargin
    ) {
        public Rect moduleCard(int index, int visibleCount) {
            if (columns <= 0) return new Rect(content.x(), content.y(), 0, 0);
            int row = index / columns;
            int column = index % columns;
            int rows = Math.max(1, (visibleCount + columns - 1) / columns);
            int h = Math.max(0, Math.min(cardHeight, content.height() - 52));
            int x = content.x() + column * (cardWidth + cardGap);
            int y = content.y() + 52 + row * (h + cardGap);
            return new Rect(x, y, cardWidth, h);
        }

        public int visibleRows(int visibleCount) {
            return Math.max(1, (visibleCount + columns - 1) / columns);
        }
    }

    private ClickGuiLayoutModel() {}

    public static Geometry compute(int screenWidth, int screenHeight, double requestedScale) {
        int safe = screenWidth < 520 || screenHeight < 360 ? 6 : 12;
        Mode mode = screenWidth < 760 || screenHeight < 520 ? Mode.NARROW
                : screenWidth < 1100 || screenHeight < 700 ? Mode.COMPACT : Mode.FULL;

        double maxScale = mode == Mode.NARROW ? 1.0 : mode == Mode.COMPACT ? 1.10 : 1.25;
        double scale = clamp(requestedScale, 0.72, maxScale);
        int targetW = (int) Math.round(900 * scale);
        int targetH = (int) Math.round(540 * scale);
        int availableW = Math.max(260, screenWidth - safe * 2);
        int availableH = Math.max(220, screenHeight - safe * 2);
        int panelW = Math.min(targetW, availableW);
        int panelH = Math.min(targetH, availableH);

        int x = Math.max(safe, (screenWidth - panelW) / 2);
        int y = Math.max(safe, (screenHeight - panelH) / 2);

        int railW = mode == Mode.NARROW ? 82 : mode == Mode.COMPACT ? 142 : 158;
        int pad = mode == Mode.NARROW ? 8 : 12;
        int gap = mode == Mode.NARROW ? 7 : 10;
        int headerH = mode == Mode.NARROW ? 38 : 46;
        int footerH = mode == Mode.NARROW ? 34 : 42;

        railW = Math.min(railW, Math.max(64, panelW / 3));
        Rect rail = new Rect(x + pad, y + headerH, Math.max(56, railW - pad), Math.max(40, panelH - headerH - footerH - pad));
        int contentX = rail.right() + gap;
        int contentW = Math.max(80, x + panelW - pad - contentX);
        Rect content = new Rect(contentX, y + headerH, contentW, Math.max(40, panelH - headerH - footerH));
        Rect search = new Rect(contentX, y + 9, Math.max(80, contentW), mode == Mode.NARROW ? 20 : 24);
        Rect footer = new Rect(x + pad, y + panelH - footerH - pad / 2, Math.max(80, panelW - pad * 2), footerH);

        int columns = mode == Mode.NARROW ? 1 : contentW >= 560 ? 2 : 1;
        int cardGap = mode == Mode.NARROW ? 7 : 9;
        int cardWidth = Math.max(70, (contentW - cardGap * (columns - 1)) / columns);
        int cardHeight = mode == Mode.NARROW ? 42 : 56;

        return new Geometry(mode, x, y, panelW, panelH, rail, search, content, footer,
                columns, cardWidth, cardHeight, cardGap, safe);
    }

    public static int moveFocus(Focus focus, int current, int delta, int categoryCount, int moduleCount) {
        int count = switch (focus) {
            case CATEGORY -> categoryCount;
            case MODULE -> moduleCount;
            default -> 1;
        };
        if (count <= 0) return -1;
        return Math.floorMod(current + delta, count);
    }

    public static int moveModule(int current, int delta, int moduleCount, int columns) {
        if (moduleCount <= 0) return -1;
        int safeColumns = Math.max(1, columns);
        int next = current + delta;
        if (delta == 1 || delta == -1) return Math.floorMod(next, moduleCount);
        int target = current + delta * safeColumns;
        return Math.max(0, Math.min(moduleCount - 1, target));
    }

    public static <T> T safeGet(List<T> values, int index) {
        return values == null || index < 0 || index >= values.size() ? null : values.get(index);
    }

    private static double clamp(double value, double min, double max) {
        return Double.isNaN(value) || Double.isInfinite(value) ? min : Math.max(min, Math.min(max, value));
    }
}
