package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Live local status-effect telemetry for HUD presentation. */
public final class PlayerEffectInfoModule extends Module {
    private int count;
    private int beneficialCount;
    private int harmfulCount;
    private String summary = "Effects 0";

    public PlayerEffectInfoModule() {
        super("player-effect-info", "Player Effect Info", Category.PLAYER,
                "Reports active status effects, their benefit classification, and the first effect's remaining duration.");
    }

    @Override
    protected void onTick(Minecraft client) {
        count = 0;
        beneficialCount = 0;
        harmfulCount = 0;
        summary = "Effects 0";
        if (client.player == null) return;

        var effects = client.player.getActiveEffects();
        count = effects.size();
        if (count == 0) return;

        String firstName = "Effect";
        int firstSeconds = 0;
        boolean first = true;
        for (var effect : effects) {
            if (effect.getEffect().value().isBeneficial()) beneficialCount++;
            else harmfulCount++;
            if (first) {
                firstName = effect.getEffect().value().getDisplayName().getString();
                firstSeconds = Math.max(0, effect.getDuration() / 20);
                first = false;
            }
        }
        summary = formatSummary(count, beneficialCount, harmfulCount, firstName, firstSeconds);
    }

    public static String formatSummary(int count, int beneficialCount, int harmfulCount,
                                       String firstEffectName, int firstEffectSeconds) {
        if (count <= 0) return "Effects 0";
        String name = firstEffectName == null || firstEffectName.isBlank() ? "Effect" : firstEffectName;
        return "Effects " + count + "  " + Math.max(0, beneficialCount) + " beneficial  "
                + Math.max(0, harmfulCount) + " harmful  " + name + " "
                + Math.max(0, firstEffectSeconds) + "s";
    }

    public int count() { return count; }
    public int beneficialCount() { return beneficialCount; }
    public int harmfulCount() { return harmfulCount; }
    public String summary() { return summary; }
    public String formatted() { return summary; }
}
