package io.arson.client.render;

import com.arson.client.render.RenderStyle;
import com.arson.client.render.RenderStyleUtil;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

/** Shared world-space line renderer used by tracer-style overlays. */
public final class RenderLineRenderer {
    private RenderLineRenderer() {
    }

    public static void line(MatrixStack matrices, VertexConsumer buffer,
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
        MatrixStack.Entry pose = matrices.peek();

        buffer.vertex(pose, (float) startX, (float) startY, (float) startZ)
                .color(rgba[0], rgba[1], rgba[2], rgba[3])
                .normal(pose, nx, ny, nz)
                .lineWidth(style.lineWidth());
        buffer.vertex(pose, (float) endX, (float) endY, (float) endZ)
                .color(rgba[0], rgba[1], rgba[2], rgba[3])
                .normal(pose, nx, ny, nz)
                .lineWidth(style.lineWidth());
    }

    public static VertexConsumer lines(VertexConsumer consumers) {
        return consumers;
    }

    public static VertexConsumer lineBuffer(net.minecraft.client.render.VertexConsumerProvider consumers) {
        return consumers.getBuffer(RenderLayers.lines());
    }
}
