package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;

/** Live local experience telemetry for HUD/API consumers. */
public final class PlayerExperienceInfoModule extends Module {
    private final BooleanSetting showTotal = setting(new BooleanSetting("show-total", "Show Total XP", true));
    private int level;
    private int totalExperience;
    private float progress;

    public PlayerExperienceInfoModule() {
        super("player-experience-info", "Player Experience Info", Category.PLAYER,
            "Reports live local experience level, progress, and total experience.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.player == null) { level = 0; totalExperience = 0; progress = 0.0f; return; }
        level = client.player.experienceLevel;
        totalExperience = client.player.totalExperience;
        progress = client.player.experienceProgress;
    }

    public int level() { return level; }
    public int totalExperience() { return totalExperience; }
    public float progress() { return progress; }
    public boolean showTotal() { return showTotal.enabled(); }
    public String formatted() {
        String out = String.format(java.util.Locale.ROOT, "XP Level %d  Progress %.0f%%", level, progress * 100.0f);
        return showTotal() ? out + "  Total " + totalExperience : out;
    }
}
