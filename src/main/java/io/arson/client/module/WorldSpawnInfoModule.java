package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live shared-spawn position and local horizontal distance to spawn. */
public final class WorldSpawnInfoModule extends Module {
    private int spawnX;
    private int spawnY;
    private int spawnZ;
    private double distance;

    public WorldSpawnInfoModule() {
        super("world-spawn-info", "World Spawn Info", Category.WORLD,
                "Reports live shared-spawn coordinates and the local player's horizontal distance from spawn.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            spawnX = spawnY = spawnZ = 0;
            distance = 0;
            return;
        }
        var spawn = client.level.getRespawnData().pos();
        spawnX = spawn.getX();
        spawnY = spawn.getY();
        spawnZ = spawn.getZ();
        double dx = client.player.getX() - spawnX;
        double dz = client.player.getZ() - spawnZ;
        distance = Math.sqrt(dx * dx + dz * dz);
    }

    public int spawnX() { return spawnX; }
    public int spawnY() { return spawnY; }
    public int spawnZ() { return spawnZ; }
    public double distance() { return distance; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "Spawn %d %d %d  Distance %.0f", spawnX, spawnY, spawnZ, distance);
    }
}
