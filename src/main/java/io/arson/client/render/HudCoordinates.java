package io.arson.client.render;

/** Dimension coordinate conversion used by the informational HUD. */
public final class HudCoordinates {
    private HudCoordinates() {}

    public record Position(long x, long y, long z) {}

    /** Converts Overworld block coordinates to the corresponding Nether coordinates. */
    public static Position fromOverworld(long x, long y, long z) {
        return new Position(Math.floorDiv(x, 8L), y, Math.floorDiv(z, 8L));
    }

    /** Converts Nether block coordinates to the corresponding Overworld coordinates. */
    public static Position fromNether(long x, long y, long z) {
        return new Position(x * 8L, y, z * 8L);
    }
}
