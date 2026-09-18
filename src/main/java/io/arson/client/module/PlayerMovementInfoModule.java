package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live local movement state for HUD and addon presentation. */
public final class PlayerMovementInfoModule extends Module {
    private double horizontalSpeed;
    private double verticalSpeed;
    private double fallDistance;
    private boolean onGround;
    private boolean sprinting;
    private boolean sneaking;

    public PlayerMovementInfoModule() {
        super("player-movement-info", "Player Movement Info", Category.PLAYER,
                "Tracks live local velocity, fall distance, ground state, sprinting, and sneaking for HUD integrations; it never changes movement.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.player == null) {
            horizontalSpeed = verticalSpeed = fallDistance = 0;
            onGround = sprinting = sneaking = false;
            return;
        }
        var player = client.player;
        var velocity = player.getDeltaMovement();
        horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        verticalSpeed = velocity.y;
        fallDistance = player.fallDistance;
        onGround = player.onGround();
        sprinting = player.isSprinting();
        sneaking = player.isCrouching();
    }

    public double horizontalSpeed() { return horizontalSpeed; }
    public double verticalSpeed() { return verticalSpeed; }
    public double fallDistance() { return fallDistance; }
    public boolean onGround() { return onGround; }
    public boolean sprinting() { return sprinting; }
    public boolean sneaking() { return sneaking; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "Move H %.2f  V %.2f  Fall %.1f  %s%s%s",
                horizontalSpeed, verticalSpeed, fallDistance,
                onGround ? "Ground" : "Air",
                sprinting ? "  Sprint" : "",
                sneaking ? "  Sneak" : "");
    }
}
