package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import com.arson.client.render.RenderStyle;
import io.arson.client.module.ItemESPModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;

/** World-render stage for dropped-item ESP boxes and labels. */
@Environment(EnvType.CLIENT)
public final class ItemRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final EntityScanner scanner;
    private final ItemESPModule module;
    private List<EntityTarget> cachedTargets = List.of();
    private long lastScanTick = Long.MIN_VALUE;

    public ItemRenderStage(Minecraft client, EntityScanner scanner, ItemESPModule module) {
        this.client = client;
        this.scanner = scanner;
        this.module = module;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) return;

        long gameTime = client.level.getGameTime();
        if (gameTime - lastScanTick >= module.scanInterval() || gameTime < lastScanTick) {
            cachedTargets = scanItems();
            lastScanTick = gameTime;
        }
        if (cachedTargets.isEmpty()) return;

        var camera = context.worldState().cameraRenderState.pos;
        List<RenderBox> boxes = new ArrayList<>(cachedTargets.size());
        for (EntityTarget target : cachedTargets) {
            float fade = distanceFade(target.distance());
            if (fade <= 0.0f) continue;
            boxes.add(new RenderBox(target.minX(), target.minY(), target.minZ(),
                    target.maxX(), target.maxY(), target.maxZ(),
                    fadeStyle(module.renderStyle(), fade)));
        }

        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.x, camera.y, camera.z, boxes);
        }
        if (module.showLabels()) {
            renderLabels(context, cachedTargets, camera.x, camera.y, camera.z);
        }
    }

    private List<EntityTarget> scanItems() {
        ItemScanConfig config = new ItemScanConfig(module);
        return scanner.scan(client, config);
    }

    private void renderLabels(WorldRenderContext context, List<EntityTarget> targets,
                              double cameraX, double cameraY, double cameraZ) {
        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        Font font = client.font;

        for (EntityTarget target : targets) {
            float fade = distanceFade(target.distance());
            if (fade <= 0.0f) continue;

            String name = target.displayName();
            String text = module.showCount() && target.itemCount() > 1
                    ? name + " x" + target.itemCount() : name;
            if (module.labelDistance()) {
                text += " [" + formatDistance(target.distance()) + "]";
            }

            int textColor = fadeAlpha(module.labelColor(), fade);
            int backgroundColor = fadeAlpha(module.labelBackgroundColor(), fade);
            float scale = module.labelScale();
            float y = (float) (target.maxY() - cameraY + module.labelHeight());

            matrices.pushPose();
            matrices.translate(target.centerX() - cameraX, y, target.centerZ() - cameraZ);
            matrices.mulPose(context.worldState().cameraRenderState.orientation);
            matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale);

            int width = font.width(text);
            float left = -width * 0.5f;
            float padding = module.labelPadding();
            if (module.labelBackground()) {
                fillQuad(matrices, consumers, left - padding, -padding,
                        left + width + padding, font.lineHeight + padding, backgroundColor);
            }
            font.drawInBatch(Component.literal(text), left, 0, textColor, false,
                    matrices.last().pose(), consumers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            matrices.popPose();
        }
    }

    private float distanceFade(double distance) {
        if (!module.distanceFade()) return 1.0f;
        double range = Math.max(1.0, module.range());
        double normalized = Math.max(0.0, Math.min(1.0, distance / range));
        return (float) Math.max(0.0, Math.min(1.0, 1.0 - Math.max(0.0, normalized - 0.45) / 0.55));
    }

    private static RenderStyle fadeStyle(RenderStyle style, float fade) {
        return new RenderStyle(style.color(), style.fill(), style.outline(),
                style.fillAlpha() * fade, style.outlineAlpha() * fade, style.lineWidth());
    }

    private static int fadeAlpha(int argb, float fade) {
        int alpha = Math.round(((argb >>> 24) & 0xFF) * fade);
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }

    private static String formatDistance(double distance) {
        return String.format(java.util.Locale.ROOT, "%.1fm", distance);
    }

    private static void fillQuad(PoseStack matrices, MultiBufferSource consumers,
                                 float left, float top, float right, float bottom, int argb) {
        var pose = matrices.last();
        var buffer = consumers.getBuffer(RenderTypes.gui());
        float r = ((argb >>> 16) & 0xFF) / 255.0f;
        float g = ((argb >>> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        float a = ((argb >>> 24) & 0xFF) / 255.0f;
        buffer.addVertex(pose, left, top, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, left, bottom, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, right, bottom, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, right, top, 0).setColor(r, g, b, a);
    }

    private static final class ItemScanConfig implements EntityScanConfig {
        private final ItemESPModule module;

        private ItemScanConfig(ItemESPModule module) {
            this.module = module;
        }

        @Override public boolean enabled() { return module.enabled(); }
        @Override public boolean showPlayers() { return false; }
        @Override public boolean showMobs() { return false; }
        @Override public boolean showAnimals() { return false; }
        @Override public boolean showItems() { return true; }
        @Override public double range() { return module.range(); }
        @Override public int playerColor() { return module.colorArgb(); }
        @Override public int mobColor() { return module.colorArgb(); }
        @Override public int animalColor() { return module.colorArgb(); }
        @Override public int itemColor() { return module.colorArgb(); }
    }
}
