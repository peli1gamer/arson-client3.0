package io.arson.client.ui;

/** Pure HUD anchor math shared by editor actions and future resize-aware layouts. */
public final class HudAnchorConstraint {
    public enum Anchor { TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT }
    private HudAnchorConstraint() {}

    public static double[] resolve(Anchor anchor, double viewportWidth, double viewportHeight, double boxWidth, double boxHeight, double offsetX, double offsetY) {
        double baseX = switch (anchor) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0.0;
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> (viewportWidth - boxWidth) / 2.0;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> viewportWidth - boxWidth;
        };
        double baseY = switch (anchor) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0.0;
            case CENTER_LEFT, CENTER, CENTER_RIGHT -> (viewportHeight - boxHeight) / 2.0;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> viewportHeight - boxHeight;
        };
        return new double[]{baseX + offsetX, baseY + offsetY};
    }

    public static double[] offsetFor(Anchor anchor, double viewportWidth, double viewportHeight, double boxWidth, double boxHeight, double x, double y) {
        double[] base = resolve(anchor, viewportWidth, viewportHeight, boxWidth, boxHeight, 0.0, 0.0);
        return new double[]{x - base[0], y - base[1]};
    }
}
