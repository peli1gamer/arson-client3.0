package io.arson.client;

import io.arson.client.render.ArrayListRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayListRendererTest {
    @Test
    void leftAlignedPlacementStaysInsideViewport() {
        assertEquals(16.0, ArrayListRenderer.clampAnchor(1000, false, 80, 4, 100), 0.001);
        assertEquals(4.0, ArrayListRenderer.clampAnchor(-100, false, 80, 4, 100), 0.001);
    }

    @Test
    void rightAlignedPlacementStaysInsideViewport() {
        assertEquals(96.0, ArrayListRenderer.clampAnchor(1000, true, 80, 4, 100), 0.001);
        assertEquals(84.0, ArrayListRenderer.clampAnchor(-100, true, 80, 4, 100), 0.001);
    }

    @Test
    void verticalPlacementStaysInsideViewport() {
        assertEquals(4.0, ArrayListRenderer.clampTop(-50, 80, 4, 100), 0.001);
        assertEquals(16.0, ArrayListRenderer.clampTop(1000, 80, 4, 100), 0.001);
    }
}
