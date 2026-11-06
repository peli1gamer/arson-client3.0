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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.List;

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
        if (StorageScanner.isRefreshDue(gameTime, lastScanTick, module.scanInterval())) {
            cachedTargets = scanner.scan(client, overlay.range(), module.scanInterval());
            lastScanTick = gameTime;
        }

        var camera = context.worldState().cameraRenderState.pos;
        List<RenderBox> boxes = overlay.build(
                client.player.getX(), client.player.getY(), client.player.getZ(), cachedTargets,
                type -> switch (type) {
                    case CHEST -> module.chestStyle();
                    case BARREL -> module.barrelStyle();
                    case SHULKER -> module.shulkerStyle();
                    case ENDER_CHEST -> module.enderChestStyle();
                    case OTHER -> module.otherStorageStyle();
                });

        if (module.distanceFade() && !boxes.isEmpty()) {
            List<RenderBox> faded = new ArrayList<>(boxes.size());
            for (RenderBox box : boxes) {
                double dx = box.centerX() - client.player.getX();
                double dy = box.centerY() - client.player.getY();
                double dz = box.centerZ() - client.player.getZ();
                float fade = fadeForDistance(Math.sqrt(dx * dx + dy * dy + dz * dz), module.range());
                if (fade > 0) faded.add(withFade(box, fade));
            }
            boxes = faded;
        }

        lastVisibleCount = boxes.size();
        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(),
                    camera.x, camera.y, camera.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(),
                    camera.x, camera.y, camera.z, boxes);
        }
        if (module.showLabels() && !cachedTargets.isEmpty()) {
            renderLabels(context, context.worldState().cameraRenderState, cachedTargets);
        }
    }

    private void renderLabels(WorldRenderContext context,
                              net.minecraft.client.renderer.state.CameraRenderState camera,
                              List<StorageOverlay.StorageTarget> targets) {
        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        Font font = client.font;
        float scale = module.labelScale();

        for (StorageOverlay.StorageTarget target : targets) {
            if (!isVisibleType(target.type())) continue;

            double dx = target.x() + target.width() * 0.5 - client.player.getX();
            double dy = target.y() + target.height() * 0.5 - client.player.getY();
            double dz = target.z() + target.depth() * 0.5 - client.player.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > module.range()) continue;

            float fade = module.distanceFade() ? fadeForDistance(distance, module.range()) : 1.0f;
            if (fade <= 0) continue;

            String text = labelText(target.type(), distance);
            double x = target.x() + target.width() * 0.5 - camera.pos.x;
            double y = target.y() + target.height() + module.labelHeight() - camera.pos.y;
            double z = target.z() + target.depth() * 0.5 - camera.pos.z;

            matrices.pushPose();
            try {
                matrices.translate(x, y, z);
                matrices.mulPose(camera.orientation);
                matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale);

                int width = font.width(text);
                PoseStack.Pose pose = matrices.last();
                if (module.labelBackground()) {
                    drawLabelBackground(consumers, pose, width, font.lineHeight,
                            module.labelPadding(), fadeColor(module.labelBackgroundColor(), fade));
                }
                font.drawInBatch(Component.literal(text), -width / 2.0f, 0,
                        fadeColor(module.labelColor(), fade), false, matrices.last().pose(),
                        consumers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            } finally {
                matrices.popPose();
            }
        }
    }

    private static void drawLabelBackground(MultiBufferSource consumers, PoseStack.Pose pose,
                                            int textWidth, int textHeight, float padding, int argb) {
        float left = -textWidth / 2.0f - padding;
        float right = textWidth / 2.0f + padding;
        float top = -padding;
        float bottom = textHeight + padding;
        float red = ((argb >>> 16) & 0xFF) / 255.0f;
        float green = ((argb >>> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;
        float alpha = ((argb >>> 24) & 0xFF) / 255.0f;
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.debugFilledBox());
        buffer.addVertex(pose, left, top, -0.01f).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, left, bottom, -0.01f).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, right, bottom, -0.01f).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, right, top, -0.01f).setColor(red, green, blue, alpha);
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
        return (float) Math.max(0, Math.min(1, 1 - Math.max(0, normalized - 0.45) / 0.55));
    }

    private static int fadeColor(int argb, float fade) {
        int alpha = Math.round(((argb >>> 24) & 255) * fade);
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }

    private static RenderBox withFade(RenderBox box, float fade) {
        RenderStyle style = box.style();
        return new RenderBox(box.minX(), box.minY(), box.minZ(),
                box.maxX(), box.maxY(), box.maxZ(),
                new RenderStyle(style.color(), style.fill(), style.outline(),
                        style.fillAlpha() * fade, style.outlineAlpha() * fade, style.lineWidth()));
    }

    public int lastVisibleCount() {
        return lastVisibleCount;
    }
}