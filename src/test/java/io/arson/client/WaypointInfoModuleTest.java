package io.arson.client;

import io.arson.client.module.WaypointInfoModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaypointInfoModuleTest {
    @Test
    void distanceUsesThreeDimensionalEuclideanCoordinates() {
        assertEquals(13.0, WaypointInfoModule.distance(0, 0, 0, 3, 4, 12), 1.0e-9);
        assertEquals(0.0, WaypointInfoModule.distance(-12, 64, 88, -12, 64, 88), 1.0e-9);
    }

    @Test
    void unavailableAndOverflowingCoordinatesAreReportedAsUnavailable() {
        assertEquals(-1.0, WaypointInfoModule.distance(Double.NaN, 0, 0, 0, 0, 0));
        assertEquals(-1.0, WaypointInfoModule.distance(-Double.MAX_VALUE, 0, 0, Double.MAX_VALUE, 0, 0));
        assertEquals("Distance —", WaypointInfoModule.formatDistance(-1.0));
        assertEquals("Distance —", WaypointInfoModule.formatDistance(Double.POSITIVE_INFINITY));
    }

    @Test
    void distancePresentationIsStableAcrossLocales() {
        assertEquals("Distance 13.0 blocks", WaypointInfoModule.formatDistance(13.0));
    }
}
