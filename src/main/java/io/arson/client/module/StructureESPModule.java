package io.arson.client.module;

import io.arson.client.render.RenderStyle;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Detects enclosed horizontal passages and connected stair-block runs. */
public final class StructureESPModule extends VisualModule {
    private final BooleanSetting tunnels = setting(new BooleanSetting("tunnels", "Tunnels", true));
    private final BooleanSetting staircases = setting(new BooleanSetting("staircases", "Staircases", true));
    private final BooleanSetting distanceFade = setting(new BooleanSetting("distance-fade", "Distance Fade", false));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 16.0, 8.0, 32.0, 1.0));
    private final DoubleSetting verticalRange = setting(new DoubleSetting("vertical-range", "Vertical Range", 6.0, 2.0, 16.0, 1.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 20.0, 1.0, 20.0, 1.0));
    private final ColorSetting tunnelColor = setting(new ColorSetting("tunnel-color", "Tunnel Color", 0xD855DFFF));
    private final ColorSetting staircaseColor = setting(new ColorSetting("staircase-color", "Staircase Color", 0xD8FFD35A));

    public StructureESPModule() {
        super("structure-esp", "Structure ESP", false);
    }

    public boolean showTunnels() { return tunnels.enabled(); }
    public boolean showStaircases() { return staircases.enabled(); }
    public boolean distanceFade() { return distanceFade.enabled(); }
    public double range() { return range.get(); }
    public double verticalRange() { return verticalRange.get(); }
    public int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }
    public RenderStyle tunnelStyle() { return renderStyle(tunnelColor.get()); }
    public RenderStyle staircaseStyle() { return renderStyle(staircaseColor.get()); }
}
