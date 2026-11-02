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
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;

/** Dedicated render stage for dropped-item boxes and billboard labels. */
@Environment(EnvType.CLIENT)
public final class ItemRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final EntityScanner scanner;
    private final ItemESPModule module;
    private List<EntityTarget> cachedTargets = List.of();
    private long lastScanTick = Long.MIN_VALUE;
    private Object cachedLevel;
    private Object cachedPlayer;

    public ItemRenderStage(Minecraft client, EntityScanner scanner, ItemESPModule module) {
        this.client = client;
        this.scanner = scanner;
        this.module = module;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) {
            cachedTargets = List.of();
            cachedLevel = null;
            cachedPlayer = null;
            lastScanTick = Long.MIN_VALUE;
            return;
        }

        long gameTime = client.level.getGameTime();
        if (cachedLevel != client.level || cachedPlayer != client.player || gameTime - lastScanTick >= module.scanInterval()
                || gameTime < lastScanTick) {
            cachedTargets = scanItems();
            cachedLevel = client.level;
            cachedPlayer = client.player;
            lastScanTick = gameTime;
        }
        if (cachedTargets.isEmpty()) return;

        var camera = context.worldState().cameraRenderState.pos;
        List<RenderBox> boxes = new ArrayList<>(cachedTargets.size());
        for (EntityTarget target : cachedTargets) {
            float fade = distanceFade(target.distance());
            if (fade > 0.0f) {
                boxes.add(new RenderBox(target.minX(), target.minY(), target.minZ(),
                        target.maxX(), target.maxY(), target.maxZ(),
                        fadeStyle(module.renderStyle(), fade)));
            }
        }

        if (!boxes.isEmpty()) {
            RenderBoxRenderer.fill(context.matrices(), context.consumers(),
                    camera.x, camera.y, camera.z, boxes);
            RenderBoxRenderer.outline(context.matrices(), context.consumers(),
                    camera.x, camera.y, camera.z, boxes);
        }
        if (module.showLabels()) {
            renderLabels(context, cachedTargets, camera.x, camera.y, camera.z);
        }
    }

    private List<EntityTarget> scanItems() {
        return scanner.scan(client, new ItemScanConfig(module));
    }

    private void renderLabels(WorldRenderContext context, List<EntityTarget> targets,
                              double cameraX, double cameraY, double cameraZ) {
        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        Font font = client.font;

        for (EntityTarget target : targets) {
            float fade = distanceFade(target.distance());
            if (fade <= 0.0f) continue;

            String label = module.showCount() && target.itemCount() > 1
                    ? target.displayName() + " x" + target.itemCount()
                    : target.displayName();
            if (module.labelDistance()) label += " [" + formatDistance(target.distance()) + "]";

            matrices.pushPose();
            try {
                matrices.translate(target.centerX() - cameraX,
                        target.maxY() - cameraY + module.labelHeight(),
                        target.centerZ() - cameraZ);
                matrices.mulPose(context.worldState().cameraRenderState.orientation);
                float scale = module.labelScale();
                matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale);

                int textColor = EspRenderMath.fadeAlpha(module.labelColor(), fade);
                int backgroundColor = module.labelBackground()
                        ? EspRenderMath.fadeAlpha(module.labelBackgroundColor(), fade) : 0;
                BillboardLabelRenderer.draw(font, matrices, consumers, label,
                        textColor, backgroundColor, module.labelPadding(), 0xF000F0);
            } finally {
                matrices.popPose();
            }
        }
    }

    private float distanceFade(double distance) {
        if (!module.distanceFade()) return 1.0f;
        return EspRenderMath.distanceFade(distance, module.range(), module.fadeStart());
    }

    private static RenderStyle fadeStyle(RenderStyle style, float fade) {
        return EspRenderMath.fadeStyle(style, fade);
    }

    private static String formatDistance(double distance) {
        return String.format(java.util.Locale.ROOT, "%.1fm", distance);
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
