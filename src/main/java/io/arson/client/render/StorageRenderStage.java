package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderColor;
import com.arson.client.render.RenderStyle;
import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageRenderProfile;
import com.arson.client.render.StorageType;
import io.arson.client.ArsonClient;
import io.arson.client.module.ContainerESPModule;
import io.arson.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;

public final class StorageRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final StorageOverlay overlay;
    private final StorageScanner scanner;
    private final ContainerESPModule module;
    private List<StorageOverlay.StorageTarget> cachedTargets = List.of();
    private List<StorageClusterDetector.Cluster> cachedClusters = List.of();
    private long lastScanTick = Long.MIN_VALUE;
    private long lastScannerRevision = Long.MIN_VALUE;
    private int lastVisibleCount;

    public StorageRenderStage(Minecraft client, StorageOverlay overlay, StorageScanner scanner, ContainerESPModule module) {
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
        long scanIntervalTicks = Math.max(1, module.scanInterval()) * 20L;
        if (gameTime - lastScanTick >= scanIntervalTicks || gameTime < lastScanTick
                || scanner.revision() != lastScannerRevision) {
            cachedTargets = scanner.scan(client, overlay.range(), module.customStorageBlocks(), scanIntervalTicks);
            cachedClusters = module.clusterEnabled()
                    ? StorageClusterDetector.detect(cachedTargets.stream().filter(target -> isVisibleType(target.type())).toList(), module.clusterRadius())
                    : List.of();
            lastScanTick = gameTime;
            lastScannerRevision = scanner.revision();
        }

        var camera = context.worldState().cameraRenderState;
        List<RenderBox> boxes = new ArrayList<>(overlay.build(camera.pos.x, camera.pos.y, camera.pos.z,
                cachedTargets, type -> switch (type) {
                    case CHEST -> module.chestStyle();
                    case BARREL -> module.barrelStyle();
                    case SHULKER -> module.shulkerStyle();
                    case ENDER_CHEST -> module.enderChestStyle();
                    case OTHER -> module.otherStorageStyle();
                }));
        for (StorageClusterDetector.Cluster cluster : cachedClusters) boxes.add(clusterBox(cluster));
        boxes = applyRenderMode(boxes, ArsonClient.getInstance().modules().renderMode());

        if (module.distanceFade() && !boxes.isEmpty()) {
            List<RenderBox> faded = new ArrayList<>(boxes.size());
            for (RenderBox box : boxes) {
                double dx = box.centerX() - camera.pos.x, dy = box.centerY() - camera.pos.y, dz = box.centerZ() - camera.pos.z;
                float fade = fadeForDistance(Math.sqrt(dx * dx + dy * dy + dz * dz), module.range());
                if (fade > 0) faded.add(withFade(box, fade));
            }
            boxes = faded;
        }
        lastVisibleCount = boxes.size();
        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.pos.x, camera.pos.y, camera.pos.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.pos.x, camera.pos.y, camera.pos.z, boxes);
            if (module.showTracers()) {
                RenderBoxRenderer.tracers(context.matrices(), context.consumers(), camera.pos.x, camera.pos.y, camera.pos.z, boxes);
            }
        }
        if (module.showLabels()) {
            if (!cachedTargets.isEmpty()) renderLabels(context, camera, cachedTargets);
            if (!cachedClusters.isEmpty()) renderClusterLabels(context, camera, cachedClusters);
        }
    }

    private RenderBox clusterBox(StorageClusterDetector.Cluster cluster) {
        int argb = module.clusterColor();
        RenderColor color = new RenderColor((argb >>> 16) & 0xFF, (argb >>> 8) & 0xFF,
                argb & 0xFF, (argb >>> 24) & 0xFF);
        RenderStyle style = new RenderStyle(color, false, true, 0.0f, 1.0f, 1.5f);
        return new RenderBox(cluster.minX(), cluster.minY(), cluster.minZ(),
                cluster.maxX(), cluster.maxY(), cluster.maxZ(), style);
    }

    private void renderLabels(WorldRenderContext context, net.minecraft.client.renderer.state.CameraRenderState camera,
                              List<StorageOverlay.StorageTarget> targets) {
        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        Font font = client.font;
        float scale = module.labelScale();
        for (StorageOverlay.StorageTarget target : targets) {
            if (!isVisibleType(target.type())) continue;
            double dx = target.x() + target.width() * .5 - client.player.getX();
            double dy = target.y() + target.height() * .5 - client.player.getY();
            double dz = target.z() + target.depth() * .5 - client.player.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > module.range()) continue;
            float fade = module.distanceFade() ? fadeForDistance(distance, module.range()) : 1;
            if (fade <= 0) continue;
            String text = labelText(target.type(), distance);
            drawLabel(matrices, consumers, font, camera, target.x() + target.width() * .5,
                    target.y() + target.height() + module.labelHeight(), target.z() + target.depth() * .5,
                    text, module.labelColor(), fade, scale);
        }
    }

    private void renderClusterLabels(WorldRenderContext context, net.minecraft.client.renderer.state.CameraRenderState camera,
                                     List<StorageClusterDetector.Cluster> clusters) {
        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        Font font = client.font;
        for (StorageClusterDetector.Cluster cluster : clusters) {
            double dx = cluster.centerX() - client.player.getX();
            double dy = cluster.centerY() - client.player.getY();
            double dz = cluster.centerZ() - client.player.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > module.range()) continue;
            String text = "Storage cluster • " + cluster.count();
            drawLabel(matrices, consumers, font, camera, cluster.centerX(), cluster.maxY() + module.labelHeight(),
                    cluster.centerZ(), text, module.clusterColor(), 1.0f, module.labelScale());
        }
    }

    private void drawLabel(PoseStack matrices, MultiBufferSource consumers, Font font,
                           net.minecraft.client.renderer.state.CameraRenderState camera,
                           double worldX, double worldY, double worldZ, String text,
                           int argb, float fade, float scale) {
        double x = worldX - camera.pos.x, y = worldY - camera.pos.y, z = worldZ - camera.pos.z;
        matrices.pushPose();
        matrices.translate(x, y, z);
        matrices.mulPose(camera.orientation);
        matrices.scale(-.025f * scale, -.025f * scale, .025f * scale);
        int width = font.width(text);
        Matrix4f pose = matrices.last().pose();
        int background = module.labelBackground() ? fadeColor(module.labelBackgroundColor(), fade) : 0;
        font.drawInBatch(Component.literal(text), -width / 2f, 0, fadeColor(argb, fade), false,
                pose, consumers, Font.DisplayMode.SEE_THROUGH, background, 0xF000F0);
        matrices.popPose();
    }

    private boolean isVisibleType(StorageType type) {
        return switch (type) {
            case CHEST -> module.showChests();
            case BARREL -> module.showBarrels();
            case SHULKER -> module.showShulkers();
            case ENDER_CHEST -> module.showEnderChests();
            case OTHER -> module.showOtherStorage();
        };
    }

    private String labelText(StorageType type, double distance) {
        String name = switch (type) {
            case CHEST -> "Chest";
            case BARREL -> "Barrel";
            case SHULKER -> "Shulker";
            case ENDER_CHEST -> "Ender Chest";
            case OTHER -> "Storage";
        };
        return module.labelDistance() ? name + " [" + Math.round(distance) + "m]" : name;
    }

    private void syncProfile() {
        StorageRenderProfile profile = overlay.profile();
        profile.enabled(StorageType.CHEST, module.showChests());
        profile.enabled(StorageType.BARREL, module.showBarrels());
        profile.enabled(StorageType.SHULKER, module.showShulkers());
        profile.enabled(StorageType.ENDER_CHEST, module.showEnderChests());
        profile.enabled(StorageType.OTHER, module.showOtherStorage());
    }

    private static float fadeForDistance(double distance, double range) {
        double normalized = Math.max(0, Math.min(1, distance / Math.max(1, range)));
        return (float) Math.max(0, Math.min(1, 1 - Math.max(0, normalized - .45) / .55));
    }

    private static int fadeColor(int argb, float fade) {
        int alpha = Math.round(((argb >>> 24) & 255) * fade);
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }

    private static RenderBox withFade(RenderBox box, float fade) {
        RenderStyle style = box.style();
        return new RenderBox(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ(),
                new RenderStyle(style.color(), style.fill(), style.outline(), style.fillAlpha() * fade,
                        style.outlineAlpha() * fade, style.lineWidth()));
    }

    public static List<RenderBox> applyRenderMode(List<RenderBox> boxes, ModuleManager.RenderMode mode) {
        if (boxes == null || boxes.isEmpty()) return List.of();
        if (mode == null || mode == ModuleManager.RenderMode.STANDARD) return List.copyOf(boxes);
        List<RenderBox> styled = new ArrayList<>(boxes.size());
        for (RenderBox box : boxes) {
            var original = box.style();
            boolean fill = mode == ModuleManager.RenderMode.HIGH_CONTRAST && original.filled();
            boolean outline = mode == ModuleManager.RenderMode.MINIMAL || original.outline();
            float fillAlpha = mode == ModuleManager.RenderMode.HIGH_CONTRAST
                    ? Math.max(original.fillAlpha(), 0.55f) : 0.0f;
            float outlineAlpha = mode == ModuleManager.RenderMode.HIGH_CONTRAST
                    ? Math.max(original.outlineAlpha(), 0.95f) : original.outlineAlpha();
            float lineWidth = mode == ModuleManager.RenderMode.HIGH_CONTRAST
                    ? Math.max(original.lineWidth(), 2.0f) : original.lineWidth();
            RenderStyle style = new RenderStyle(original.color(), fill, outline,
                    fillAlpha, outlineAlpha, lineWidth);
            styled.add(new RenderBox(box.minX(), box.minY(), box.minZ(),
                    box.maxX(), box.maxY(), box.maxZ(), style));
        }
        return List.copyOf(styled);
    }

    public int lastVisibleCount() { return lastVisibleCount; }
}
