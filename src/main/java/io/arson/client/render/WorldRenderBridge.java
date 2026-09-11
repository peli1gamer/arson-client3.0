package io.arson.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import java.util.ArrayList;
import java.util.List;

/** Single integration point between Arson's render pipeline and Fabric's world renderer. */
public final class WorldRenderBridge {
    private final List<WorldRenderStage> stages = new ArrayList<>();
    private boolean registered;

    public void register(WorldRenderStage stage) {
        if (stage != null && !stages.contains(stage)) {
            stages.add(stage);
        }
    }

    public void attach() {
        if (registered) return;

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            for (WorldRenderStage stage : stages) {
                stage.render(context);
            }
        });
        registered = true;
    }

    public void clear() {
        stages.clear();
    }

    @FunctionalInterface
    public interface WorldRenderStage {
        void render(WorldRenderContext context);
    }
}
