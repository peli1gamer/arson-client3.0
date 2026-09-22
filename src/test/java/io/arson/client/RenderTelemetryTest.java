package io.arson.client.module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RenderTelemetryTest {
    @Test
    void guiMouseCoordinatesClampToScaledViewport() {
        assertEquals(0, RenderGuiInfoModule.clampMouseCoordinate(-4, 320));
        assertEquals(319, RenderGuiInfoModule.clampMouseCoordinate(999, 320));
        assertEquals(0, RenderGuiInfoModule.clampMouseCoordinate(7, 0));
        assertEquals(127, RenderGuiInfoModule.clampMouseCoordinate(127, 128));
    }

    @Test
    void frameTimeIsZeroWhenFpsIsUnavailable() {
        assertEquals(0L, RenderFrameInfoModule.frameTimeNanosForFps(0));
        assertEquals(0L, RenderFrameInfoModule.frameTimeNanosForFps(-10));
        assertEquals(50_000_000L, RenderFrameInfoModule.frameTimeNanosForFps(20));
    }
}
