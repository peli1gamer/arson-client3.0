package io.arson.client.render;

import io.arson.client.module.WorldEffectsModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Lightweight line-based renderer for local movement feedback. */
@Environment(EnvType.CLIENT)
public final class WorldEffectsRenderStage implements WorldRenderBridge.WorldRenderStage {
    private static final int SEGMENTS = 40;
    private final Minecraft client;
    private final WorldEffectsModule module;

    public WorldEffectsRenderStage(Minecraft client, WorldEffectsModule module) {
        this.client = client;
        this.module = module;
    }

    @Override public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) return;
        var camera = context.worldState().cameraRenderState.pos;
        MultiBufferSource consumers = context.consumers();
        if (consumers == null) return;
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());
        PoseStack matrices = context.matrices();
        int argb = module.colorArgb();
        float red = ((argb >>> 16) & 255) / 255.0f;
        float green = ((argb >>> 8) & 255) / 255.0f;
        float blue = (argb & 255) / 255.0f;
        int maxAge = Math.max(1, module.lifetimeTicks());

        for (WorldEffectsModule.Effect effect : module.effects()) {
            float life = Math.max(0.0f, Math.min(1.0f, 1.0f - effect.age() / (float) maxAge));
            if (life <= 0.0f) continue;
            float alpha = life * (effect.type() == WorldEffectsModule.EffectType.LANDING_RIPPLE ? 0.9f : 0.65f);
            if (effect.type() == WorldEffectsModule.EffectType.LANDING_RIPPLE) {
                float progress = Math.min(1.0f, effect.age() / (float) maxAge);
                float radius = (float) (0.15 + module.rippleRadius() * progress);
                ring(buffer, matrices, effect.x() - camera.x, effect.y() - camera.y, effect.z() - camera.z,
                        radius, red, green, blue, alpha);
            } else {
                float progress = effect.age() / (float) maxAge;
                float radius = 0.08f + 0.12f * progress;
                ring(buffer, matrices, effect.x() - camera.x, effect.y() - camera.y, effect.z() - camera.z,
                        radius, red, green, blue, alpha * effect.strength());
            }
        }
    }

    private static void ring(VertexConsumer buffer, PoseStack matrices, double x, double y, double z,
                             float radius, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = matrices.last();
        float px = (float) x, py = (float) y, pz = (float) z;
        for (int i = 0; i < SEGMENTS; i++) {
            double a0 = Math.PI * 2.0 * i / SEGMENTS;
            double a1 = Math.PI * 2.0 * (i + 1) / SEGMENTS;
            float x0 = px + (float) Math.cos(a0) * radius;
            float z0 = pz + (float) Math.sin(a0) * radius;
            float x1 = px + (float) Math.cos(a1) * radius;
            float z1 = pz + (float) Math.sin(a1) * radius;
            buffer.addVertex(pose, x0, py, z0).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 1.0f, 0.0f).setLineWidth(1.6f);
            buffer.addVertex(pose, x1, py, z1).setColor(red, green, blue, alpha).setNormal(pose, 0.0f, 1.0f, 0.0f).setLineWidth(1.6f);
        }
    }
}
