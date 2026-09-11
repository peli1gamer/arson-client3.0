package io.arson.client.render;

import io.arson.client.module.EntityTracerModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
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
            cachedTargets = scanner.scan(client, new EntityESPModuleView(module));
            lastScanTick = gameTime;
        }

        if (cachedTargets.isEmpty()) return;

        var camera = context.worldState().cameraRenderState.pos;
        PoseStack.Pose pose = context.matrices().last();
        VertexConsumer buffer = context.consumers().getBuffer(RenderTypes.lines());
        int argb = module.color();
        float r = ((argb >>> 16) & 0xFF) / 255.0f;
        float g = ((argb >>> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        float a = ((argb >>> 24) & 0xFF) / 255.0f;
        float width = (float) module.lineWidth();

        for (EntityTarget target : cachedTargets) {
            float x = (float) (target.centerX() - camera.x);
            float y = (float) (target.centerY() - camera.y);
            float z = (float) (target.centerZ() - camera.z);
            float length = (float) Math.sqrt(x * x + y * y + z * z);
            if (length < 0.001f) continue;
            float nx = x / length;
            float ny = y / length;
            float nz = z / length;

            buffer.addVertex(pose, 0.0f, 0.0f, 0.0f)
                    .setColor(r, g, b, a)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(width);
            buffer.addVertex(pose, x, y, z)
                    .setColor(r, g, b, a)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(width);
        }
    }

    /** Adapter exposing tracer filters through the existing scanner without duplicating entity discovery. */
    private static final class EntityESPModuleView extends io.arson.client.module.EntityESPModule {
        private final EntityTracerModule source;

        private EntityESPModuleView(EntityTracerModule source) {
            this.source = source;
        }

        @Override public boolean showPlayers() { return source.showPlayers(); }
        @Override public boolean showMobs() { return source.showMobs(); }
        @Override public boolean showAnimals() { return source.showAnimals(); }
        @Override public boolean showItems() { return source.showItems(); }
        @Override public double range() { return source.range(); }
    }
}
