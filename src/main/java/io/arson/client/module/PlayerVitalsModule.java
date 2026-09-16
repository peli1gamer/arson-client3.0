package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live local player vitals for HUD and addon presentation. */
public final class PlayerVitalsModule extends Module {
    private float health;
    private float maxHealth;
    private int hunger;
    private float saturation;
    private int armor;
    private int air;
    private int maxAir;
    private int level;
    private float experienceProgress;
    private int totalExperience;
    private boolean sprinting;
    private boolean sneaking;

    public PlayerVitalsModule() {
        super("player-vitals", "Player Vitals", Category.PLAYER,
                "Tracks live local health, food, saturation, armor, air, experience, sprint, and sneak state for HUD presentation.");
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.player == null) {
            health = 0;
            maxHealth = 0;
            hunger = 0;
            saturation = 0;
            armor = 0;
            air = 0;
            maxAir = 0;
            level = 0;
            experienceProgress = 0;
            totalExperience = 0;
            sprinting = false;
            sneaking = false;
            return;
        }
        var player = client.player;
        health = player.getHealth();
        maxHealth = player.getMaxHealth();
        hunger = player.getFoodData().getFoodLevel();
        saturation = player.getFoodData().getSaturationLevel();
        armor = player.getArmorValue();
        air = player.getAirSupply();
        maxAir = player.getMaxAirSupply();
        level = player.experienceLevel;
        experienceProgress = player.experienceProgress;
        totalExperience = player.totalExperience;
        sprinting = player.isSprinting();
        sneaking = player.isCrouching();
    }

    public float health() { return health; }
    public float maxHealth() { return maxHealth; }
    public int hunger() { return hunger; }
    public float saturation() { return saturation; }
    public int armor() { return armor; }
    public int air() { return air; }
    public int maxAir() { return maxAir; }
    public int level() { return level; }
    public float experienceProgress() { return experienceProgress; }
    public int totalExperience() { return totalExperience; }
    public boolean sprinting() { return sprinting; }
    public boolean sneaking() { return sneaking; }

    public String formattedExperience() {
        return String.format(java.util.Locale.ROOT, "XP %d  Level %d (%d%%)", totalExperience, level, Math.round(experienceProgress * 100.0f));
    }

    public String formattedAir() {
        return "Air " + air + "/" + maxAir;
    }
}
