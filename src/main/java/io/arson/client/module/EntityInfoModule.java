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
    private final BooleanSetting distanceFade = setting(new BooleanSetting("distance-fade", "Distance Fade", false));
    private final ColorSetting playerTextColor = setting(new ColorSetting("player-text-color", "Player Text Color", 0xFFFFFFFF));
    private final ColorSetting mobTextColor = setting(new ColorSetting("mob-text-color", "Mob Text Color", 0xFFFF7777));
    private final ColorSetting animalTextColor = setting(new ColorSetting("animal-text-color", "Animal Text Color", 0xFF77FF99));
    private final ColorSetting itemTextColor = setting(new ColorSetting("item-text-color", "Item Text Color", 0xFFFFDD55));
    private final ColorSetting backgroundColor = setting(new ColorSetting("background-color", "Background Color", 0xA0101010));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 128.0, 4.0));
    private final DoubleSetting scale = setting(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05));
    private final DoubleSetting heightOffset = setting(new DoubleSetting("height-offset", "Height Offset", 0.35, 0.0, 1.5, 0.05));
    private final DoubleSetting backgroundPadding = setting(new DoubleSetting("background-padding", "Background Padding", 2.0, 0.0, 8.0, 1.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 2.0, 1.0, 10.0, 1.0));

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
    public boolean distanceFade() { return distanceFade.enabled(); }
    public int textColor() { return playerTextColor.get(); }
    public int textColorFor(io.arson.client.render.EntityType type) {
        return switch (type) {
            case PLAYER -> playerTextColor.get();
            case MOB -> mobTextColor.get();
            case ANIMAL -> animalTextColor.get();
            case ITEM -> itemTextColor.get();
        };
    }
    public int backgroundColor() { return backgroundColor.get(); }
    public double range() { return range.get(); }
    public double scale() { return scale.get(); }
    public double heightOffset() { return heightOffset.get(); }
    public double backgroundPadding() { return backgroundPadding.get(); }
    public int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }

    @Override public int playerColor() { return playerTextColor.get(); }
    @Override public int mobColor() { return mobTextColor.get(); }
    @Override public int animalColor() { return animalTextColor.get(); }
    @Override public int itemColor() { return itemTextColor.get(); }
}
