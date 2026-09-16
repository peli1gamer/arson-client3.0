package io.arson.client.context;

import java.util.Objects;

/**
 * Shared lifecycle boundary used by modules, HUD/render integration and platform adapters.
 * The context itself contains no Fabric or Minecraft types, making propagation testable in isolation.
 */
public final class FeatureContext {
    public enum Lifecycle { NEW, INITIALIZED, WORLD, DISCONNECTED, STOPPED }

    private ClientState clientState = ClientState.empty();
    private RenderState renderState = RenderState.empty();
    private Lifecycle lifecycle = Lifecycle.NEW;

    public synchronized void initialize() {
        lifecycle = Lifecycle.INITIALIZED;
    }

    public synchronized void worldAvailable(ClientState state) {
        clientState = Objects.requireNonNull(state, "state");
        lifecycle = state.inWorld() ? Lifecycle.WORLD : Lifecycle.INITIALIZED;
    }

    public synchronized void tick(ClientState state) {
        clientState = Objects.requireNonNull(state, "state");
        if (lifecycle == Lifecycle.NEW) lifecycle = Lifecycle.INITIALIZED;
        if (state.inWorld()) lifecycle = Lifecycle.WORLD;
        else if (lifecycle == Lifecycle.WORLD) lifecycle = Lifecycle.DISCONNECTED;
    }

    public synchronized void render(long frame, float partialTick, RenderState.Phase phase) {
        renderState = new RenderState(frame, clientState.tick(), partialTick, Objects.requireNonNull(phase, "phase"));
    }

    public synchronized void disconnect() {
        lifecycle = Lifecycle.DISCONNECTED;
        clientState = ClientState.empty();
        renderState = RenderState.empty();
    }

    public synchronized void stop() {
        lifecycle = Lifecycle.STOPPED;
    }

    public synchronized ClientState state() { return clientState; }
    public synchronized RenderState renderState() { return renderState; }
    public synchronized Lifecycle lifecycle() { return lifecycle; }
}
