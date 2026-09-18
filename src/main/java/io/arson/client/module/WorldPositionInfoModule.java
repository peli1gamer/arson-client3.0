package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live world bounds and spawn information for HUD and addon presentation. */
public final class WorldPositionInfoModule extends Module {
    private int minBuildHeight;
    private int maxBuildHeight;
    private double borderDiameter;
    private int spawnX;
    private int spawnY;
    private int spawnZ;
    private String difficulty = "unknown";

    public WorldPositionInfoModule() {
        super("world-position-info", "World Position Info", Category.WORLD,
                "Tracks live world build bounds, world border size, spawn position, and difficulty without changing world state.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.level == null) {
            minBuildHeight = maxBuildHeight = 0;
            borderDiameter = 0;
            spawnX = spawnY = spawnZ = 0;
            difficulty = "unknown";
            return;
        }
        var level = client.level;
        minBuildHeight = level.getMinBuildHeight();
        maxBuildHeight = level.getMaxBuildHeight();
        borderDiameter = level.getWorldBorder().getSize();
        var spawn = level.getSharedSpawnPos();
        spawnX = spawn.getX();
        spawnY = spawn.getY();
        spawnZ = spawn.getZ();
        difficulty = level.getDifficulty().toString();
    }

    public int minBuildHeight() { return minBuildHeight; }
    public int maxBuildHeight() { return maxBuildHeight; }
    public double borderDiameter() { return borderDiameter; }
    public int spawnX() { return spawnX; }
    public int spawnY() { return spawnY; }
    public int spawnZ() { return spawnZ; }
    public String difficulty() { return difficulty; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "Bounds %d..%d  Border %.0f  Spawn %d %d %d  %s",
                minBuildHeight, maxBuildHeight, borderDiameter, spawnX, spawnY, spawnZ, difficulty);
    }
}
