package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.DoubleSetting;

/**
 * Non-automating combat/weapon information module.
 * Keeps combat presentation/settings separate from future combat logic.
 */
public final class CombatInfoModule extends Module {
    private final BooleanSetting showHeldItem = setting(
            new BooleanSetting("show-held-item", "Show Held Item", true));
    private final BooleanSetting showDurability = setting(
            new BooleanSetting("show-durability", "Show Durability", true));
    private final BooleanSetting showAttackCooldown = setting(
            new BooleanSetting("show-attack-cooldown", "Show Attack Cooldown", true));
    private final DoubleSetting infoScale = setting(
            new DoubleSetting("info-scale", "Info Scale", 1.0, 0.5, 2.0, 0.1));

    public CombatInfoModule() {
        super("combat-info", "Combat Info", Category.COMBAT);
    }

    public boolean showHeldItem() {
        return showHeldItem.enabled();
    }

    public boolean showDurability() {
        return showDurability.enabled();
    }

    public boolean showAttackCooldown() {
        return showAttackCooldown.enabled();
    }

    public double infoScale() {
        return infoScale.get();
    }
}
