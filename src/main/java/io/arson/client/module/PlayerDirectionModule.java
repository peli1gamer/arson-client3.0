package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks the local player's horizontal facing direction for HUD presentation. */
public final class PlayerDirectionModule extends Module {
    private String direction = "North";
    private float yaw;

    public PlayerDirectionModule() {
        super("player-direction", "Player Direction", Category.PLAYER,
                "Tracks the local player's horizontal facing direction for HUD and addon integrations without changing rotation.");
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.player == null) return;
        yaw = client.player.getYRot();
        direction = cardinal(yaw);
    }

    public float yaw() { return yaw; }
    public String direction() { return direction; }
    public String formatted() { return direction + " (" + Math.round(yaw) + "°)"; }

    static String cardinal(float yaw) {
        float normalized = ((yaw % 360f) + 360f) % 360f;
        if (normalized >= 337.5f || normalized < 22.5f) return "South";
        if (normalized < 67.5f) return "Southwest";
        if (normalized < 112.5f) return "West";
        if (normalized < 157.5f) return "Northwest";
        if (normalized < 202.5f) return "North";
        if (normalized < 247.5f) return "Northeast";
        if (normalized < 292.5f) return "East";
        return "Southeast";
    }
}
