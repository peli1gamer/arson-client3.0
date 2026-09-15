package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Configurable enabled-module list HUD. */
public final class ArrayListModule extends Module {
    private final BooleanSetting showCategory = setting(new BooleanSetting("show-category", "Show Category", false));
    private final BooleanSetting background = setting(new BooleanSetting("background", "Background", true));
    private final BooleanSetting shadow = setting(new BooleanSetting("shadow", "Text Shadow", true));
    private final BooleanSetting rightAlign = setting(new BooleanSetting("right-align", "Right Align", true));
    private final DoubleSetting x = setting(new DoubleSetting("x", "X", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting y = setting(new DoubleSetting("y", "Y", 6.0, 0.0, 1000.0, 1.0));
    private final DoubleSetting scale = setting(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting spacing = setting(new DoubleSetting("spacing", "Spacing", 11.0, 8.0, 24.0, 1.0));
    private final DoubleSetting maxModules = setting(new DoubleSetting("max-modules", "Max Modules", 32.0, 1.0, 64.0, 1.0));
    private final DoubleSetting padding = setting(new DoubleSetting("padding", "Background Padding", 2.0, 0.0, 8.0, 1.0));
    private final ColorSetting textColor = setting(new ColorSetting("text-color", "Text Color", 0xFFFFFFFF));
    private final ColorSetting backgroundColor = setting(new ColorSetting("background-color", "Background Color", 0x90000000));

    public ArrayListModule() {
        super("array-list", "Array List", Category.RENDER);
    }

    public boolean showCategory() { return showCategory.enabled(); }
    public boolean background() { return background.enabled(); }
    public boolean shadow() { return shadow.enabled(); }
    public boolean rightAlign() { return rightAlign.enabled(); }
    public int x() { return (int) Math.round(x.get()); }
    public int y() { return (int) Math.round(y.get()); }
    public float scale() { return scale.get().floatValue(); }
    public int spacing() { return (int) Math.round(spacing.get()); }
    public int maxModules() { return Math.max(1, (int) Math.round(maxModules.get())); }
    public int padding() { return (int) Math.round(padding.get()); }
    public int textColor() { return textColor.get(); }
    public int backgroundColor() { return backgroundColor.get(); }
}
