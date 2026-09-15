package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import com.arson.client.render.RenderStyleUtil;
import io.arson.client.module.EntityESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.List;

/** Shared world-render stage for entity ESP. */
public final class EntityRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final EntityScanner scanner;
    private final EntityESPModule module;
    private List<EntityTarget> cachedTargets = List.of();
    private long lastScanTick = Long.MIN_VALUE;
    private int lastVisibleCount;

    public EntityRenderStage(Minecraft client, EntityScanner scanner, EntityESPModule module) {
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
        lastVisibleCount = cachedTargets.size();
        List<RenderBox> boxes = new ArrayList<>(cachedTargets.size());
        for (EntityTarget target : cachedTargets) {
            float fade = distanceFade(target);
            if (fade <= 0.0f) continue;
            RenderStyle style = fadeStyle(styleFor(target.type()), fade);
            boxes.add(new RenderBox(target.minX(), target.minY(), target.minZ(),
                    target.maxX(), target.maxY(), target.maxZ(), style));
        }

        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        }
        if (module.showHealth()) {
            renderHealthBars(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, cachedTargets);
        }
    }

    private RenderStyle styleFor(EntityType type) {
        return switch (type) {
            case PLAYER -> module.playerStyle();
            case MOB -> module.mobStyle();
            case ANIMAL -> module.animalStyle();
            case ITEM -> module.itemStyle();
        };
    }

    private float distanceFade(EntityTarget target) {
        if (!module.distanceFade()) return 1.0f;
        double range = Math.max(1.0, module.range());
        double normalized = Math.max(0.0, Math.min(1.0, target.distance() / range));
        return (float) Math.max(0.0, Math.min(1.0, 1.0 - Math.max(0.0, normalized - 0.45) / 0.55));
    }

    private static RenderStyle fadeStyle(RenderStyle style, float fade) {
        return new RenderStyle(style.color(), style.fill(), style.outline(),
                style.fillAlpha() * fade, style.outlineAlpha() * fade, style.lineWidth());
    }

    /** Renders a compact vertical health bar beside living entity bounds. */
    private void renderHealthBars(PoseStack matrices, MultiBufferSource consumers,
                                  double cameraX, double cameraY, double cameraZ,
                                  List<EntityTarget> targets) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.debugFilledBox());
        RenderStyle healthStyle = module.healthStyle();
        RenderStyle backgroundStyle = module.healthBackgroundStyle();
        float width = (float) module.healthWidth();
        float offset = (float) module.healthOffset();

        for (EntityTarget target : targets) {
            if (target.maxHealth() <= 0.0f) continue;
            float fade = distanceFade(target);
            if (fade <= 0.0f) continue;

            float healthRatio = Math.max(0.0f, Math.min(1.0f, target.health() / target.maxHealth()));
            if (healthRatio <= 0.0f) continue;

            float edgeX = module.healthRight()
                    ? (float) (target.maxX() - cameraX + offset)
                    : (float) (target.minX() - cameraX - offset);
            float minX = module.healthRight() ? edgeX : edgeX - width;
            float maxX = module.healthRight() ? edgeX + width : edgeX;
            float minY = (float) (target.minY() - cameraY);
            float maxY = (float) (target.maxY() - cameraY);
            float minZ = (float) (target.minZ() - cameraZ);
            float maxZ = minZ + width;
            float filledMaxY = minY + (maxY - minY) * healthRatio;

            RenderStyle fadedBackground = fadeStyle(backgroundStyle, fade);
            RenderStyle fadedHealth = fadeStyle(healthStyle, fade);
            if (module.showHealthBackground()) {
                quad(buffer, pose, minX - 0.01f, minY, minZ, maxX + 0.01f, maxY, maxZ + 0.01f,
                        RenderStyleUtil.rgba(fadedBackground, false));
            }
            quad(buffer, pose, minX, minY, minZ, maxX, filledMaxY, maxZ,
                    RenderStyleUtil.rgba(fadedHealth, false));
        }
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float[] rgba) {
        if (x1 == x2) {
            vertex(buffer, pose, x1, y1, z1, rgba); vertex(buffer, pose, x1, y2, z1, rgba);
            vertex(buffer, pose, x1, y2, z2, rgba); vertex(buffer, pose, x1, y1, z2, rgba);
        } else if (y1 == y2) {
            vertex(buffer, pose, x1, y1, z1, rgba); vertex(buffer, pose, x2, y1, z1, rgba);
            vertex(buffer, pose, x2, y1, z2, rgba); vertex(buffer, pose, x1, y1, z2, rgba);
        } else {
            vertex(buffer, pose, x1, y1, z1, rgba); vertex(buffer, pose, x2, y1, z1, rgba);
            vertex(buffer, pose, x2, y2, z1, rgba); vertex(buffer, pose, x1, y2, z1, rgba);
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                               float x, float y, float z, float[] rgba) {
        buffer.addVertex(pose, x, y, z).setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}
