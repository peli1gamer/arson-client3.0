package io.arson.client.module;

import com.arson.client.render.RenderColor;
import io.arson.client.render.RenderStyle;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Configurable container visualization. Drawing is performed by the render stage. */
public final class ContainerESPModule extends VisualModule {
    private final BooleanSetting chests = setting(new BooleanSetting("chests", "Chests", true));
    private final BooleanSetting barrels = setting(new BooleanSetting("barrels", "Barrels", true));
    private final BooleanSetting shulkers = setting(new BooleanSetting("shulkers", "Shulkers", true));
    private final BooleanSetting enderChests = setting(new BooleanSetting("ender-chests", "Ender Chests", true));
    private final BooleanSetting otherStorage = setting(new BooleanSetting("other-storage", "Other Storage", false));
    private final BooleanSetting distanceFade = setting(new BooleanSetting("distance-fade", "Distance Fade", false));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 128.0, 4.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 5.0, 1.0, 20.0, 1.0));
    private final BooleanSetting showLabels = setting(new BooleanSetting("show-labels", "Show Labels", false));
    private final BooleanSetting labelDistance = setting(new BooleanSetting("label-distance", "Label Distance", true));
    private final BooleanSetting labelBackground = setting(new BooleanSetting("label-background", "Label Background", true));
    private final DoubleSetting labelScale = setting(new DoubleSetting("label-scale", "Label Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting labelHeight = setting(new DoubleSetting("label-height", "Label Height", 0.35, 0.0, 2.0, 0.05));
    private final DoubleSetting labelPadding = setting(new DoubleSetting("label-padding", "Label Padding", 2.0, 0.0, 6.0, 0.5));
    private final ColorSetting labelColor = setting(new ColorSetting("label-color", "Label Color", 0xFFFFFFFF));
    private final ColorSetting labelBackgroundColor = setting(new ColorSetting("label-background-color", "Label Background Color", 0xA0000000));
    private final BooleanSetting chestFill = setting(new BooleanSetting("chest-fill", "Chest Fill", true));
    private final BooleanSetting chestOutline = setting(new BooleanSetting("chest-outline", "Chest Outline", true));
    private final BooleanSetting barrelFill = setting(new BooleanSetting("barrel-fill", "Barrel Fill", true));
    private final BooleanSetting barrelOutline = setting(new BooleanSetting("barrel-outline", "Barrel Outline", true));
    private final BooleanSetting shulkerFill = setting(new BooleanSetting("shulker-fill", "Shulker Fill", true));
    private final BooleanSetting shulkerOutline = setting(new BooleanSetting("shulker-outline", "Shulker Outline", true));
    private final BooleanSetting enderChestFill = setting(new BooleanSetting("ender-chest-fill", "Ender Chest Fill", true));
    private final BooleanSetting enderChestOutline = setting(new BooleanSetting("ender-chest-outline", "Ender Chest Outline", true));
    private final BooleanSetting otherStorageFill = setting(new BooleanSetting("other-storage-fill", "Other Storage Fill", true));
    private final BooleanSetting otherStorageOutline = setting(new BooleanSetting("other-storage-outline", "Other Storage Outline", true));
    private final DoubleSetting chestFillAlpha = setting(new DoubleSetting("chest-fill-alpha", "Chest Fill Alpha", 0.30, 0.0, 1.0, 0.05));
    private final DoubleSetting chestOutlineAlpha = setting(new DoubleSetting("chest-outline-alpha", "Chest Outline Alpha", 1.0, 0.0, 1.0, 0.05));
    private final DoubleSetting chestLineWidth = setting(new DoubleSetting("chest-line-width", "Chest Line Width", 1.0, 0.5, 8.0, 0.5));
    private final DoubleSetting barrelFillAlpha = setting(new DoubleSetting("barrel-fill-alpha", "Barrel Fill Alpha", 0.30, 0.0, 1.0, 0.05));
    private final DoubleSetting barrelOutlineAlpha = setting(new DoubleSetting("barrel-outline-alpha", "Barrel Outline Alpha", 1.0, 0.0, 1.0, 0.05));
    private final DoubleSetting barrelLineWidth = setting(new DoubleSetting("barrel-line-width", "Barrel Line Width", 1.0, 0.5, 8.0, 0.5));
    private final DoubleSetting shulkerFillAlpha = setting(new DoubleSetting("shulker-fill-alpha", "Shulker Fill Alpha", 0.30, 0.0, 1.0, 0.05));
    private final DoubleSetting shulkerOutlineAlpha = setting(new DoubleSetting("shulker-outline-alpha", "Shulker Outline Alpha", 1.0, 0.0, 1.0, 0.05));
    private final DoubleSetting shulkerLineWidth = setting(new DoubleSetting("shulker-line-width", "Shulker Line Width", 1.0, 0.5, 8.0, 0.5));
    private final DoubleSetting enderChestFillAlpha = setting(new DoubleSetting("ender-chest-fill-alpha", "Ender Chest Fill Alpha", 0.30, 0.0, 1.0, 0.05));
    private final DoubleSetting enderChestOutlineAlpha = setting(new DoubleSetting("ender-chest-outline-alpha", "Ender Chest Outline Alpha", 1.0, 0.0, 1.0, 0.05));
    private final DoubleSetting enderChestLineWidth = setting(new DoubleSetting("ender-chest-line-width", "Ender Chest Line Width", 1.0, 0.5, 8.0, 0.5));
    private final DoubleSetting otherStorageFillAlpha = setting(new DoubleSetting("other-storage-fill-alpha", "Other Storage Fill Alpha", 0.30, 0.0, 1.0, 0.05));
    private final DoubleSetting otherStorageOutlineAlpha = setting(new DoubleSetting("other-storage-outline-alpha", "Other Storage Outline Alpha", 1.0, 0.0, 1.0, 0.05));
    private final DoubleSetting otherStorageLineWidth = setting(new DoubleSetting("other-storage-line-width", "Other Storage Line Width", 1.0, 0.5, 8.0, 0.5));
    private final ColorSetting chestColor = setting(new ColorSetting("chest-color", "Chest Color", 0xD8F2B84B));
    private final ColorSetting barrelColor = setting(new ColorSetting("barrel-color", "Barrel Color", 0xD89B6B43));
    private final ColorSetting shulkerColor = setting(new ColorSetting("shulker-color", "Shulker Color", 0xD8A66BFF));
    private final ColorSetting enderChestColor = setting(new ColorSetting("ender-chest-color", "Ender Chest Color", 0xD85B4BDB));
    private final ColorSetting otherStorageColor = setting(new ColorSetting("other-storage-color", "Other Storage Color", 0xD8A0A0A0));

    public ContainerESPModule() { super("container-esp", "Container ESP", false); }
    public boolean showChests() { return chests.enabled(); }
    public boolean showBarrels() { return barrels.enabled(); }
    public boolean showShulkers() { return shulkers.enabled(); }
    public boolean showEnderChests() { return enderChests.enabled(); }
    public boolean showOtherStorage() { return otherStorage.enabled(); }
    public boolean distanceFade() { return distanceFade.enabled(); }
    public double range() { return range.get(); }
    public int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }
    public boolean showLabels() { return showLabels.enabled(); }
    public boolean labelDistance() { return labelDistance.enabled(); }
    public boolean labelBackground() { return labelBackground.enabled(); }
    public float labelScale() { return labelScale.get().floatValue(); }
    public float labelHeight() { return labelHeight.get().floatValue(); }
    public float labelPadding() { return labelPadding.get().floatValue(); }
    public int labelColor() { return labelColor.get(); }
    public int labelBackgroundColor() { return labelBackgroundColor.get(); }
    public boolean fill() { return super.filled(); }
    public boolean outline() { return super.outline(); }
    public double fillAlpha() { return super.fillAlpha(); }
    public double outlineAlpha() { return super.outlineAlpha(); }
    public double lineWidth() { return super.lineWidth(); }

    public RenderStyle chestStyle() { return style(chestColor.get(), chestFill.enabled(), chestOutline.enabled(), chestFillAlpha.get(), chestOutlineAlpha.get(), chestLineWidth.get()); }
    public RenderStyle barrelStyle() { return style(barrelColor.get(), barrelFill.enabled(), barrelOutline.enabled(), barrelFillAlpha.get(), barrelOutlineAlpha.get(), barrelLineWidth.get()); }
    public RenderStyle shulkerStyle() { return style(shulkerColor.get(), shulkerFill.enabled(), shulkerOutline.enabled(), shulkerFillAlpha.get(), shulkerOutlineAlpha.get(), shulkerLineWidth.get()); }
    public RenderStyle enderChestStyle() { return style(enderChestColor.get(), enderChestFill.enabled(), enderChestOutline.enabled(), enderChestFillAlpha.get(), enderChestOutlineAlpha.get(), enderChestLineWidth.get()); }
    public RenderStyle otherStorageStyle() { return style(otherStorageColor.get(), otherStorageFill.enabled(), otherStorageOutline.enabled(), otherStorageFillAlpha.get(), otherStorageOutlineAlpha.get(), otherStorageLineWidth.get()); }
    private static RenderStyle style(int argb, boolean fill, boolean outline, double fillAlpha, double outlineAlpha, double lineWidth) {
        RenderColor color = new RenderColor((argb >>> 16) & 0xFF, (argb >>> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
        return new RenderStyle(color, fill, outline, (float) fillAlpha, (float) outlineAlpha, (float) lineWidth);
    }
}
