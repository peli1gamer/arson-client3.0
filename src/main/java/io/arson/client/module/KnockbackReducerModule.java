package io.arson.client.module;

import io.arson.client.settings.DoubleSetting;
import net.minecraft.client.Minecraft;

/** Scales client-applied incoming velocity for the local player. */
public final class KnockbackReducerModule extends Module {
    private final DoubleSetting horizontalPercent = setting(new DoubleSetting("horizontal-percent", "Horizontal Knockback %", 50.0, 0.0, 100.0, 5.0));
    private final DoubleSetting verticalPercent = setting(new DoubleSetting("vertical-percent", "Vertical Knockback %", 80.0, 0.0, 100.0, 5.0));

    public KnockbackReducerModule() {
        super("knockback-reducer", "Knockback Reducer", Category.COMBAT,
                "Reduces incoming client-applied knockback. Lower percentages mean less velocity is applied.");
    }

    public double horizontalScale() { return horizontalPercent.get() / 100.0; }
    public double verticalScale() { return verticalPercent.get() / 100.0; }

    public boolean appliesToLocalPlayer(Minecraft client, int entityId) {
        return enabled() && client.player != null && client.player.getId() == entityId;
    }
}
