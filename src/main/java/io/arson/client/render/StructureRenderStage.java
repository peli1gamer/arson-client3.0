package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import io.arson.client.module.StructureESPModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/** Renders cached tunnel and staircase bounding volumes. */
@Environment(EnvType.CLIENT)
public final class StructureRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final StructureScanner scanner;
    private final StructureESPModule module;
    private int lastVisibleCount;

    public StructureRenderStage(Minecraft client, StructureScanner scanner, StructureESPModule module) {
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

        List<StructureDetector.StructureTarget> targets = scanner.scan(client, module);
        if (targets.isEmpty()) {
            lastVisibleCount = 0;
            return;
        }

        var camera = context.worldState().cameraRenderState.pos;
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();
        List<RenderBox> boxes = new ArrayList<>(targets.size());

        for (StructureDetector.StructureTarget target : targets) {
            if (target.type() == StructureDetector.StructureType.TUNNEL && !module.showTunnels()) continue;
            if (target.type() == StructureDetector.StructureType.STAIRCASE && !module.showStaircases()) continue;
            if (!StructureScanner.isTargetLoaded(client, target)) continue;

            double nearestX = Math.max(target.minX(), Math.min(target.maxX(), playerX));
            double nearestY = Math.max(target.minY(), Math.min(target.maxY(), playerY));
            double nearestZ = Math.max(target.minZ(), Math.min(target.maxZ(), playerZ));
            double dx = nearestX - playerX;
            double dy = nearestY - playerY;
            double dz = nearestZ - playerZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > module.range()) continue;

            float fade = module.distanceFade()
                    ? EspRenderMath.distanceFade(distance, module.range(), module.fadeStart())
                    : 1.0f;
            if (fade <= 0.0f) continue;

            RenderStyle style = target.type() == StructureDetector.StructureType.TUNNEL
                    ? module.tunnelStyle() : module.staircaseStyle();
            boxes.add(new RenderBox(target.minX(), target.minY(), target.minZ(),
                    target.maxX(), target.maxY(), target.maxZ(),
                    EspRenderMath.fadeStyle(style, fade)));
        }

        lastVisibleCount = boxes.size();
        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(),
                    camera.x, camera.y, camera.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(),
                    camera.x, camera.y, camera.z, boxes);
        }
    }

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}
