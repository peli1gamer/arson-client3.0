package io.arson.client.render;

import io.arson.client.context.FeatureContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorldRenderBridgeTest {
    @Test
    void renderPartialTickUsesCurrentDeltaTrackerValue() {
        WorldRenderBridge bridge = new WorldRenderBridge(new FeatureContext(), null, () -> 0.375f);
        assertEquals(0.375f, bridge.partialTick());
    }

    @Test
    void renderPartialTickIsClampedToTheValidInterpolationRange() {
        assertEquals(0.0f, new WorldRenderBridge(new FeatureContext(), null, () -> -2.0f).partialTick());
        assertEquals(1.0f, new WorldRenderBridge(new FeatureContext(), null, () -> 3.0f).partialTick());
    }
}
