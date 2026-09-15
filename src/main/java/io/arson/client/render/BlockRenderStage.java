package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import io.arson.client.module.BlockESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/** World-render adapter for cached Block ESP targets. */
public final class BlockRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final BlockScanner scanner;
    private final BlockESPModule module;
    private List<BlockTarget> cachedTargets = List.of();
    private long lastScanTick = Long.MIN_VALUE;
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
            return;
        }

        long gameTime = client.level.getGameTime();
        if (gameTime - lastScanTick >= module.scanInterval() || gameTime < lastScanTick) {
            cachedTargets = scanner.scan(client, module);
            lastScanTick = gameTime;
        }

        if (cachedTargets.isEmpty()) {
            lastVisibleCount = 0;
            return;
        }

        var camera = context.worldState().cameraRenderState.pos;
        List<RenderBox> boxes = new ArrayList<>(cachedTargets.size());
        for (BlockTarget target : cachedTargets) {
            float fade = distanceFade(target.distance());
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

    private float distanceFade(double distance) {
        if (!module.distanceFade()) return 1.0f;
        double range = Math.max(1.0, module.range());
        double normalized = Math.max(0.0, Math.min(1.0, distance / range));
        return (float) Math.max(0.0, Math.min(1.0,
                1.0 - Math.max(0.0, normalized - 0.45) / 0.55));
    }

    private static RenderStyle fadeStyle(RenderStyle style, float fade) {
        return new RenderStyle(style.color(), style.fill(), style.outline(),
                style.fillAlpha() * fade, style.outlineAlpha() * fade, style.lineWidth());
    }

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}
