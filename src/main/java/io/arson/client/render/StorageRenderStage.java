package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderColor;
import com.arson.client.render.RenderStyle;
import com.arson.client.render.RenderStyleUtil;
import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import io.arson.client.module.ContainerESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

/** Render-stage adapter for container ESP. Detection is cached and drawing stays in the render stage. */
public final class StorageRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final StorageOverlay overlay;
    private final StorageScanner scanner;
    private final ContainerESPModule module;
    private List<StorageOverlay.StorageTarget> cachedTargets = List.of();
    private long lastScanTick = Long.MIN_VALUE;
    private int lastVisibleCount;

    public StorageRenderStage(Minecraft client, StorageOverlay overlay, StorageScanner scanner,
                              ContainerESPModule module) {
        this.client = client;
        this.overlay = overlay;
        this.scanner = scanner;
        this.module = module;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) {
            lastVisibleCount = 0;
            return;
        }

        syncProfile();
        overlay.range(module.range());

        long gameTime = client.level.getGameTime();
        if (gameTime - lastScanTick >= 5 || gameTime < lastScanTick) {
            cachedTargets = scanner.scan(client, overlay.range());
            lastScanTick = gameTime;
        }

        var camera = context.worldState().cameraRenderState.pos;
        List<RenderBox> boxes = overlay.build(camera.x, camera.y, camera.z, cachedTargets);
        lastVisibleCount = boxes.size();
        if (boxes.isEmpty()) return;

        if (module.fill()) {
            renderFills(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        }
        if (module.outline()) {
            renderOutlines(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        }
    }

    private void syncProfile() {
        StorageRenderProfile profile = overlay.profile();
        profile.enabled(StorageType.CHEST, module.showChests());
        profile.enabled(StorageType.BARREL, module.showBarrels());
        profile.enabled(StorageType.SHULKER, module.showShulkers());
        profile.enabled(StorageType.ENDER_CHEST, module.showEnderChests());
        profile.enabled(StorageType.OTHER, module.showOtherStorage());
        profile.color(StorageType.CHEST, fromArgb(module.chestColor()));
        profile.color(StorageType.BARREL, fromArgb(module.barrelColor()));
        profile.color(StorageType.SHULKER, fromArgb(module.shulkerColor()));
        profile.color(StorageType.ENDER_CHEST, fromArgb(module.enderChestColor()));
        profile.color(StorageType.OTHER, fromArgb(module.otherStorageColor()));
    }

    private static RenderColor fromArgb(int argb) {
        return new RenderColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }

    private static void renderFills(PoseStack matrices, MultiBufferSource consumers,
                                    double cameraX, double cameraY, double cameraZ,
                                    List<RenderBox> boxes) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.debugFilledBox());
        for (RenderBox box : boxes) {
            RenderStyle style = RenderStyleUtil.of(box.color(), true, false, 0.35f, 1.0f, 1.0f);
            float[] c = RenderStyleUtil.rgba(style, false);
            drawBoxFaces(buffer, pose, box, cameraX, cameraY, cameraZ, c);
        }
    }

    private static void renderOutlines(PoseStack matrices, MultiBufferSource consumers,
                                       double cameraX, double cameraY, double cameraZ,
                                       List<RenderBox> boxes) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());
        for (RenderBox box : boxes) {
            RenderStyle style = RenderStyleUtil.of(box.color(), false, true, 0.35f, 1.0f, 1.0f);
            float[] c = RenderStyleUtil.rgba(style, true);
            float minX = (float) (box.minX() - cameraX), minY = (float) (box.minY() - cameraY), minZ = (float) (box.minZ() - cameraZ);
            float maxX = (float) (box.maxX() - cameraX), maxY = (float) (box.maxY() - cameraY), maxZ = (float) (box.maxZ() - cameraZ);
            line(buffer, pose, minX, minY, minZ, maxX, minY, minZ, c);
            line(buffer, pose, maxX, minY, minZ, maxX, minY, maxZ, c);
            line(buffer, pose, maxX, minY, maxZ, minX, minY, maxZ, c);
            line(buffer, pose, minX, minY, maxZ, minX, minY, minZ, c);
            line(buffer, pose, minX, maxY, minZ, maxX, maxY, minZ, c);
            line(buffer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, c);
            line(buffer, pose, maxX, maxY, maxZ, minX, maxY, maxZ, c);
            line(buffer, pose, minX, maxY, maxZ, minX, maxY, minZ, c);
            line(buffer, pose, minX, minY, minZ, minX, maxY, minZ, c);
            line(buffer, pose, maxX, minY, minZ, maxX, maxY, minZ, c);
            line(buffer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, c);
            line(buffer, pose, minX, minY, maxZ, minX, maxY, maxZ, c);
        }
    }

    private static void drawBoxFaces(VertexConsumer buffer, PoseStack.Pose pose, RenderBox box,
                                     double cameraX, double cameraY, double cameraZ, float[] c) {
        float minX = (float) (box.minX() - cameraX), minY = (float) (box.minY() - cameraY), minZ = (float) (box.minZ() - cameraZ);
        float maxX = (float) (box.maxX() - cameraX), maxY = (float) (box.maxY() - cameraY), maxZ = (float) (box.maxZ() - cameraZ);
        quad(buffer, pose, minX, minY, minZ, maxX, minY, maxZ, c);
        quad(buffer, pose, minX, maxY, minZ, maxX, maxY, maxZ, c);
        quad(buffer, pose, minX, minY, minZ, maxX, maxY, minZ, c);
        quad(buffer, pose, minX, minY, maxZ, maxX, maxY, maxZ, c);
        quad(buffer, pose, minX, minY, minZ, minX, maxY, maxZ, c);
        quad(buffer, pose, maxX, minY, minZ, maxX, maxY, maxZ, c);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2, float[] c) {
        if (x1 == x2) {
            vertex(buffer, pose, x1, y1, z1, c); vertex(buffer, pose, x1, y2, z1, c);
            vertex(buffer, pose, x1, y2, z2, c); vertex(buffer, pose, x1, y1, z2, c);
        } else if (y1 == y2) {
            vertex(buffer, pose, x1, y1, z1, c); vertex(buffer, pose, x2, y1, z1, c);
            vertex(buffer, pose, x2, y1, z2, c); vertex(buffer, pose, x1, y1, z2, c);
        } else {
            vertex(buffer, pose, x1, y1, z1, c); vertex(buffer, pose, x2, y1, z1, c);
            vertex(buffer, pose, x2, y2, z1, c); vertex(buffer, pose, x1, y2, z1, c);
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float[] c) {
        buffer.addVertex(pose, x, y, z).setColor(c[0], c[1], c[2], c[3]);
    }

    private static void line(VertexConsumer buffer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2, float[] c) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0e-5f) return;
        buffer.addVertex(pose, x1, y1, z1).setColor(c[0], c[1], c[2], c[3]).setLineWidth(1.0f);
        buffer.addVertex(pose, x2, y2, z2).setColor(c[0], c[1], c[2], c[3]).setLineWidth(1.0f);
    }

    public int lastVisibleCount() { return lastVisibleCount; }
}
