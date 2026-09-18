package io.arson.client.module;

import net.minecraft.client.Minecraft;

/** Tracks live world-border geometry and local distance information. */
public final class WorldBorderInfoModule extends Module {
    private double centerX;
    private double centerZ;
    private double diameter;
    private double distanceToEdge;

    public WorldBorderInfoModule() {
        super("world-border-info", "World Border Info", Category.WORLD,
                "Reports live world-border center, diameter, and the local player's nearest horizontal border distance.");
    }

    @Override protected void onTick(Minecraft client) {
        if (client.level == null || client.player == null) {
            centerX = centerZ = diameter = distanceToEdge = 0;
            return;
        }
        var border = client.level.getWorldBorder();
        centerX = border.getCenterX();
        centerZ = border.getCenterZ();
        diameter = border.getSize();
        double half = diameter / 2.0;
        double dx = half - Math.abs(client.player.getX() - centerX);
        double dz = half - Math.abs(client.player.getZ() - centerZ);
        distanceToEdge = Math.max(0.0, Math.min(dx, dz));
    }

    public double centerX() { return centerX; }
    public double centerZ() { return centerZ; }
    public double diameter() { return diameter; }
    public double distanceToEdge() { return distanceToEdge; }

    public String formatted() {
        return String.format(java.util.Locale.ROOT, "Border %.0f  Edge %.0f  Center %.0f %.0f",
                diameter, distanceToEdge, centerX, centerZ);
    }
}
