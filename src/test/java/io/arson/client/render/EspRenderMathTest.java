package io.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EspRenderMathTest {
    @Test
    void keepsFullOpacityBeforeFadeStart() {
        assertEquals(1.0f, EspRenderMath.distanceFade(0.0, 100.0, 0.5));
        assertEquals(1.0f, EspRenderMath.distanceFade(50.0, 100.0, 0.5));
    }

    @Test
    void fadesLinearlyFromConfiguredStartToRange() {
        assertEquals(0.5f, EspRenderMath.distanceFade(75.0, 100.0, 0.5), 0.0001f);
        assertEquals(0.0f, EspRenderMath.distanceFade(100.0, 100.0, 0.5));
        assertEquals(0.0f, EspRenderMath.distanceFade(120.0, 100.0, 0.5));
    }

    @Test
    void clampsFadeStartAndRejectsInvalidDistanceInputs() {
        assertEquals(0.5f, EspRenderMath.distanceFade(50.0, 100.0, -1.0), 0.0001f);
        assertEquals(1.0f, EspRenderMath.distanceFade(50.0, 100.0, 1.0));
        assertEquals(0.0f, EspRenderMath.distanceFade(Double.NaN, 100.0, 0.5));
        assertEquals(0.0f, EspRenderMath.distanceFade(1.0, 0.0, 0.5));
        assertEquals(0.0f, EspRenderMath.distanceFade(Double.POSITIVE_INFINITY, 100.0, 0.5));
    }

    @Test
    void fadesOnlyTheAlphaChannel() {
        assertEquals(0x80402010, EspRenderMath.fadeAlpha(0xFF402010, 0.5f));
        assertEquals(0x00402010, EspRenderMath.fadeAlpha(0xFF402010, 0.0f));
        assertEquals(0xFF402010, EspRenderMath.fadeAlpha(0xFF402010, 2.0f));
    }
}