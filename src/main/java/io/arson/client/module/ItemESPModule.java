package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Dedicated visualization settings for dropped ItemEntity objects. */
public final class ItemESPModule extends VisualModule {
    private final BooleanSetting showLabels = setting(new BooleanSetting("show-labels", "Show Labels", true));
    private final BooleanSetting showCount = setting(new BooleanSetting("show-count", "Show Count", true));
    private final BooleanSetting labelDistance = setting(new BooleanSetting("label-distance", "Label Distance", true));
    private final BooleanSetting labelBackground = setting(new BooleanSetting("label-background", "Label Background", true));
    private final DoubleSetting labelScale = setting(new DoubleSetting("label-scale", "Label Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting labelHeight = setting(new DoubleSetting("label-height", "Label Height", 0.35, 0.0, 2.0, 0.05));
    private final DoubleSetting labelPadding = setting(new DoubleSetting("label-padding", "Label Padding", 2.0, 0.0, 6.0, 0.5));
    private final ColorSetting labelColor = setting(new ColorSetting("label-color", "Label Color", 0xFFFFFFFF));
    private final ColorSetting labelBackgroundColor = setting(new ColorSetting("label-background-color", "Label Background Color", 0xA0000000));
    private final BooleanSetting distanceFade = setting(new BooleanSetting("distance-fade", "Distance Fade", false));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 128.0, 4.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 2.0, 1.0, 10.0, 1.0));

    public ItemESPModule() {
        super("item-esp", "Item ESP", true);
        this.color.set(0xD6FFDC46);
    }

    public boolean showLabels() { return showLabels.enabled(); }
    public boolean showCount() { return showCount.enabled(); }
    public boolean labelDistance() { return labelDistance.enabled(); }
    public boolean labelBackground() { return labelBackground.enabled(); }
    public float labelScale() { return labelScale.get().floatValue(); }
    public float labelHeight() { return labelHeight.get().floatValue(); }
    public float labelPadding() { return labelPadding.get().floatValue(); }
    public int labelColor() { return labelColor.get(); }
    public int labelBackgroundColor() { return labelBackgroundColor.get(); }
    public boolean distanceFade() { return distanceFade.enabled(); }
    public double range() { return range.get(); }
    public int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }
}
