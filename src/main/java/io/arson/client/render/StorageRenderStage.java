package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import io.arson.client.module.ContainerESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
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
        if (gameTime - lastScanTick >= module.scanInterval() || gameTime < lastScanTick) {
            cachedTargets = scanner.scan(client, overlay.range());
            lastScanTick = gameTime;
        }

        var camera = context.worldState().cameraRenderState.pos;
        List<RenderBox> boxes = overlay.build(camera.x, camera.y, camera.z, cachedTargets, type -> switch (type) {
            case CHEST -> module.chestStyle();
            case BARREL -> module.barrelStyle();
            case SHULKER -> module.shulkerStyle();
            case ENDER_CHEST -> module.enderChestStyle();
            case OTHER -> module.otherStorageStyle();
        });

        if (module.distanceFade() && !boxes.isEmpty()) {
            List<RenderBox> faded = new ArrayList<>(boxes.size());
            for (RenderBox box : boxes) {
                double dx = box.centerX() - camera.x;
                double dy = box.centerY() - camera.y;
                double dz = box.centerZ() - camera.z;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                float fade = fadeForDistance(distance, module.range());
                if (fade <= 0.0f) continue;
                faded.add(withFade(box, fade));
            }
            boxes = faded;
        }

        lastVisibleCount = boxes.size();
        if (boxes.isEmpty()) return;

        RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
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
        double normalized = Math.max(0.0, Math.min(1.0, distance / Math.max(1.0, range)));
        return (float) Math.max(0.0, Math.min(1.0,
                1.0 - Math.max(0.0, normalized - 0.45) / 0.55));
    }

    private static RenderBox withFade(RenderBox box, float fade) {
        RenderStyle style = box.style();
        RenderStyle faded = new RenderStyle(
                style.color(), style.fill(), style.outline(),
                style.fillAlpha() * fade,
                style.outlineAlpha() * fade,
                style.lineWidth());
        return new RenderBox(box.minX(), box.minY(), box.minZ(),
                box.maxX(), box.maxY(), box.maxZ(), faded);
    }

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}
