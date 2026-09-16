package io.arson.client;

import io.arson.client.context.ClientState;
import io.arson.client.context.FeatureContext;
import io.arson.client.context.RenderState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureContextIntegrationTest {
    @Test
    void propagatesLiveTickStateAndRenderState() {
        FeatureContext context = new FeatureContext();
        context.initialize();
        ClientState live = new ClientState(42, 420, true, false, "minecraft:overworld", 12.5, 64.0, -8.25, 90.0f, 12.0f, 18.0f, 20.0f);

        context.tick(live);
        context.render(7, 0.5f, RenderState.Phase.AFTER_ENTITIES);

        assertEquals(FeatureContext.Lifecycle.WORLD, context.lifecycle());
        assertSame(live, context.state());
        assertEquals(42, context.renderState().tick());
        assertEquals(7, context.renderState().frame());
        assertEquals(0.5f, context.renderState().partialTick());
        assertTrue(context.state().hasPlayer());
    }

    @Test
    void disconnectClearsLiveStateWithoutDestroyingContext() {
        FeatureContext context = new FeatureContext();
        context.tick(new ClientState(10, 10, true, false, "minecraft:overworld", 1, 2, 3, 0, 0, 20, 20));
        context.disconnect();

        assertEquals(FeatureContext.Lifecycle.DISCONNECTED, context.lifecycle());
        assertFalse(context.state().inWorld());
        assertEquals(0, context.state().tick());
    }
}
