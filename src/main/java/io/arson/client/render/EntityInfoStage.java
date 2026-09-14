package io.arson.client.render;

import io.arson.client.module.EntityInfoModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

/** Renders configurable name, distance and health labels above entity targets. */
@Environment(EnvType.CLIENT)
public final class EntityInfoStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final EntityScanner scanner;
    private final EntityInfoModule module;
    private List<EntityTarget> cachedTargets = List.of();
    private long lastScanTick = Long.MIN_VALUE;

    public EntityInfoStage(Minecraft client, EntityScanner scanner, EntityInfoModule module) {
        this.client = client;
        this.scanner = scanner;
        this.module = module;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) return;

        long gameTime = client.level.getGameTime();
        if (gameTime - lastScanTick >= module.scanInterval() || gameTime < lastScanTick) {
            cachedTargets = scanner.scan(client, module);
            lastScanTick = gameTime;
        }
        if (cachedTargets.isEmpty()) return;

        var camera = context.worldState().cameraRenderState;
        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        Font font = client.font;
        float scale = (float) module.scale();

        for (EntityTarget target : cachedTargets) {
            String text = buildText(target);
            if (text.isEmpty()) continue;

            double x = target.centerX() - camera.pos.x;
            double y = target.maxY() - camera.pos.y + 0.35;
            double z = target.centerZ() - camera.pos.z;

            matrices.pushPose();
            matrices.translate(x, y, z);
            matrices.mulPose(camera.orientation);
            matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale);

            int width = font.width(text);
            float left = -width / 2.0f;
            int textColor = module.textColorFor(target.type());
            if (module.background()) {
                int padding = 2;
                fillQuad(matrices, consumers, left - padding, -2, left + width + padding, 9,
                        module.backgroundColor());
            }

            Matrix4f pose = matrices.last().pose();
            font.drawInBatch(Component.literal(text), left, 0, textColor, false,
                    pose, consumers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            matrices.popPose();
        }
    }

    private String buildText(EntityTarget target) {
        StringBuilder text = new StringBuilder();
        if (module.showName()) text.append(target.displayName());
        if (module.showDistance()) {
            if (!text.isEmpty()) text.append(" ");
            text.append("[").append(Math.round(target.distance())).append("m]");
        }
        if (module.showHealth() && target.maxHealth() > 0.0f) {
            if (!text.isEmpty()) text.append(" ");
            text.append("HP ").append(formatHealth(target.health()))
                    .append("/").append(formatHealth(target.maxHealth()));
        }
        return text.toString();
    }

    private static String formatHealth(float health) {
        return String.valueOf(Math.round(health * 10.0f) / 10.0f);
    }

    private static void fillQuad(PoseStack matrices, MultiBufferSource consumers,
                                 float x1, float y1, float x2, float y2, int argb) {
        var pose = matrices.last();
        var buffer = consumers.getBuffer(RenderTypes.gui());
        float a = ((argb >>> 24) & 0xFF) / 255.0f;
        float r = ((argb >>> 16) & 0xFF) / 255.0f;
        float g = ((argb >>> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        buffer.addVertex(pose, x1, y1, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, x1, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, x2, y2, 0).setColor(r, g, b, a);
        buffer.addVertex(pose, x2, y1, 0).setColor(r, g, b, a);
    }
}
