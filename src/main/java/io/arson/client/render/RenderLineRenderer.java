package io.arson.client.render;

import com.arson.client.render.RenderStyle;
import com.arson.client.render.RenderStyleUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Shared world-space line renderer used by tracer-style overlays. */
public final class RenderLineRenderer {
    private RenderLineRenderer() {
    }

    public static void line(PoseStack matrices, VertexConsumer buffer,
                            double startX, double startY, double startZ,
                            double endX, double endY, double endZ,
                            RenderStyle style) {
        float[] rgba = RenderStyleUtil.rgba(style, true);
        float dx = (float) (endX - startX);
        float dy = (float) (endY - startY);
        float dz = (float) (endZ - startZ);
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.001f) return;

        float nx = dx / length;
        float ny = dy / length;
        float nz = dz / length;
        PoseStack.Pose pose = matrices.last();

        buffer.addVertex(pose, (float) startX, (float) startY, (float) startZ)
                .setColor(rgba[0], rgba[1], rgba[2], rgba[3])
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(style.lineWidth());
        buffer.addVertex(pose, (float) endX, (float) endY, (float) endZ)
                .setColor(rgba[0], rgba[1], rgba[2], rgba[3])
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(style.lineWidth());
    }

    public static VertexConsumer lines(VertexConsumer consumers) {
        return consumers;
    }

    public static VertexConsumer lineBuffer(net.minecraft.client.renderer.MultiBufferSource consumers) {
        return consumers.getBuffer(RenderTypes.lines());
    }
}
