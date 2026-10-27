package io.arson.client.render;

import io.arson.client.context.FeatureContext;
import io.arson.client.platform.FeatureContextAdapter;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Single integration point between Arson's render pipeline and Fabric's world renderer. */
public final class WorldRenderBridge {
    private static final Logger LOGGER = Logger.getLogger(WorldRenderBridge.class.getName());
    private final List<WorldRenderStage> stages = new ArrayList<>();
    private final Set<String> loggedFailures = new HashSet<>();
    private final FeatureContext featureContext;
    private final FeatureContextAdapter contextAdapter;
    private final Supplier<Float> partialTickSource;
    private boolean registered;
    private boolean active;
    private long frame;

    public WorldRenderBridge() { this(new FeatureContext(), null); }
    public WorldRenderBridge(FeatureContext featureContext, FeatureContextAdapter contextAdapter) {
        this(featureContext, contextAdapter, () -> Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }
    WorldRenderBridge(FeatureContext featureContext, FeatureContextAdapter contextAdapter, Supplier<Float> partialTickSource) {
        this.featureContext = featureContext;
        this.contextAdapter = contextAdapter;
        this.partialTickSource = partialTickSource;
    }
    public void register(WorldRenderStage stage) { if (stage != null && !stages.contains(stage)) stages.add(stage); }
    public void attach() {
        if (registered) {
            active = true;
            return;
        }
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!active) return;
            frame++;
            renderFrame(context);
        });
        registered = true;
        active = true;
    }

    /**
     * Disables this bridge's callback without registering a duplicate callback on reattach.
     * Fabric's event API does not expose listener removal for this event, so the registered
     * listener remains inert after detaching.
     */
    public void detach() {
        active = false;
    }

    public boolean active() { return active; }

    /** Runs each pipeline component independently so one faulty stage cannot skip later stages. */
    void renderFrame(WorldRenderContext context) {
        if (contextAdapter != null) {
            try {
                contextAdapter.render(featureContext, frame, partialTick());
            } catch (RuntimeException exception) {
                logFailureOnce("feature-context-adapter", exception);
            }
        }
        // Work on a stable snapshot in case a stage is registered or cleared during a callback.
        for (WorldRenderStage stage : List.copyOf(stages)) {
            try {
                stage.render(context);
            } catch (RuntimeException exception) {
                logFailureOnce(stage.getClass().getName(), exception);
            }
        }
    }

    private void logFailureOnce(String component, RuntimeException exception) {
        if (loggedFailures.add(component)) {
            LOGGER.log(Level.WARNING, "World render component failed; later stages will continue: " + component, exception);
        }
    }

    float partialTick() {
        float value = partialTickSource.get();
        if (!Float.isFinite(value) || value < 0.0f || value > 1.0f) {
            if (loggedFailures.add("partial-tick")) {
                LOGGER.warning("Invalid render partial tick received: " + value + "; clamping to [0, 1]");
            }
        }
        if (!Float.isFinite(value)) return 0.0f;
        return Math.max(0.0f, Math.min(1.0f, value));
    }
    public void clear() { stages.clear(); }
    public int stageCount() { return stages.size(); }
    public FeatureContext featureContext() { return featureContext; }
    public long frame() { return frame; }
    @FunctionalInterface public interface WorldRenderStage { void render(WorldRenderContext context); }
}
