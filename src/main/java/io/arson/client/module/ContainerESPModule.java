package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;

/** Configuration and lifecycle for container visualization. Actual drawing belongs to the render stage. */
public final class ContainerESPModule extends VisualModule {
    private final BooleanSetting chests = setting(new BooleanSetting("chests", "Chests", true));
    private final BooleanSetting barrels = setting(new BooleanSetting("barrels", "Barrels", true));
    private final BooleanSetting shulkers = setting(new BooleanSetting("shulkers", "Shulkers", true));
    private final BooleanSetting enderChests = setting(new BooleanSetting("ender-chests", "Ender Chests", true));
    private final BooleanSetting otherStorage = setting(new BooleanSetting("other-storage", "Other Storage", false));

    public ContainerESPModule() {
        super("container-esp", "Container ESP");
    }

    public boolean showChests() { return chests.enabled(); }
    public boolean showBarrels() { return barrels.enabled(); }
    public boolean showShulkers() { return shulkers.enabled(); }
    public boolean showEnderChests() { return enderChests.enabled(); }
    public boolean showOtherStorage() { return otherStorage.enabled(); }
}
