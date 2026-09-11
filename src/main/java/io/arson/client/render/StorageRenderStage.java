package io.arson.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;

import java.util.List;

/** Render-stage adapter for the container overlay. Detection remains isolated from drawing. */
public final class StorageRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final StorageOverlay overlay;
    private final StorageScanner scanner;
    private int lastVisibleCount;

    public StorageRenderStage(Minecraft client, StorageOverlay overlay, StorageScanner scanner) {
        this.client = client;
        this.overlay = overlay;
        this.scanner = scanner;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (client.level == null || client.player == null) {
            lastVisibleCount = 0;
            return;
        }

        List<StorageOverlay.StorageTarget> targets = scanner.scan(client, overlay.range());
        double cameraX = context.camera().position().x();
        double cameraY = context.camera().position().y();
        double cameraZ = context.camera().position().z();
        lastVisibleCount = overlay.build(cameraX, cameraY, cameraZ, targets).size();
    }

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}
