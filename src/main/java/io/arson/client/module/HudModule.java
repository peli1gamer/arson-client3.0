package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Configurable HUD module. Drawing is kept separate from state/configuration. */
public final class HudModule extends Module {
    private final BooleanSetting watermark = setting(new BooleanSetting("watermark", "Watermark", true));
    private final BooleanSetting coordinates = setting(new BooleanSetting("coordinates", "Coordinates", true));
    private final BooleanSetting fps = setting(new BooleanSetting("fps", "FPS", true));
    private final BooleanSetting background = setting(new BooleanSetting("background", "Background", false));
    private final BooleanSetting shadow = setting(new BooleanSetting("shadow", "Text Shadow", true));
    private final DoubleSetting scale = setting(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting x = setting(new DoubleSetting("x", "X", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting y = setting(new DoubleSetting("y", "Y", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting padding = setting(new DoubleSetting("padding", "Background Padding", 3.0, 0.0, 16.0, 1.0));
    private final DoubleSetting lineSpacing = setting(new DoubleSetting("line-spacing", "Line Spacing", 11.0, 8.0, 24.0, 1.0));
    private final BooleanSetting snap = setting(new BooleanSetting("snap", "Grid Snap", true));
    private final DoubleSetting gridSize = setting(new DoubleSetting("grid-size", "Grid Size", 4.0, 1.0, 32.0, 1.0));
    private final ColorSetting textColor = setting(new ColorSetting("text-color", "Text Color", 0xFFFFFFFF));
    private final ColorSetting secondaryColor = setting(new ColorSetting("secondary-color", "Secondary Color", 0xFFD0D0D0));
    private final ColorSetting backgroundColor = setting(new ColorSetting("background-color", "Background Color", 0x80000000));

    public HudModule() {
        super("hud", "HUD", Category.RENDER);
    }

    public boolean showWatermark() { return watermark.enabled(); }
    public boolean showCoordinates() { return coordinates.enabled(); }
    public boolean showFps() { return fps.enabled(); }
    public boolean showBackground() { return background.enabled(); }
    public boolean showShadow() { return shadow.enabled(); }
    public double scale() { return scale.get(); }
    public int x() { return (int) Math.round(x.get()); }
    public int y() { return (int) Math.round(y.get()); }
    public int padding() { return (int) Math.round(padding.get()); }
    public int lineSpacing() { return (int) Math.round(lineSpacing.get()); }
    public boolean gridSnap() { return snap.enabled(); }
    public int gridSize() { return (int) Math.round(gridSize.get()); }
    public int textColor() { return textColor.get(); }
    public int secondaryColor() { return secondaryColor.get(); }
    public int backgroundColor() { return backgroundColor.get(); }

    public void setEditorPosition(double newX, double newY) {
        double step = gridSnap() ? gridSize() : 1.0;
        x.set(Math.round(Math.max(0.0, newX) / step) * step);
        y.set(Math.round(Math.max(0.0, newY) / step) * step);
    }

    /** Apply a complete HUD layout preset without changing the module enabled state. */
    public void applyPreset(String preset) {
        switch (preset.toLowerCase(java.util.Locale.ROOT)) {
            case "minimal" -> {
                watermark.set(true); coordinates.set(false); fps.set(true); background.set(false); shadow.set(true);
                scale.set(1.0); x.set(6.0); y.set(6.0);
            }
            case "compact" -> {
                watermark.set(true); coordinates.set(true); fps.set(true); background.set(true); shadow.set(true);
                scale.set(0.90); x.set(6.0); y.set(6.0); backgroundColor.set(0x90000000);
            }
            case "full" -> {
                watermark.set(true); coordinates.set(true); fps.set(true); background.set(true); shadow.set(true);
                scale.set(1.10); x.set(10.0); y.set(10.0); backgroundColor.set(0xA0000000);
            }
            default -> throw new IllegalArgumentException("Unknown HUD preset: " + preset);
        }
    }
}
