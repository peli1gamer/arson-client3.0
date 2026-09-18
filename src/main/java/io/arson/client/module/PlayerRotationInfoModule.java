package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks the local player's live view rotation for HUD and addon integrations. */
public final class PlayerRotationInfoModule extends Module {
    private float yaw;
    private float pitch;
    private String direction = "North";

    public PlayerRotationInfoModule() {
        super("player-rotation-info", "Player Rotation Info", Category.PLAYER,
                "Tracks live local yaw, pitch, and cardinal facing direction for HUD integrations; it never changes rotation.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.player == null) {
            yaw = pitch = 0;
            direction = "North";
            return;
        }
        yaw = client.player.getYRot();
        pitch = client.player.getXRot();
        direction = directionForYaw(yaw);
    }

    static String directionForYaw(float yaw) {
        float normalized = ((yaw % 360.0f) + 360.0f) % 360.0f;
        if (normalized >= 315.0f || normalized < 45.0f) return "South";
        if (normalized < 135.0f) return "West";
        if (normalized < 225.0f) return "North";
        return "East";
    }

    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public String direction() { return direction; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "Yaw %.1f  Pitch %.1f  %s", yaw, pitch, direction);
    }
}
