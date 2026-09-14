package io.arson.client.module;

import io.arson.client.render.EntityScanConfig;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Configurable entity labels shown above nearby targets. */
public final class EntityInfoModule extends Module implements EntityScanConfig {
    private final BooleanSetting players = setting(new BooleanSetting("players", "Players", true));
    private final BooleanSetting mobs = setting(new BooleanSetting("mobs", "Mobs", true));
    private final BooleanSetting animals = setting(new BooleanSetting("animals", "Animals", false));
    private final BooleanSetting items = setting(new BooleanSetting("items", "Dropped Items", false));
    private final BooleanSetting showName = setting(new BooleanSetting("name", "Name", true));
    private final BooleanSetting showDistance = setting(new BooleanSetting("distance", "Distance", true));
    private final BooleanSetting showHealth = setting(new BooleanSetting("health", "Health", true));
    private final BooleanSetting background = setting(new BooleanSetting("background", "Background", true));
    private final ColorSetting textColor = setting(new ColorSetting("text-color", "Text Color", 0xFFFFFFFF));
    private final ColorSetting backgroundColor = setting(new ColorSetting("background-color", "Background Color", 0xA0101010));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 128.0, 4.0));
    private final DoubleSetting scale = setting(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05));

    public EntityInfoModule() {
        super("entity-info", "Entity Info", Category.RENDER);
    }

    public boolean showPlayers() { return players.enabled(); }
    public boolean showMobs() { return mobs.enabled(); }
    public boolean showAnimals() { return animals.enabled(); }
    public boolean showItems() { return items.enabled(); }
    public boolean showName() { return showName.enabled(); }
    public boolean showDistance() { return showDistance.enabled(); }
    public boolean showHealth() { return showHealth.enabled(); }
    public boolean background() { return background.enabled(); }
    public int textColor() { return textColor.get(); }
    public int backgroundColor() { return backgroundColor.get(); }
    public double range() { return range.get(); }
    public double scale() { return scale.get(); }

    @Override public int playerColor() { return textColor.get(); }
    @Override public int mobColor() { return textColor.get(); }
    @Override public int animalColor() { return textColor.get(); }
    @Override public int itemColor() { return textColor.get(); }
}
