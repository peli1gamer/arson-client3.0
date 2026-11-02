package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import io.arson.client.module.*;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Renders the completed specialized-detection snapshot using each detector's own visual settings. */
public final class StructureRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final StructureScanner scanner;
    private final List<StructureESPModule> modules;
    private final Map<StructureESPModule.Detector, StructureESPModule> byType =
            new EnumMap<>(StructureESPModule.Detector.class);

    public StructureRenderStage(Minecraft client, StructureScanner scanner, List<StructureESPModule> modules) {
        this.client = client;
        this.scanner = scanner;
        this.modules = List.copyOf(modules);
        for (StructureESPModule module : modules) byType.put(module.detector(), module);
    }

    @Override
    public void render(WorldRenderContext context) {
        if (client.level == null || client.player == null || modules.stream().noneMatch(StructureESPModule::enabled)) {
            scanner.invalidate();
            return;
        }
        List<StructureTarget> targets = scanner.scan(client, modules);
        if (targets.isEmpty()) return;

        var camera = context.worldState().cameraRenderState.pos;
        List<RenderBox> boxes = new ArrayList<>(targets.size());
        for (StructureTarget target : targets) {
            StructureESPModule module = byType.get(target.detector());
            if (module == null || !module.enabled()) continue;
            double cx = (target.minX() + target.maxX()) * 0.5;
            double cy = (target.minY() + target.maxY()) * 0.5;
            double cz = (target.minZ() + target.maxZ()) * 0.5;
            double dx = cx - camera.x, dy = cy - camera.y, dz = cz - camera.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            float fade = EspRenderMath.distanceFade(distance, module.range(), module.fadeStart());
            if (fade <= 0.0f) continue;
            RenderStyle style = EspRenderMath.fadeStyle(module.renderStyle(), fade);
            boxes.add(new RenderBox(target.minX(), target.minY(), target.minZ(),
                    target.maxX(), target.maxY(), target.maxZ(), style));
        }
        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        }
    }
}
