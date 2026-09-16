package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageRenderProfile;
import com.arson.client.render.StorageType;
import io.arson.client.module.ContainerESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;

/** Render-stage adapter for container ESP. Detection is cached and drawing stays in the render stage. */
public final class StorageRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client; private final StorageOverlay overlay; private final StorageScanner scanner; private final ContainerESPModule module;
    private List<StorageOverlay.StorageTarget> cachedTargets = List.of(); private long lastScanTick = Long.MIN_VALUE; private int lastVisibleCount;
    public StorageRenderStage(Minecraft client, StorageOverlay overlay, StorageScanner scanner, ContainerESPModule module) { this.client = client; this.overlay = overlay; this.scanner = scanner; this.module = module; }
    @Override public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) { lastVisibleCount = 0; return; }
        syncProfile(); overlay.range(module.range()); long gameTime = client.level.getGameTime();
        if (gameTime - lastScanTick >= module.scanInterval() || gameTime < lastScanTick) { cachedTargets = scanner.scan(client, overlay.range()); lastScanTick = gameTime; }
        var camera = context.worldState().cameraRenderState.pos;
        List<RenderBox> boxes = overlay.build(camera.x, camera.y, camera.z, cachedTargets, type -> switch (type) { case CHEST -> module.chestStyle(); case BARREL -> module.barrelStyle(); case SHULKER -> module.shulkerStyle(); case ENDER_CHEST -> module.enderChestStyle(); case OTHER -> module.otherStorageStyle(); });
        if (module.distanceFade() && !boxes.isEmpty()) { List<RenderBox> faded = new ArrayList<>(boxes.size()); for (RenderBox box : boxes) { double dx = box.centerX() - camera.x, dy = box.centerY() - camera.y, dz = box.centerZ() - camera.z; float fade = fadeForDistance(Math.sqrt(dx * dx + dy * dy + dz * dz), module.range()); if (fade > 0.0f) faded.add(withFade(box, fade)); } boxes = faded; }
        lastVisibleCount = boxes.size();
        if (!boxes.isEmpty()) { RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes); RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes); }
        if (module.showLabels() && !cachedTargets.isEmpty()) renderLabels(context, context.worldState().cameraRenderState, cachedTargets);
    }
    private void renderLabels(WorldRenderContext context, net.minecraft.client.renderer.state.CameraRenderState camera, List<StorageOverlay.StorageTarget> targets) {
        PoseStack matrices = context.matrices(); MultiBufferSource consumers = context.consumers(); Font font = client.font; float scale = module.labelScale();
        for (StorageOverlay.StorageTarget target : targets) {
            if (!isVisibleType(target.type())) continue;
            double dx = target.x() + target.width() * .5 - client.player.getX(), dy = target.y() + target.height() * .5 - client.player.getY(), dz = target.z() + target.depth() * .5 - client.player.getZ(); double distance = Math.sqrt(dx * dx + dy * dy + dz * dz); if (distance > module.range()) continue;
            float fade = module.distanceFade() ? fadeForDistance(distance, module.range()) : 1.0f; if (fade <= 0) continue;
            String text = labelText(target.type(), distance); int width = font.width(text); float left = -width / 2.0f; double x = target.x() + target.width() * .5 - camera.pos.x, y = target.y() + target.height() + module.labelHeight() - camera.pos.y, z = target.z() + target.depth() * .5 - camera.pos.z;
            matrices.pushPose(); matrices.translate(x, y, z); matrices.mulPose(camera.orientation); matrices.scale(-.025f * scale, -.025f * scale, .025f * scale);
            int textColor = fadeColor(module.labelColor(), fade); if (module.labelBackground()) { float padding = module.labelPadding(); fillQuad(matrices, consumers, left - padding, -2, left + width + padding, 9, fadeColor(module.labelBackgroundColor(), fade)); }
            Matrix4f pose = matrices.last().pose(); font.drawInBatch(Component.literal(text), left, 0, textColor, false, pose, consumers, Font.DisplayMode.NORMAL, 0, 0xF000F0); matrices.popPose();
        }
    }
    private boolean isVisibleType(StorageType type) { return switch (type) { case CHEST -> module.showChests(); case BARREL -> module.showBarrels(); case SHULKER -> module.showShulkers(); case ENDER_CHEST -> module.showEnderChests(); case OTHER -> module.showOtherStorage(); }; }
    private String labelText(StorageType type, double distance) { String name = switch (type) { case CHEST -> "Chest"; case BARREL -> "Barrel"; case SHULKER -> "Shulker"; case ENDER_CHEST -> "Ender Chest"; case OTHER -> "Storage"; }; return module.labelDistance() ? name + " [" + Math.round(distance) + "m]" : name; }
    private void syncProfile() { StorageRenderProfile profile = overlay.profile(); profile.enabled(StorageType.CHEST, module.showChests()); profile.enabled(StorageType.BARREL, module.showBarrels()); profile.enabled(StorageType.SHULKER, module.showShulkers()); profile.enabled(StorageType.ENDER_CHEST, module.showEnderChests()); profile.enabled(StorageType.OTHER, module.showOtherStorage()); }
    private static float fadeForDistance(double distance, double range) { double n = Math.max(0, Math.min(1, distance / Math.max(1, range))); return (float)Math.max(0, Math.min(1, 1 - Math.max(0, n - .45) / .55)); }
    private static int fadeColor(int argb, float fade) { int alpha = Math.round(((argb >>> 24) & 255) * fade); return (argb & 0x00FFFFFF) | (alpha << 24); }
    private static void fillQuad(PoseStack matrices, MultiBufferSource consumers, float x1, float y1, float x2, float y2, int argb) { var pose = matrices.last(); var buffer = consumers.getBuffer(RenderType.guiOverlay()); float a = ((argb >>> 24) & 255) / 255f, r = ((argb >>> 16) & 255) / 255f, g = ((argb >>> 8) & 255) / 255f, b = (argb & 255) / 255f; buffer.addVertex(pose, x1, y1, 0).setColor(r,g,b,a); buffer.addVertex(pose, x1,y2,0).setColor(r,g,b,a); buffer.addVertex(pose,x2,y2,0).setColor(r,g,b,a); buffer.addVertex(pose,x2,y1,0).setColor(r,g,b,a); }
    private static RenderBox withFade(RenderBox box, float fade) { RenderStyle style = box.style(); return new RenderBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ(), new RenderStyle(style.color(), style.fill(), style.outline(), style.fillAlpha()*fade, style.outlineAlpha()*fade, style.lineWidth())); }
    public int lastVisibleCount() { return lastVisibleCount; }
}
