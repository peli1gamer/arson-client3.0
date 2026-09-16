package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live camera/render viewpoint state without changing the camera. */
public final class CameraInfoModule extends Module {
    private double x, y, z;
    private float yaw, pitch;
    private String perspective = "FIRST_PERSON";

    public CameraInfoModule() {
        super("camera-info", "Camera Info", Category.RENDER,
                "Tracks live camera position, rotation, and perspective for HUD and visual integrations; it never changes camera state.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.player == null) {
            x = y = z = 0;
            yaw = pitch = 0;
            perspective = "FIRST_PERSON";
            return;
        }
        var player = client.player;
        x = player.getX();
        y = player.getY();
        z = player.getZ();
        yaw = player.getYRot();
        pitch = player.getXRot();
        perspective = client.options.getCameraType().toString();
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public String perspective() { return perspective; }
    public String formatted() { return String.format(java.util.Locale.ROOT, "Camera %.1f %.1f %.1f  Yaw %.1f  Pitch %.1f  %s", x, y, z, yaw, pitch, perspective); }
}
