package io.arson.client;

import io.arson.client.context.ClientState;
import io.arson.client.context.FeatureContext;
import io.arson.client.context.RenderState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleTickRenderIntegrationTest {
    @Test
    void tickAdvancesLiveStateAndRenderConsumesTheSameTick() {
        FeatureContext context = new FeatureContext();
        context.initialize();
        for (long tick = 1; tick <= 3; tick++) {
            context.tick(new ClientState(tick, tick * 20, true, false, "minecraft:overworld", tick, 64, tick, 0, 0, 20, 20));
        }
        context.render(11, 0.25f, RenderState.Phase.AFTER_ENTITIES);

        assertEquals(3, context.state().tick());
        assertEquals(60, context.state().gameTime());
        assertEquals(3, context.renderState().tick());
        assertEquals(RenderState.Phase.AFTER_ENTITIES, context.renderState().phase());
    }

    @Test
    void lifecycleMovesThroughInitializationWorldAndDisconnect() {
        FeatureContext context = new FeatureContext();
        assertEquals(FeatureContext.Lifecycle.NEW, context.lifecycle());
        context.initialize();
        assertEquals(FeatureContext.Lifecycle.INITIALIZED, context.lifecycle());
        context.tick(new ClientState(1, 1, true, false, "minecraft:overworld", 0, 64, 0, 0, 0, 20, 20));
        assertEquals(FeatureContext.Lifecycle.WORLD, context.lifecycle());
        context.tick(new ClientState(2, 2, false, false, "", 0, 0, 0, 0, 0, 0, 0));
        assertEquals(FeatureContext.Lifecycle.DISCONNECTED, context.lifecycle());
    }
}
