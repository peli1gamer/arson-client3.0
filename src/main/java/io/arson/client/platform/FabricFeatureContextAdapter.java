package io.arson.client.platform;

import io.arson.client.context.ClientState;
import io.arson.client.context.FeatureContext;
import io.arson.client.context.RenderState;
import net.minecraft.client.Minecraft;

/** Fabric/Minecraft adapter; all game-object access is kept behind this boundary. */
public final class FabricFeatureContextAdapter implements FeatureContextAdapter {
    private final Minecraft client;
    public FabricFeatureContextAdapter(Minecraft client) { this.client = client; }
    @Override public void initialize(FeatureContext context) { context.initialize(); context.tick(snapshot()); }
    @Override public void tick(FeatureContext context) { context.tick(snapshot()); }
    @Override public void render(FeatureContext context, long frame, float partialTick) { context.render(frame, partialTick, RenderState.Phase.AFTER_ENTITIES); }
    @Override public void disconnect(FeatureContext context) { context.disconnect(); }
    public ClientState snapshot() {
        if (client.level == null || client.player == null) {
            long tick = client.level == null ? 0L : client.level.getGameTime();
            return new ClientState(tick, tick, false, client.screen != null, "", 0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        var player = client.player;
        long gameTime = client.level.getGameTime();
        return new ClientState(gameTime, gameTime, true, client.screen != null, client.level.dimension().identifier().toString(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.getHealth(), player.getMaxHealth());
    }
}
