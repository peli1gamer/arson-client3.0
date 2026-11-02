package io.arson.client.render;

import io.arson.client.module.EntityInfoModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;

/** Renders configurable billboard labels for the shared entity snapshot. */
@Environment(EnvType.CLIENT)
public final class EntityInfoStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final EntityScanner scanner;
    private final EntityInfoModule module;
    private List<EntityTarget> cachedTargets = List.of();
    private long lastScanTick = Long.MIN_VALUE;
    private Object cachedLevel;
    private Object cachedPlayer;

    public EntityInfoStage(Minecraft client, EntityScanner scanner, EntityInfoModule module) {
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
            cachedTargets = scanner.scan(client, module);
            cachedLevel = client.level;
            cachedPlayer = client.player;
            lastScanTick = gameTime;
        }
        if (cachedTargets.isEmpty()) return;

        var camera = context.worldState().cameraRenderState;
        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        Font font = client.font;
        float scale = (float) module.scale();

        for (EntityTarget target : cachedTargets) {
            float fade = distanceFade(target);
            if (fade <= 0.0f) continue;

            String text = buildText(target);
            if (text.isEmpty()) continue;

            matrices.pushPose();
            try {
                matrices.translate(target.centerX() - camera.pos.x,
                        target.maxY() - camera.pos.y + module.heightOffset(),
                        target.centerZ() - camera.pos.z);
                matrices.mulPose(camera.orientation);
                matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale);

                int width = font.width(text);
                int textColor = EspRenderMath.fadeAlpha(module.textColorFor(target.type()), fade);
                int backgroundColor = module.background()
                        ? EspRenderMath.fadeAlpha(module.backgroundColor(), fade) : 0;
                font.drawInBatch(Component.literal(text), -width / 2.0f, 0, textColor, false,
                        matrices.last().pose(), consumers, Font.DisplayMode.NORMAL,
                        backgroundColor, 0xF000F0);
            } finally {
                matrices.popPose();
            }
        }
    }

    private String buildText(EntityTarget target) {
        StringBuilder text = new StringBuilder();
        if (module.showName()) text.append(target.displayName());
        if (module.showDistance()) append(text, "[" + Math.round(target.distance()) + "m]");
        if (module.showHealth() && target.maxHealth() > 0.0f) {
            append(text, "HP " + Math.round(Math.max(0.0f,
                    Math.min(1.0f, target.health() / target.maxHealth())) * 100.0f) + "%");
        }
        return text.toString();
    }

    private static void append(StringBuilder text, String value) {
        if (!text.isEmpty()) text.append(' ');
        text.append(value);
    }

    private float distanceFade(EntityTarget target) {
        if (!module.distanceFade()) return 1.0f;
        return EspRenderMath.distanceFade(target.distance(), module.range(), module.fadeStart());
    }
}
