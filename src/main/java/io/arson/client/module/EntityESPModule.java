package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;

/** Entity visualization configuration. The render implementation can consume these values without tick polling. */
public final class EntityESPModule extends VisualModule {
    private final BooleanSetting players = setting(new BooleanSetting("players", "Players", true));
    private final BooleanSetting mobs = setting(new BooleanSetting("mobs", "Mobs", true));
    private final BooleanSetting animals = setting(new BooleanSetting("animals", "Animals", false));
    private final BooleanSetting items = setting(new BooleanSetting("items", "Dropped Items", false));
    private final BooleanSetting showHealth = setting(new BooleanSetting("health", "Health", false));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 128.0, 4.0));

    public EntityESPModule() {
        super("entity-esp", "Entity ESP");
    }

    public boolean showPlayers() { return players.enabled(); }
    public boolean showMobs() { return mobs.enabled(); }
    public boolean showAnimals() { return animals.enabled(); }
    public boolean showItems() { return items.enabled(); }
    public boolean showHealth() { return showHealth.enabled(); }
    public double range() { return range.get(); }
}
