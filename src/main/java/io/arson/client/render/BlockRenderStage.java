package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import io.arson.client.render.RenderLineRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.arson.client.module.BlockESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** World-render adapter for incrementally scanned block ESP targets. */
public final class BlockRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final BlockScanner scanner;
    private final BlockESPModule module;
    private int lastVisibleCount;

    public BlockRenderStage(Minecraft client, BlockScanner scanner, BlockESPModule module) {
        this.client = client;
        this.scanner = scanner;
        this.module = module;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) {
            lastVisibleCount = 0;
            scanner.invalidate();
            return;
        }

        List<BlockTarget> targets = scanner.scan(client, module);
        if (targets.isEmpty()) {
            lastVisibleCount = 0;
            return;
        }

        var camera = context.worldState().cameraRenderState.pos;
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();
        double range = Math.max(1.0, module.range());
        double rangeSquared = range * range;
        List<RenderBox> boxes = new ArrayList<>(targets.size());
        VertexConsumer tracerBuffer = module.tracers()
                ? RenderLineRenderer.lineBuffer(context.consumers()) : null;

        for (BlockTarget target : targets) {
            BlockPos position = BlockPos.containing(target.minX(), target.minY(), target.minZ());
            if (!client.level.hasChunkAt(position)) continue;

            double dx = target.centerX() - playerX;
            double dy = target.centerY() - playerY;
            double dz = target.centerZ() - playerZ;
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            if (distanceSquared > rangeSquared) continue;

            double distance = Math.sqrt(distanceSquared);
            float fade = module.distanceFade() ? EspRenderMath.distanceFade(distance, range, module.fadeStart()) : 1.0f;
            if (fade <= 0.0f) continue;

            RenderStyle style = EspRenderMath.fadeStyle(target.style(), fade);
            boxes.add(new RenderBox(target.minX(), target.minY(), target.minZ(),
                    target.maxX(), target.maxY(), target.maxZ(), style));
            if (tracerBuffer != null) {
                RenderStyle tracerStyle = new RenderStyle(style.color(), false, true, 0.0f,
                        module.tracerAlpha() * fade, module.tracerLineWidth());
                RenderLineRenderer.line(context.matrices(), tracerBuffer, 0.0, 0.0, 0.0,
                        target.centerX() - camera.x, target.centerY() - camera.y,
                        target.centerZ() - camera.z, tracerStyle);
            }
        }

        lastVisibleCount = boxes.size();
        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        }
    }

    public int lastVisibleCount() { return lastVisibleCount; }
}