package io.arson.client.module;

import net.minecraft.client.MinecraftClient;

/** Exposes the local player's block coordinates for HUD and addon integrations. */
public final class PlayerCoordinatesModule extends Module {
    private int x;
    private int y;
    private int z;

    public PlayerCoordinatesModule() {
        super("player-coordinates", "Player Coordinates", Category.PLAYER,
                "Tracks the local player's current block coordinates without changing movement or world state.");
    }

    @Override protected void onTick(MinecraftClient client) {
        if (client.player == null) return;
        x = client.player.getBlockPos().getX();
        y = client.player.getBlockPos().getY();
        z = client.player.getBlockPos().getZ();
    }

    public int x() { return x; }
    public int y() { return y; }
    public int z() { return z; }
    public String formatted() { return "XYZ " + x + " " + y + " " + z; }
}
