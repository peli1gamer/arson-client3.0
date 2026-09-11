package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderColor;
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

        if (!module.outline() || boxes.isEmpty()) {
            return;
        }

        renderOutlines(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
    }

    private void syncProfile() {
        StorageRenderProfile profile = overlayProfile();
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

    private StorageRenderProfile overlayProfile() {
        return overlay.profile();
    }

    private static RenderColor fromArgb(int argb) {
        return new RenderColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }

    private static void renderOutlines(PoseStack matrices, MultiBufferSource consumers,
                                       double cameraX, double cameraY, double cameraZ,
                                       List<RenderBox> boxes) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());

        for (RenderBox box : boxes) {
            int argb = box.color().argb();
            float alpha = ((argb >>> 24) & 0xFF) / 255.0f;
            float red = ((argb >>> 16) & 0xFF) / 255.0f;
            float green = ((argb >>> 8) & 0xFF) / 255.0f;
            float blue = (argb & 0xFF) / 255.0f;

            float minX = (float) (box.minX() - cameraX);
            float minY = (float) (box.minY() - cameraY);
            float minZ = (float) (box.minZ() - cameraZ);
            float maxX = (float) (box.maxX() - cameraX);
            float maxY = (float) (box.maxY() - cameraY);
            float maxZ = (float) (box.maxZ() - cameraZ);

            line(buffer, pose, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
            line(buffer, pose, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
            line(buffer, pose, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
            line(buffer, pose, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);
            line(buffer, pose, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
            line(buffer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
            line(buffer, pose, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
            line(buffer, pose, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);
            line(buffer, pose, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
            line(buffer, pose, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
            line(buffer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
            line(buffer, pose, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        }
    }

    private static void line(VertexConsumer buffer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float red, float green, float blue, float alpha) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0e-5f) return;
        dx /= length;
        dy /= length;
        dz /= length;

        buffer.addVertex(pose, x1, y1, z1)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, dx, dy, dz)
                .setLineWidth(1.0f);
        buffer.addVertex(pose, x2, y2, z2)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, dx, dy, dz)
                .setLineWidth(1.0f);
    }

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}
