package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudCoordinatesTest {
    @Test
    void overworldToNetherUsesFloorDivisionForNegativeCoordinates() {
        assertEquals(new HudCoordinates.Position(-1, 72, -2),
                HudCoordinates.fromOverworld(-1, 72, -9));
    }

    @Test
    void netherToOverworldScalesHorizontalCoordinatesByEight() {
        assertEquals(new HudCoordinates.Position(-80, 64, 72),
                HudCoordinates.fromNether(-10, 64, 9));
    }

    @Test
    void conversionsPreserveVerticalCoordinate() {
        assertEquals(123, HudCoordinates.fromOverworld(800, 123, -800).y());
        assertEquals(123, HudCoordinates.fromNether(100, 123, -100).y());
    }
}
