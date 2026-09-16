package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderColor;
import com.arson.client.render.RenderStyle;
import io.arson.client.module.AimAssistModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/** Highlights the entity currently selected by Aim Assist. */
@Environment(EnvType.CLIENT)
public final class AimAssistRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final AimAssistModule module;

    public AimAssistRenderStage(Minecraft client, AimAssistModule module) {
        this.client = client;
        this.module = module;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!module.enabled() || !module.highlightTarget() || client.player == null || client.level == null) return;

        LivingEntity target = module.target();
        if (target == null || !target.isAlive() || target.isRemoved()) return;

        double expand = module.highlightExpand();
        var box = target.getBoundingBox().inflate(expand);
        RenderStyle style = new RenderStyle(
                color(module.highlightColor()),
                module.highlightFill(),
                module.highlightOutline(),
                (float) module.highlightFillAlpha(),
                (float) module.highlightOutlineAlpha(),
                (float) module.highlightLineWidth());

        var camera = context.worldState().cameraRenderState.pos;
        RenderBox renderBox = new RenderBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, style);
        List<RenderBox> boxes = List.of(renderBox);
        RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
    }

    private static RenderColor color(int argb) {
        return new RenderColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }
}
