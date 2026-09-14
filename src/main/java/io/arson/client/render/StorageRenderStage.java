package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderColor;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.StorageOverlay;
import com.arson.client.render.StorageType;
import io.arson.client.module.ContainerESPModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;

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
            RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z,
                    boxes, module.fillAlpha());
        }
        if (module.outline()) {
            RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z,
                    boxes, module.outlineAlpha(), module.lineWidth());
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

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}
