package io.arson.client.render;

import com.arson.client.render.RenderColor;
import io.arson.client.module.EntityESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

/**
 * Shared world-render stage for entity ESP. Scanning is throttled to avoid doing entity discovery
 * for every render frame; drawing remains on the render stage.
 */
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
        if (gameTime - lastScanTick >= 2 || gameTime < lastScanTick) {
            cachedTargets = scanner.scan(client, module);
            lastScanTick = gameTime;
        }

        if (cachedTargets.isEmpty()) {
            lastVisibleCount = 0;
            return;
        }

        var camera = context.worldState().cameraRenderState.pos;
        lastVisibleCount = cachedTargets.size();

        if (module.fill()) {
            renderFills(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, cachedTargets);
        }
        if (module.outline()) {
            renderOutlines(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, cachedTargets);
        }
    }

    private static void renderFills(PoseStack matrices, MultiBufferSource consumers,
                                    double cameraX, double cameraY, double cameraZ,
                                    List<EntityTarget> targets) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.debugFilledBox());

        for (EntityTarget target : targets) {
            float[] rgba = rgba(target.color(), 0.30f);
            float minX = (float) (target.minX() - cameraX);
            float minY = (float) (target.minY() - cameraY);
            float minZ = (float) (target.minZ() - cameraZ);
            float maxX = (float) (target.maxX() - cameraX);
            float maxY = (float) (target.maxY() - cameraY);
            float maxZ = (float) (target.maxZ() - cameraZ);

            quad(buffer, pose, minX, minY, minZ, maxX, minY, maxZ, rgba);
            quad(buffer, pose, minX, maxY, minZ, maxX, maxY, maxZ, rgba);
            quad(buffer, pose, minX, minY, minZ, maxX, maxY, minZ, rgba);
            quad(buffer, pose, minX, minY, maxZ, maxX, maxY, maxZ, rgba);
            quad(buffer, pose, minX, minY, minZ, minX, maxY, maxZ, rgba);
            quad(buffer, pose, maxX, minY, minZ, maxX, maxY, maxZ, rgba);
        }
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float[] rgba) {
        if (x1 == x2) {
            vertex(buffer, pose, x1, y1, z1, rgba);
            vertex(buffer, pose, x1, y2, z1, rgba);
            vertex(buffer, pose, x1, y2, z2, rgba);
            vertex(buffer, pose, x1, y1, z2, rgba);
        } else if (y1 == y2) {
            vertex(buffer, pose, x1, y1, z1, rgba);
            vertex(buffer, pose, x2, y1, z1, rgba);
            vertex(buffer, pose, x2, y1, z2, rgba);
            vertex(buffer, pose, x1, y1, z2, rgba);
        } else {
            vertex(buffer, pose, x1, y1, z1, rgba);
            vertex(buffer, pose, x2, y1, z1, rgba);
            vertex(buffer, pose, x2, y2, z1, rgba);
            vertex(buffer, pose, x1, y2, z1, rgba);
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                               float x, float y, float z, float[] rgba) {
        buffer.addVertex(pose, x, y, z).setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    private static void renderOutlines(PoseStack matrices, MultiBufferSource consumers,
                                       double cameraX, double cameraY, double cameraZ,
                                       List<EntityTarget> targets) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());

        for (EntityTarget target : targets) {
            float[] rgba = rgba(target.color(), 1.0f);
            float minX = (float) (target.minX() - cameraX);
            float minY = (float) (target.minY() - cameraY);
            float minZ = (float) (target.minZ() - cameraZ);
            float maxX = (float) (target.maxX() - cameraX);
            float maxY = (float) (target.maxY() - cameraY);
            float maxZ = (float) (target.maxZ() - cameraZ);

            line(buffer, pose, minX, minY, minZ, maxX, minY, minZ, rgba);
            line(buffer, pose, maxX, minY, minZ, maxX, minY, maxZ, rgba);
            line(buffer, pose, maxX, minY, maxZ, minX, minY, maxZ, rgba);
            line(buffer, pose, minX, minY, maxZ, minX, minY, minZ, rgba);
            line(buffer, pose, minX, maxY, minZ, maxX, maxY, minZ, rgba);
            line(buffer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, rgba);
            line(buffer, pose, maxX, maxY, maxZ, minX, maxY, maxZ, rgba);
            line(buffer, pose, minX, maxY, maxZ, minX, maxY, minZ, rgba);
            line(buffer, pose, minX, minY, minZ, minX, maxY, minZ, rgba);
            line(buffer, pose, maxX, minY, minZ, maxX, maxY, minZ, rgba);
            line(buffer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, rgba);
            line(buffer, pose, minX, minY, maxZ, minX, maxY, maxZ, rgba);
        }
    }

    private static void line(VertexConsumer buffer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float[] rgba) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0e-5f) return;
        dx /= length;
        dy /= length;
        dz /= length;

        buffer.addVertex(pose, x1, y1, z1)
                .setColor(rgba[0], rgba[1], rgba[2], rgba[3])
                .setNormal(pose, dx, dy, dz)
                .setLineWidth(1.0f);
        buffer.addVertex(pose, x2, y2, z2)
                .setColor(rgba[0], rgba[1], rgba[2], rgba[3])
                .setNormal(pose, dx, dy, dz)
                .setLineWidth(1.0f);
    }

    private static float[] rgba(RenderColor color, float alphaMultiplier) {
        int argb = color.argb();
        float alpha = (((argb >>> 24) & 0xFF) / 255.0f) * alphaMultiplier;
        return new float[]{
                ((argb >>> 16) & 0xFF) / 255.0f,
                ((argb >>> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f,
                alpha
        };
    }

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}
