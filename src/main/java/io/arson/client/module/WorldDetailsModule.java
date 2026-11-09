package io.arson.client.module;

import net.minecraft.client.MinecraftClient;

/** Tracks live world dimensions useful to information HUDs and addons. */
public final class WorldDetailsModule extends Module {
    private long dayTime;
    private long gameTime;
    private int seaLevel;
    private int viewDistance;
    private String levelName = "unknown";

    public WorldDetailsModule() {
        super("world-details", "World Details", Category.WORLD,
                "Tracks live world time, game time, sea level, render distance, and local level identity for HUDs; it never changes world state.");
    }

    @Override protected void onTick(MinecraftClient client) {
        if (client.world == null) {
            dayTime = gameTime = 0;
            seaLevel = 0;
            viewDistance = 0;
            levelName = "unknown";
            return;
        }
        dayTime = client.world.getTimeOfDay();
        gameTime = client.world.getTime();
        seaLevel = client.world.getSeaLevel();
        viewDistance = client.options.getViewDistance().getValue();
        levelName = client.world.getRegistryKey().getValue().toString();
    }

    public long dayTime() { return dayTime; }
    public long gameTime() { return gameTime; }
    public int seaLevel() { return seaLevel; }
    public int viewDistance() { return viewDistance; }
    public String levelName() { return levelName; }
    public String formatted() { return "World " + levelName + "  Day " + dayTime + "  Game " + gameTime + "  Sea " + seaLevel + "  View " + viewDistance; }
}
