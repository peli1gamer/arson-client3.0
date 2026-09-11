package io.arson.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;

import java.util.List;

/** Render-stage adapter for the container overlay. Detection remains isolated from drawing. */
public final class StorageRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final StorageOverlay overlay;
    private final StorageScanner scanner;
    private final StorageRenderProfile profile;

    public StorageRenderStage(Minecraft client, StorageOverlay overlay,
                              StorageScanner scanner, StorageRenderProfile profile) {
        this.client = client;
        this.overlay = overlay;
        this.scanner = scanner;
        this.profile = profile;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (client.level == null || client.player == null) return;

        List<StorageOverlay.StorageTarget> targets = scanner.scan(client, overlay.range());
        double cameraX = context.camera().position().x();
        double cameraY = context.camera().position().y();
        double cameraZ = context.camera().position().z();
        List<RenderBox> boxes = overlay.build(cameraX, cameraY, cameraZ, targets);

        // Geometry is intentionally produced here but not drawn yet. The next renderer
        // stage consumes these commands, keeping detection and Minecraft drawing separate.
        profile.lastVisibleCount(boxes.size());
    }
}
