package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.EnumSetting;
import io.arson.client.settings.StringSetting;

/** Configurable HUD module. Drawing is kept separate from state/configuration. */
public final class HudModule extends Module {
    public enum RowFormat { STACKED, COMPACT, DENSE, TWO_COLUMN }
    private final BooleanSetting watermark = setting(new BooleanSetting("watermark", "Watermark", true));
    private final StringSetting watermarkText = setting(new StringSetting("watermark-text", "Watermark Text", "Arson V3", 32));
    private final BooleanSetting coordinates = setting(new BooleanSetting("coordinates", "Coordinates", true));
    private final BooleanSetting fps = setting(new BooleanSetting("fps", "FPS", true));
    private final BooleanSetting playerInfo = setting(new BooleanSetting("player-info", "Player Info Element", true));
    private final BooleanSetting worldInfo = setting(new BooleanSetting("world-info", "World Info Element", true));
    private final BooleanSetting background = setting(new BooleanSetting("background", "Background", false));
    private final BooleanSetting shadow = setting(new BooleanSetting("shadow", "Text Shadow", true));
    private final DoubleSetting scale = setting(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting x = setting(new DoubleSetting("x", "X", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting y = setting(new DoubleSetting("y", "Y", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting coordinatesX = setting(new DoubleSetting("coordinates-x", "Coordinates X", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting coordinatesY = setting(new DoubleSetting("coordinates-y", "Coordinates Y", 28.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting fpsX = setting(new DoubleSetting("fps-x", "FPS X", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting fpsY = setting(new DoubleSetting("fps-y", "FPS Y", 39.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting playerInfoX = setting(new DoubleSetting("player-info-x", "Player Info X", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting playerInfoY = setting(new DoubleSetting("player-info-y", "Player Info Y", 50.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting worldInfoX = setting(new DoubleSetting("world-info-x", "World Info X", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting worldInfoY = setting(new DoubleSetting("world-info-y", "World Info Y", 94.0, 0.0, 1000.0, 1.0));
    private final StringSetting watermarkAlign = setting(new StringSetting("watermark-align", "Watermark Align", "left", 8));
    private final StringSetting coordinatesAlign = setting(new StringSetting("coordinates-align", "Coordinates Align", "left", 8));
    private final StringSetting fpsAlign = setting(new StringSetting("fps-align", "FPS Align", "left", 8));
    private final StringSetting playerInfoAlign = setting(new StringSetting("player-info-align", "Player Info Align", "left", 8));
    private final StringSetting worldInfoAlign = setting(new StringSetting("world-info-align", "World Info Align", "left", 8));

    private final ColorSetting watermarkColor = setting(new ColorSetting("watermark-color", "Watermark Color", 0xFFFFFFFF));
    private final ColorSetting coordinatesColor = setting(new ColorSetting("coordinates-color", "Coordinates Color", 0xFFD0D0D0));
    private final ColorSetting fpsColor = setting(new ColorSetting("fps-color", "FPS Color", 0xFFD0D0D0));
    private final ColorSetting playerInfoColor = setting(new ColorSetting("player-info-color", "Player Info Color", 0xFFD0D0D0));
    private final ColorSetting worldInfoColor = setting(new ColorSetting("world-info-color", "World Info Color", 0xFFD0D0D0));
    private final ColorSetting elementBackgroundColor = setting(new ColorSetting("element-background-color", "Element Background Color", 0x80000000));
    private final DoubleSetting watermarkScale = setting(new DoubleSetting("watermark-scale", "Watermark Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting coordinatesScale = setting(new DoubleSetting("coordinates-scale", "Coordinates Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting fpsScale = setting(new DoubleSetting("fps-scale", "FPS Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting playerInfoScale = setting(new DoubleSetting("player-info-scale", "Player Info Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting worldInfoScale = setting(new DoubleSetting("world-info-scale", "World Info Scale", 1.0, 0.5, 2.0, 0.05));
    private final BooleanSetting watermarkBackground = setting(new BooleanSetting("watermark-background", "Watermark Background", false));
    private final BooleanSetting coordinatesBackground = setting(new BooleanSetting("coordinates-background", "Coordinates Background", false));
    private final BooleanSetting fpsBackground = setting(new BooleanSetting("fps-background", "FPS Background", false));
    private final BooleanSetting playerInfoBackground = setting(new BooleanSetting("player-info-background", "Player Info Background", false));
    private final BooleanSetting worldInfoBackground = setting(new BooleanSetting("world-info-background", "World Info Background", false));

    private final DoubleSetting padding = setting(new DoubleSetting("padding", "Background Padding", 3.0, 0.0, 16.0, 1.0));
    private final DoubleSetting lineSpacing = setting(new DoubleSetting("line-spacing", "Line Spacing", 11.0, 8.0, 24.0, 1.0));
    private final EnumSetting<RowFormat> rowFormat = setting(new EnumSetting<>("row-format", "Row Format", RowFormat.STACKED));
    private final BooleanSetting snap = setting(new BooleanSetting("snap", "Grid Snap", true));
    private final DoubleSetting gridSize = setting(new DoubleSetting("grid-size", "Grid Size", 4.0, 1.0, 32.0, 1.0));
    private final ColorSetting textColor = setting(new ColorSetting("text-color", "Text Color", 0xFFFFFFFF));
    private final ColorSetting secondaryColor = setting(new ColorSetting("secondary-color", "Secondary Color", 0xFFD0D0D0));
    private final ColorSetting backgroundColor = setting(new ColorSetting("background-color", "Background Color", 0x80000000));

    public HudModule() { super("hud", "HUD", Category.RENDER); }

    public boolean showWatermark() { return watermark.enabled(); }
    public String watermarkText() { return watermarkText.get(); }
    public boolean showCoordinates() { return coordinates.enabled(); }
    public boolean showFps() { return fps.enabled(); }
    public boolean showPlayerInfo() { return playerInfo.enabled(); }
    public boolean showWorldInfo() { return worldInfo.enabled(); }
    public boolean showBackground() { return background.enabled(); }
    public boolean showShadow() { return shadow.enabled(); }
    public double scale() { return scale.get(); }
    public int x() { return (int) Math.round(x.get()); }
    public int y() { return (int) Math.round(y.get()); }
    public int coordinatesX() { return (int) Math.round(coordinatesX.get()); }
    public int coordinatesY() { return (int) Math.round(coordinatesY.get()); }
    public int fpsX() { return (int) Math.round(fpsX.get()); }
    public int fpsY() { return (int) Math.round(fpsY.get()); }
    public int playerInfoX() { return (int) Math.round(playerInfoX.get()); }
    public int playerInfoY() { return (int) Math.round(playerInfoY.get()); }
    public int worldInfoX() { return (int) Math.round(worldInfoX.get()); }
    public int worldInfoY() { return (int) Math.round(worldInfoY.get()); }
    public String watermarkAlign() { return watermarkAlign.get(); }
    public String coordinatesAlign() { return coordinatesAlign.get(); }
    public String fpsAlign() { return fpsAlign.get(); }
    public String playerInfoAlign() { return playerInfoAlign.get(); }
    public String worldInfoAlign() { return worldInfoAlign.get(); }
    public int elementColor(String element) { return switch (element) { case "watermark" -> watermarkColor.get(); case "coordinates" -> coordinatesColor.get(); case "fps" -> fpsColor.get(); case "player-info" -> playerInfoColor.get(); case "world-info" -> worldInfoColor.get(); default -> textColor.get(); }; }
    public double elementScale(String element) { return switch (element) { case "watermark" -> watermarkScale.get(); case "coordinates" -> coordinatesScale.get(); case "fps" -> fpsScale.get(); case "player-info" -> playerInfoScale.get(); case "world-info" -> worldInfoScale.get(); default -> 1.0; }; }
    public boolean elementBackground(String element) { return switch (element) { case "watermark" -> watermarkBackground.enabled(); case "coordinates" -> coordinatesBackground.enabled(); case "fps" -> fpsBackground.enabled(); case "player-info" -> playerInfoBackground.enabled(); case "world-info" -> worldInfoBackground.enabled(); default -> false; }; }
    public int elementBackgroundColor() { return elementBackgroundColor.get(); }
    public int padding() { return (int) Math.round(padding.get()); }
    public int lineSpacing() { return (int) Math.round(lineSpacing.get()); }
    public RowFormat rowFormat() { return rowFormat.get(); }
    public boolean gridSnap() { return snap.enabled(); }
    public void setGridSnap(boolean enabled) { snap.set(enabled); }
    public int gridSize() { return (int) Math.round(gridSize.get()); }
    public int textColor() { return textColor.get(); }
    public int secondaryColor() { return secondaryColor.get(); }
    public int backgroundColor() { return backgroundColor.get(); }

    public void cycleElementColor(String element) {
        int next = switch (elementColor(element)) {
            case 0xFFFFFFFF -> 0xFF55FFFF;
            case 0xFF55FFFF -> 0xFF55FF55;
            case 0xFF55FF55 -> 0xFFFFFF55;
            case 0xFFFFFF55 -> 0xFFFF55FF;
            case 0xFFFF55FF -> 0xFFFF5555;
            default -> 0xFFFFFFFF;
        };
        switch (element) {
            case "watermark" -> watermarkColor.set(next);
            case "coordinates" -> coordinatesColor.set(next);
            case "fps" -> fpsColor.set(next);
            case "player-info" -> playerInfoColor.set(next);
            case "world-info" -> worldInfoColor.set(next);
            default -> throw new IllegalArgumentException("Unknown HUD element: " + element);
        }
    }

    public void cycleElementBackgroundColor() {
        int next = switch (elementBackgroundColor()) {
            case 0x80000000 -> 0x90000080;
            case 0x90000080 -> 0x90008080;
            case 0x90008080 -> 0x90008000;
            case 0x90008000 -> 0x90800080;
            case 0x90800080 -> 0x90800000;
            default -> 0x80000000;
        };
        elementBackgroundColor.set(next);
    }

    public void adjustElementScale(String element, double delta) {
        double next = Math.max(0.5, Math.min(2.0, elementScale(element) + delta));
        switch (element) {
            case "watermark" -> watermarkScale.set(next);
            case "coordinates" -> coordinatesScale.set(next);
            case "fps" -> fpsScale.set(next);
            case "player-info" -> playerInfoScale.set(next);
            case "world-info" -> worldInfoScale.set(next);
            default -> throw new IllegalArgumentException("Unknown HUD element: " + element);
        }
    }

    public boolean elementVisible(String element) { return switch (element) { case "watermark" -> watermark.enabled(); case "coordinates" -> coordinates.enabled(); case "fps" -> fps.enabled(); case "player-info" -> playerInfo.enabled(); case "world-info" -> worldInfo.enabled(); default -> false; }; }
    public void setElementVisible(String element, boolean visible) { switch (element) { case "watermark" -> watermark.set(visible); case "coordinates" -> coordinates.set(visible); case "fps" -> fps.set(visible); case "player-info" -> playerInfo.set(visible); case "world-info" -> worldInfo.set(visible); default -> throw new IllegalArgumentException("Unknown HUD element: " + element); } }
    public String elementAlignment(String element) { return switch (element) { case "watermark" -> watermarkAlign(); case "coordinates" -> coordinatesAlign(); case "fps" -> fpsAlign(); case "player-info" -> playerInfoAlign(); case "world-info" -> worldInfoAlign(); default -> "left"; }; }
    public void cycleAlignment(String element) { String current = elementAlignment(element).trim().toLowerCase(java.util.Locale.ROOT); String next = switch (current) { case "left" -> "center"; case "center" -> "right"; default -> "left"; }; switch (element) { case "watermark" -> watermarkAlign.set(next); case "coordinates" -> coordinatesAlign.set(next); case "fps" -> fpsAlign.set(next); case "player-info" -> playerInfoAlign.set(next); case "world-info" -> worldInfoAlign.set(next); default -> throw new IllegalArgumentException("Unknown HUD element: " + element); } }

    public void setEditorPosition(String element, double newX, double newY) {
        double step = gridSnap() ? gridSize() : 1.0;
        double px = Math.round(Math.max(0.0, newX) / step) * step;
        double py = Math.round(Math.max(0.0, newY) / step) * step;
        switch (element) { case "watermark" -> { x.set(px); y.set(py); } case "coordinates" -> { coordinatesX.set(px); coordinatesY.set(py); } case "fps" -> { fpsX.set(px); fpsY.set(py); } case "player-info" -> { playerInfoX.set(px); playerInfoY.set(py); } case "world-info" -> { worldInfoX.set(px); worldInfoY.set(py); } default -> throw new IllegalArgumentException("Unknown HUD element: " + element); }
    }
    public void setEditorPosition(double newX, double newY) { setEditorPosition("watermark", newX, newY); }
    public void resetElement(String element) {
        // Reset means restore the declared defaults exactly; editor grid snapping must not alter defaults.
        switch (element) {
            case "watermark" -> { x.set(x.defaultValue()); y.set(y.defaultValue()); }
            case "coordinates" -> { coordinatesX.set(coordinatesX.defaultValue()); coordinatesY.set(coordinatesY.defaultValue()); }
            case "fps" -> { fpsX.set(fpsX.defaultValue()); fpsY.set(fpsY.defaultValue()); }
            case "player-info" -> { playerInfoX.set(playerInfoX.defaultValue()); playerInfoY.set(playerInfoY.defaultValue()); }
            case "world-info" -> { worldInfoX.set(worldInfoX.defaultValue()); worldInfoY.set(worldInfoY.defaultValue()); }
            default -> throw new IllegalArgumentException("Unknown HUD element: " + element);
        }
    }

    public void applyPreset(String preset) {
        switch (preset.toLowerCase(java.util.Locale.ROOT)) {
            case "minimal" -> { watermark.set(true); coordinates.set(false); fps.set(true); playerInfo.set(false); worldInfo.set(false); background.set(false); shadow.set(true); scale.set(1.0); resetElement("watermark"); resetElement("fps"); }
            case "compact" -> { watermark.set(true); coordinates.set(true); fps.set(true); playerInfo.set(true); worldInfo.set(true); background.set(true); shadow.set(true); scale.set(0.90); backgroundColor.set(0x90000000); resetElement("watermark"); resetElement("coordinates"); resetElement("fps"); resetElement("player-info"); resetElement("world-info"); }
            case "full" -> { watermark.set(true); coordinates.set(true); fps.set(true); playerInfo.set(true); worldInfo.set(true); background.set(true); shadow.set(true); scale.set(1.10); backgroundColor.set(0xA0000000); resetElement("watermark"); resetElement("coordinates"); resetElement("fps"); resetElement("player-info"); resetElement("world-info"); }
            default -> throw new IllegalArgumentException("Unknown HUD preset: " + preset);
        }
    }
}
