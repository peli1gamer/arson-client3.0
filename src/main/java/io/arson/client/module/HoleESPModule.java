package io.arson.client.module;

import io.arson.client.render.RenderStyle;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Detects enclosed one-block and two-block player holes around the local player. */
public final class HoleESPModule extends VisualModule {
    private final BooleanSetting safeHoles = setting(new BooleanSetting("safe-holes", "Safe Holes", true));
    private final BooleanSetting unsafeHoles = setting(new BooleanSetting("unsafe-holes", "Unsafe Holes", true));
    private final BooleanSetting singleHoles = setting(new BooleanSetting("single-holes", "Single Holes", true));
    private final BooleanSetting doubleHoles = setting(new BooleanSetting("double-holes", "Double Holes", true));
    private final BooleanSetting distanceFade = setting(new BooleanSetting("distance-fade", "Distance Fade", false));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 16.0, 8.0, 32.0, 1.0));
    private final DoubleSetting verticalRange = setting(new DoubleSetting("vertical-range", "Vertical Range", 6.0, 2.0, 16.0, 1.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 20.0, 1.0, 20.0, 1.0));
    private final ColorSetting safeColor = setting(new ColorSetting("safe-color", "Safe Hole Color", 0xD655FF78));
    private final ColorSetting unsafeColor = setting(new ColorSetting("unsafe-color", "Unsafe Hole Color", 0xD8FF5555));

    public HoleESPModule() {
        super("hole-esp", "Hole ESP", false);
    }

    public boolean showSafeHoles() { return safeHoles.enabled(); }
    public boolean showUnsafeHoles() { return unsafeHoles.enabled(); }
    public boolean showSingleHoles() { return singleHoles.enabled(); }
    public boolean showDoubleHoles() { return doubleHoles.enabled(); }
    public boolean distanceFade() { return distanceFade.enabled(); }
    public double range() { return range.get(); }
    public double verticalRange() { return verticalRange.get(); }
    public int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }

    public RenderStyle safeStyle() { return renderStyle(safeColor.get()); }
    public RenderStyle unsafeStyle() { return renderStyle(unsafeColor.get()); }
}
