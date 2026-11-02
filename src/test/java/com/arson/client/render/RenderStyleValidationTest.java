package com.arson.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderStyleValidationTest {
    @Test
    void nonFiniteAlphaAndLineWidthUseSafeDefaults() {
        RenderStyle style = new RenderStyle(new RenderColor(10, 20, 30),
                true, true, Float.NaN, Float.POSITIVE_INFINITY, Float.NaN);

        assertEquals(0.0f, style.fillAlpha());
        assertEquals(0.0f, style.outlineAlpha());
        assertEquals(1.0f, style.lineWidth());
    }

    @Test
    void finiteStyleValuesAreClampedToSupportedRanges() {
        RenderStyle style = new RenderStyle(new RenderColor(10, 20, 30),
                true, true, 2.0f, -1.0f, 99.0f);

        assertEquals(1.0f, style.fillAlpha());
        assertEquals(0.0f, style.outlineAlpha());
        assertEquals(8.0f, style.lineWidth());
    }
}
