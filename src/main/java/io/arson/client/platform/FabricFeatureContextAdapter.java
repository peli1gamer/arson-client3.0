package io.arson.client.platform;

import io.arson.client.context.ClientState;
import io.arson.client.context.FeatureContext;
import io.arson.client.context.RenderState;
import net.minecraft.client.MinecraftClient;

/** Fabric/Minecraft adapter; all game-object access is kept behind this boundary. */
public final class FabricFeatureContextAdapter implements FeatureContextAdapter {
    private final MinecraftClient client;
    public FabricFeatureContextAdapter(MinecraftClient client) { this.client = client; }
    @Override public void initialize(FeatureContext context) { context.initialize(); context.tick(snapshot()); }
    @Override public void tick(FeatureContext context) { context.tick(snapshot()); }
    @Override public void render(FeatureContext context, long frame, float partialTick) { context.render(frame, partialTick, RenderState.Phase.AFTER_ENTITIES); }
    @Override public void disconnect(FeatureContext context) { context.disconnect(); }
    public ClientState snapshot() {
        if (client.world == null || client.player == null) {
            long tick = client.world == null ? 0L : client.world.getTime();
            return new ClientState(tick, tick, false, client.currentScreen != null, "", 0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        var player = client.player;
        long gameTime = client.world.getTime();
        return new ClientState(gameTime, gameTime, true, client.currentScreen != null, client.world.getRegistryKey().getValue().toString(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), player.getHealth(), player.getMaxHealth());
    }
}
