package io.arson.client.render;

import com.arson.client.render.RenderStyle;
import io.arson.client.module.EntityTracerModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

/** Draws simple client-side tracer lines from the camera to cached entity targets. */
public final class EntityTracerStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final EntityScanner scanner;
    private final EntityTracerModule module;
    private List<EntityTarget> cachedTargets = List.of();
    private long lastScanTick = Long.MIN_VALUE;

    public EntityTracerStage(Minecraft client, EntityScanner scanner, EntityTracerModule module) {
        this.client = client;
        this.scanner = scanner;
        this.module = module;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) return;

        long gameTime = client.level.getGameTime();
        if (gameTime - lastScanTick >= module.scanInterval() || gameTime < lastScanTick) {
            cachedTargets = scanner.scan(client, module);
            lastScanTick = gameTime;
        }

        if (cachedTargets.isEmpty()) return;

        var camera = context.worldState().cameraRenderState.pos;
        VertexConsumer buffer = context.consumers().getBuffer(RenderTypes.lines());

        for (EntityTarget target : cachedTargets) {
            RenderStyle style = module.styleFor(target);
            double x = target.centerX() - camera.x;
            double y = target.centerY() - camera.y;
            double z = target.centerZ() - camera.z;
            RenderLineRenderer.line(context.matrices(), buffer,
                    0.0, 0.0, 0.0, x, y, z, style);
        }
    }
}
