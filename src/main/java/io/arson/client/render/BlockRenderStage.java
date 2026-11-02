package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import io.arson.client.module.BlockESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;

/** World-render adapter for incrementally scanned Ore ESP targets. */
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
        double playerX = client.player.getX(), playerY = client.player.getY(), playerZ = client.player.getZ();
        double range = Math.max(1.0, module.range());
        List<RenderBox> boxes = new ArrayList<>(targets.size());
        for (BlockTarget target : targets) {
            double dx = target.centerX() - playerX, dy = target.centerY() - playerY, dz = target.centerZ() - playerZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > range) continue;
            float fade = module.distanceFade() ? distanceFade(distance, range) : 1.0f;
            if (fade <= 0.0f) continue;
            RenderStyle style = fadeStyle(target.style(), fade);
            boxes.add(new RenderBox(target.minX(), target.minY(), target.minZ(),
                    target.maxX(), target.maxY(), target.maxZ(), style));
        }
        lastVisibleCount = boxes.size();
        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        }
    }

    private static float distanceFade(double distance, double range) {
        if (range <= 0.0) return 0.0f;
        double normalized = Math.max(0.0, Math.min(1.0, distance / range));
        return (float) Math.max(0.0, Math.min(1.0, 1.0 - Math.max(0.0, normalized - 0.45) / 0.55));
    }

    private static RenderStyle fadeStyle(RenderStyle style, float fade) {
        return new RenderStyle(style.color(), style.fill(), style.outline(),
                style.fillAlpha() * fade, style.outlineAlpha() * fade, style.lineWidth());
    }

    public int lastVisibleCount() { return lastVisibleCount; }
}