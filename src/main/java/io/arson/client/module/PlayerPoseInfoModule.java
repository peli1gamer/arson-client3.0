package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live local player pose and view angles for HUD and presentation integrations. */
public final class PlayerPoseInfoModule extends Module {
    private float yaw;
    private float pitch;
    private float bodyYaw;
    private float headYaw;
    private String pose = "standing";
    private boolean swimming;
    private boolean fallFlying;

    public PlayerPoseInfoModule() {
        super("player-pose-info", "Player Pose Info", Category.PLAYER,
                "Tracks live player yaw, pitch, body/head rotation, pose, swimming, and fall-flying state without changing player state.");
    }

    @Override
    protected void onTick(Minecraft client) {
        if (client.player == null) {
            yaw = pitch = bodyYaw = headYaw = 0;
            pose = "standing";
            swimming = fallFlying = false;
            return;
        }
        var player = client.player;
        yaw = player.getYRot();
        pitch = player.getXRot();
        bodyYaw = player.yBodyRot;
        headYaw = player.yHeadRot;
        pose = player.getPose().name().toLowerCase(java.util.Locale.ROOT);
        swimming = player.isSwimming();
        fallFlying = player.isFallFlying();
    }

    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public float bodyYaw() { return bodyYaw; }
    public float headYaw() { return headYaw; }
    public String pose() { return pose; }
    public boolean swimming() { return swimming; }
    public boolean fallFlying() { return fallFlying; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "View Y %.1f  P %.1f  Pose %s%s%s",
                yaw, pitch, pose,
                swimming ? "  Swim" : "",
                fallFlying ? "  Elytra" : "");
    }
}
