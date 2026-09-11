package io.arson.client.module;

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
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 128.0, 4.0));
    private final BooleanSetting outline = setting(new BooleanSetting("outline", "Outline", true));
    private final BooleanSetting fill = setting(new BooleanSetting("fill", "Fill", false));

    private final ColorSetting chestColor = setting(new ColorSetting("chest-color", "Chest Color", 0xD8F2B84B));
    private final ColorSetting barrelColor = setting(new ColorSetting("barrel-color", "Barrel Color", 0xD89B6B43));
    private final ColorSetting shulkerColor = setting(new ColorSetting("shulker-color", "Shulker Color", 0xD8A66BFF));
    private final ColorSetting enderChestColor = setting(new ColorSetting("ender-chest-color", "Ender Chest Color", 0xD85B4BDB));
    private final ColorSetting otherStorageColor = setting(new ColorSetting("other-storage-color", "Other Storage Color", 0xD8A0A0A0));

    public ContainerESPModule() {
        super("container-esp", "Container ESP");
    }

    public boolean showChests() { return chests.enabled(); }
    public boolean showBarrels() { return barrels.enabled(); }
    public boolean showShulkers() { return shulkers.enabled(); }
    public boolean showEnderChests() { return enderChests.enabled(); }
    public boolean showOtherStorage() { return otherStorage.enabled(); }
    public double range() { return range.get(); }
    public boolean outline() { return outline.enabled(); }
    public boolean fill() { return fill.enabled(); }
    public int chestColor() { return chestColor.get(); }
    public int barrelColor() { return barrelColor.get(); }
    public int shulkerColor() { return shulkerColor.get(); }
    public int enderChestColor() { return enderChestColor.get(); }
    public int otherStorageColor() { return otherStorageColor.get(); }
}
