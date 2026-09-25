package io.arson.client.module;

import io.arson.client.settings.DoubleSetting;
import io.arson.client.settings.StringSetting;
import net.minecraft.client.Minecraft;

import java.util.Locale;

/** Read-only distance display for a manually configured waypoint. */
public final class WaypointInfoModule extends Module {
    private final StringSetting label = setting(new StringSetting("label", "Waypoint Label", "Waypoint", 32));
    private final DoubleSetting x = setting(new DoubleSetting("x", "X", 0.0, -30_000_000.0, 30_000_000.0, 1.0));
    private final DoubleSetting y = setting(new DoubleSetting("y", "Y", 64.0, -4096.0, 4096.0, 1.0));
    private final DoubleSetting z = setting(new DoubleSetting("z", "Z", 0.0, -30_000_000.0, 30_000_000.0, 1.0));
    private double distance = -1.0;

    public WaypointInfoModule() {
        super("waypoint-info", "Waypoint Info", Category.DONUTSMP,
                "Shows distance to manually configured coordinates; it never moves the player or interacts with the server.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.player == null) {
            distance = -1.0;
            return;
        }
        distance = distance(client.player.getX(), client.player.getY(), client.player.getZ(), x.get(), y.get(), z.get());
    }

    public String label() { return label.get(); }
    public double x() { return x.get(); }
    public double y() { return y.get(); }
    public double z() { return z.get(); }
    public double distance() { return distance; }
    public String formatted() {
        String name = label().isBlank() ? "Waypoint" : label().trim();
        return name + " • " + formatDistance(distance);
    }

    public static double distance(double x, double y, double z, double targetX, double targetY, double targetZ) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !Double.isFinite(targetX) || !Double.isFinite(targetY) || !Double.isFinite(targetZ)) return -1.0;
        return Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2) + Math.pow(targetZ - z, 2));
    }

    public static String formatDistance(double distance) {
        return !Double.isFinite(distance) || distance < 0.0
                ? "Distance —"
                : String.format(Locale.ROOT, "Distance %.1f blocks", distance);
    }
}
