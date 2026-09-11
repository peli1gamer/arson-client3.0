package io.arson.client.module;

import io.arson.client.render.EntityScanConfig;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Client-side entity tracer visualization. */
public final class EntityTracerModule extends Module implements EntityScanConfig {
    private final BooleanSetting players = setting(new BooleanSetting("players", "Players", true));
    private final BooleanSetting mobs = setting(new BooleanSetting("mobs", "Mobs", true));
    private final BooleanSetting animals = setting(new BooleanSetting("animals", "Animals", false));
    private final BooleanSetting items = setting(new BooleanSetting("items", "Dropped Items", false));
    private final ColorSetting playerColor = setting(new ColorSetting("player-color", "Player Color", 0xD6AA66FF));
    private final ColorSetting mobColor = setting(new ColorSetting("mob-color", "Mob Color", 0xD6FF5555));
    private final ColorSetting animalColor = setting(new ColorSetting("animal-color", "Animal Color", 0xD655FF78));
    private final ColorSetting itemColor = setting(new ColorSetting("item-color", "Item Color", 0xD6FFDC46));
    private final DoubleSetting lineWidth = setting(new DoubleSetting("line-width", "Line Width", 1.0, 1.0, 4.0, 1.0));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 128.0, 4.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 2.0, 1.0, 10.0, 1.0));

    public EntityTracerModule() {
        super("entity-tracers", "Entity Tracers", Category.RENDER);
    }

    public boolean showPlayers() { return players.enabled(); }
    public boolean showMobs() { return mobs.enabled(); }
    public boolean showAnimals() { return animals.enabled(); }
    public boolean showItems() { return items.enabled(); }
    public int color() { return playerColor.get(); }
    public int playerColor() { return playerColor.get(); }
    public int mobColor() { return mobColor.get(); }
    public int animalColor() { return animalColor.get(); }
    public int itemColor() { return itemColor.get(); }
    public double lineWidth() { return lineWidth.get(); }
    public double range() { return range.get(); }
    public int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }
}
