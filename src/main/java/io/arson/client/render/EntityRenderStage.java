package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
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
            RenderStyle style = styleFor(target.type());
            boxes.add(new RenderBox(target.minX(), target.minY(), target.minZ(),
                    target.maxX(), target.maxY(), target.maxZ(), style));
        }

        if (module.fill()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z,
                    boxes, (float) module.fillAlpha());
        }
        if (module.outline()) {
            RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z,
                    boxes, (float) module.outlineAlpha(), (float) module.lineWidth());
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

    /** Renders a compact vertical health bar just to the left of living entity bounds. */
    private static void renderHealthBars(PoseStack matrices, MultiBufferSource consumers,
                                         double cameraX, double cameraY, double cameraZ,
                                         List<EntityTarget> targets) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.debugFilledBox());

        for (EntityTarget target : targets) {
            if (target.maxHealth() <= 0.0f) continue;
            float healthRatio = Math.max(0.0f, Math.min(1.0f, target.health() / target.maxHealth()));
            if (healthRatio <= 0.0f) continue;

            float minX = (float) (target.minX() - cameraX - 0.08);
            float maxX = minX + 0.035f;
            float minY = (float) (target.minY() - cameraY);
            float maxY = (float) (target.maxY() - cameraY);
            float minZ = (float) (target.minZ() - cameraZ);
            float maxZ = minZ + 0.035f;
            float filledMaxY = minY + (maxY - minY) * healthRatio;
            float[] background = new float[]{0.03f, 0.03f, 0.03f, 0.75f};
            quad(buffer, pose, minX - 0.01f, minY, minZ, maxX + 0.01f, maxY, maxZ + 0.01f, background);
            float red = 1.0f - healthRatio;
            float green = healthRatio;
            float[] healthColor = new float[]{red, green, 0.08f, 0.95f};
            quad(buffer, pose, minX, minY, minZ, maxX, filledMaxY, maxZ, healthColor);
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
