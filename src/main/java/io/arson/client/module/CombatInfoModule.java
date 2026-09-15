package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.ColorSetting;
import io.arson.client.settings.DoubleSetting;

/** Non-automating combat target and weapon information HUD. */
public final class CombatInfoModule extends Module {
    private final BooleanSetting showTarget = setting(new BooleanSetting("show-target", "Show Target", true));
    private final BooleanSetting showHeldItem = setting(new BooleanSetting("show-held-item", "Show Held Item", true));
    private final BooleanSetting showDurability = setting(new BooleanSetting("show-durability", "Show Durability", true));
    private final BooleanSetting showAttackCooldown = setting(new BooleanSetting("show-attack-cooldown", "Show Attack Cooldown", true));
    private final BooleanSetting showDistance = setting(new BooleanSetting("show-distance", "Show Distance", true));
    private final BooleanSetting showHealth = setting(new BooleanSetting("show-health", "Show Health", true));
    private final BooleanSetting background = setting(new BooleanSetting("background", "Background", true));
    private final ColorSetting textColor = setting(new ColorSetting("text-color", "Text Color", 0xFFFFFFFF));
    private final ColorSetting healthColor = setting(new ColorSetting("health-color", "Health Color", 0xFFFF5555));
    private final ColorSetting backgroundColor = setting(new ColorSetting("background-color", "Background Color", 0xA0101010));
    private final DoubleSetting range = setting(new DoubleSetting("range", "Target Range", 16.0, 4.0, 64.0, 1.0));
    private final DoubleSetting x = setting(new DoubleSetting("x", "X", 10.0, 0.0, 5000.0, 1.0));
    private final DoubleSetting y = setting(new DoubleSetting("y", "Y", 10.0, 0.0, 5000.0, 1.0));
    private final DoubleSetting infoScale = setting(new DoubleSetting("info-scale", "Info Scale", 1.0, 0.5, 2.0, 0.1));

    public CombatInfoModule() {
        super("combat-info", "Combat Info", Category.COMBAT);
    }

    public boolean showTarget() { return showTarget.enabled(); }
    public boolean showHeldItem() { return showHeldItem.enabled(); }
    public boolean showDurability() { return showDurability.enabled(); }
    public boolean showAttackCooldown() { return showAttackCooldown.enabled(); }
    public boolean showDistance() { return showDistance.enabled(); }
    public boolean showHealth() { return showHealth.enabled(); }
    public boolean background() { return background.enabled(); }
    public int textColor() { return textColor.get(); }
    public int healthColor() { return healthColor.get(); }
    public int backgroundColor() { return backgroundColor.get(); }
    public double range() { return range.get(); }
    public int x() { return (int) Math.round(x.get()); }
    public int y() { return (int) Math.round(y.get()); }
    public double infoScale() { return infoScale.get(); }
}
