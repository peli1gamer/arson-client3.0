package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import io.arson.client.module.HoleESPModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/** Renders cached safe and unsafe hole geometry. */
@Environment(EnvType.CLIENT)
public final class HoleRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final HoleScanner scanner;
    private final HoleESPModule module;
    private int lastVisibleCount;

    public HoleRenderStage(Minecraft client, HoleScanner scanner, HoleESPModule module) {
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

        List<HoleDetector.HoleTarget> targets = scanner.scan(client, module);
        if (targets.isEmpty()) {
            lastVisibleCount = 0;
            return;
        }

        var camera = context.worldState().cameraRenderState.pos;
        double playerX = client.player.getX();
        double playerY = client.player.getY();
        double playerZ = client.player.getZ();
        List<RenderBox> boxes = new ArrayList<>(targets.size());

        for (HoleDetector.HoleTarget target : targets) {
            if (target.isDouble() ? !module.showDoubleHoles() : !module.showSingleHoles()) continue;
            if (target.safe() ? !module.showSafeHoles() : !module.showUnsafeHoles()) continue;
            if (!HoleScanner.isTargetLoaded(client, target)) continue;

            double centerX = target.x() + target.width() * 0.5;
            double centerY = target.y() + 1.0;
            double centerZ = target.z() + target.depth() * 0.5;
            double dx = centerX - playerX;
            double dy = centerY - playerY;
            double dz = centerZ - playerZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > module.range()) continue;

            float fade = module.distanceFade()
                    ? EspRenderMath.distanceFade(distance, module.range(), module.fadeStart())
                    : 1.0f;
            if (fade <= 0.0f) continue;

            RenderStyle style = target.safe() ? module.safeStyle() : module.unsafeStyle();
            boxes.add(new RenderBox(target.x(), target.y(), target.z(),
                    target.x() + target.width(), target.y() + 2.0, target.z() + target.depth(),
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
