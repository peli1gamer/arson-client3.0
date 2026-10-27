package io.arson.client.render;

import io.arson.client.context.FeatureContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void failingStageDoesNotPreventLaterStagesFromRendering() {
        WorldRenderBridge bridge = new WorldRenderBridge(new FeatureContext(), null, () -> 0.5f);
        AtomicInteger laterStageCalls = new AtomicInteger();
        bridge.register(context -> { throw new IllegalStateException("simulated stage failure"); });
        bridge.register(context -> laterStageCalls.incrementAndGet());

        bridge.renderFrame(null);

        assertEquals(1, laterStageCalls.get());
    }

    @Test
    void renderBridgeUsesSnapshotWhenStagesMutateDuringRendering() {
        WorldRenderBridge bridge = new WorldRenderBridge(new FeatureContext(), null, () -> 0.5f);
        AtomicInteger calls = new AtomicInteger();
        bridge.register(context -> {
            calls.incrementAndGet();
            bridge.clear();
        });
        bridge.register(context -> calls.incrementAndGet());

        bridge.renderFrame(null);

        assertEquals(2, calls.get());
        assertEquals(0, bridge.stageCount());
    }

}
