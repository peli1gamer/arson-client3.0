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
        if (lifecycle == Lifecycle.STOPPED) return;
        if (lifecycle == Lifecycle.NEW || lifecycle == Lifecycle.DISCONNECTED) lifecycle = Lifecycle.INITIALIZED;
    }

    public synchronized void worldAvailable(ClientState state) {
        if (lifecycle == Lifecycle.STOPPED) return;
        clientState = Objects.requireNonNull(state, "state");
        lifecycle = state.inWorld() ? Lifecycle.WORLD : Lifecycle.INITIALIZED;
        if (!state.inWorld()) renderState = RenderState.empty();
    }

    public synchronized void tick(ClientState state) {
        if (lifecycle == Lifecycle.STOPPED) return;
        ClientState next = Objects.requireNonNull(state, "state");
        boolean wasInWorld = lifecycle == Lifecycle.WORLD || clientState.inWorld();
        clientState = next;
        if (next.inWorld()) lifecycle = Lifecycle.WORLD;
        else {
            if (wasInWorld) renderState = RenderState.empty();
            lifecycle = lifecycle == Lifecycle.NEW ? Lifecycle.INITIALIZED : Lifecycle.DISCONNECTED;
        }
    }

    public synchronized void render(long frame, float partialTick, RenderState.Phase phase) {
        if (lifecycle == Lifecycle.STOPPED || !clientState.inWorld()) return;
        renderState = new RenderState(frame, clientState.tick(), partialTick, Objects.requireNonNull(phase, "phase"));
    }

    public synchronized void disconnect() {
        if (lifecycle == Lifecycle.STOPPED) return;
        lifecycle = Lifecycle.DISCONNECTED;
        clientState = ClientState.empty();
        renderState = RenderState.empty();
    }

    public synchronized void stop() {
        lifecycle = Lifecycle.STOPPED;
        clientState = ClientState.empty();
        renderState = RenderState.empty();
    }

    public synchronized ClientState state() { return clientState; }
    public synchronized RenderState renderState() { return renderState; }
    public synchronized Lifecycle lifecycle() { return lifecycle; }
}
