package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks the local player's active status-effect summary for HUD presentation. */
public final class PlayerEffectsInfoModule extends Module {
    private int effectCount;
    private int beneficialCount;
    private int harmfulCount;

    public PlayerEffectsInfoModule() {
        super("player-effects-info", "Player Effects Info", Category.PLAYER,
                "Tracks live local status-effect counts for HUD presentation without changing effects.");
    }

    @Override protected void onTick(Minecraft client) {
        effectCount = beneficialCount = harmfulCount = 0;
        if (client.player == null) return;
        for (var effect : client.player.getActiveEffects()) {
            effectCount++;
            if (effect.getEffect().value().isBeneficial()) beneficialCount++;
            else harmfulCount++;
        }
    }

    public int effectCount() { return effectCount; }
    public int beneficialCount() { return beneficialCount; }
    public int harmfulCount() { return harmfulCount; }
    public String formatted() { return "Effects " + effectCount + "  Beneficial " + beneficialCount + "  Harmful " + harmfulCount; }
}
