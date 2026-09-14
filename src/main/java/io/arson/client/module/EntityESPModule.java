package io.arson.client.module;

import io.arson.client.render.EntityScanConfig;
import io.arson.client.render.RenderStyle;
import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Entity visualization configuration consumed by the shared render stage. */
public final class EntityESPModule extends VisualModule implements EntityScanConfig {
    private final BooleanSetting players = setting(new BooleanSetting("players", "Players", true));
    private final BooleanSetting mobs = setting(new BooleanSetting("mobs", "Mobs", true));
    private final BooleanSetting animals = setting(new BooleanSetting("animals", "Animals", false));
    private final BooleanSetting items = setting(new BooleanSetting("items", "Dropped Items", false));
    private final BooleanSetting showHealth = setting(new BooleanSetting("health", "Health", false));
    private final BooleanSetting healthBackground = setting(new BooleanSetting("health-background", "Health Background", true));
    private final ColorSetting playerColor = setting(new ColorSetting("player-color", "Player Color", 0xD655AAFF));
    private final ColorSetting mobColor = setting(new ColorSetting("mob-color", "Mob Color", 0xD6FF5555));
    private final ColorSetting animalColor = setting(new ColorSetting("animal-color", "Animal Color", 0xD655FF78));
    private final ColorSetting itemColor = setting(new ColorSetting("item-color", "Item Color", 0xD6FFDC46));
    private final ColorSetting healthColor = setting(new ColorSetting("health-color", "Health Color", 0xF6FF5555));
    private final ColorSetting healthBackgroundColor = setting(new ColorSetting("health-background-color", "Health Background Color", 0xC6202020));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Range", 64.0, 8.0, 128.0, 4.0));
    private final DoubleSetting scanInterval = setting(new DoubleSetting("scan-interval", "Scan Interval", 2.0, 1.0, 10.0, 1.0));

    public EntityESPModule() {
        super("entity-esp", "Entity ESP", true);
    }

    public boolean showPlayers() { return players.enabled(); }
    public boolean showMobs() { return mobs.enabled(); }
    public boolean showAnimals() { return animals.enabled(); }
    public boolean showItems() { return items.enabled(); }
    public boolean showHealth() { return showHealth.enabled(); }
    public boolean showHealthBackground() { return healthBackground.enabled(); }
    public boolean fill() { return super.filled(); }
    public boolean outline() { return super.outline(); }
    public int playerColor() { return playerColor.get(); }
    public int mobColor() { return mobColor.get(); }
    public int animalColor() { return animalColor.get(); }
    public int itemColor() { return itemColor.get(); }
    public int healthColor() { return healthColor.get(); }
    public int healthBackgroundColor() { return healthBackgroundColor.get(); }
    public double fillAlpha() { return super.fillAlpha(); }
    public double outlineAlpha() { return super.outlineAlpha(); }
    public double lineWidth() { return super.lineWidth(); }
    public double range() { return range.get(); }
    public int scanInterval() { return Math.max(1, (int) Math.round(scanInterval.get())); }

    public RenderStyle playerStyle() { return renderStyle(playerColor.get()); }
    public RenderStyle mobStyle() { return renderStyle(mobColor.get()); }
    public RenderStyle animalStyle() { return renderStyle(animalColor.get()); }
    public RenderStyle itemStyle() { return renderStyle(itemColor.get()); }

    /** Health fill style. */
    public RenderStyle healthStyle() { return renderStyle(healthColor.get()); }

    /** Health background style. */
    public RenderStyle healthBackgroundStyle() { return renderStyle(healthBackgroundColor.get()); }
}
