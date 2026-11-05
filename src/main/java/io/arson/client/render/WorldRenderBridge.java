package io.arson.client.render;

import io.arson.client.context.FeatureContext;
import io.arson.client.platform.FeatureContextAdapter;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Single integration point between Arson's render pipeline and Fabric's world renderer. */
public final class WorldRenderBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("Arson/WorldRender");
    private final List<WorldRenderStage> stages = new ArrayList<>();
    private final FeatureContext featureContext;
    private final FeatureContextAdapter contextAdapter;
    private final Supplier<Float> partialTickSource;
    private boolean registered;
    private long frame;

    public WorldRenderBridge() { this(new FeatureContext(), null); }
    public WorldRenderBridge(FeatureContext featureContext, FeatureContextAdapter contextAdapter) {
        this(featureContext, contextAdapter, () -> MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true));
    }
    WorldRenderBridge(FeatureContext featureContext, FeatureContextAdapter contextAdapter, Supplier<Float> partialTickSource) {
        this.featureContext = featureContext;
        this.contextAdapter = contextAdapter;
        this.partialTickSource = partialTickSource;
    }
    public void register(WorldRenderStage stage) { if (stage != null && !stages.contains(stage)) stages.add(stage); }
    public void attach() {
        if (registered) return;
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            frame++;
            if (contextAdapter != null) contextAdapter.render(featureContext, frame, partialTick());
            for (WorldRenderStage stage : List.copyOf(stages)) {
                try {
                    stage.render(context);
                } catch (RuntimeException exception) {
                    LOGGER.error("World render stage {} failed; continuing with remaining stages.",
                            stage.getClass().getSimpleName(), exception);
                }
            }
        });
        registered = true;
    }
    float partialTick() { return Math.max(0.0f, Math.min(1.0f, partialTickSource.get())); }
    public void clear() { stages.clear(); }
    public int stageCount() { return stages.size(); }
    public FeatureContext featureContext() { return featureContext; }
    public long frame() { return frame; }
    @FunctionalInterface public interface WorldRenderStage { void render(WorldRenderContext context); }
}
