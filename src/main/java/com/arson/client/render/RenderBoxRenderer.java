package com.arson.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.util.List;

/** Shared world-space box drawing used by entity and storage overlays. */
public final class RenderBoxRenderer {
    private RenderBoxRenderer() {}

    public static void fill(PoseStack matrices, MultiBufferSource consumers,
                            double cameraX, double cameraY, double cameraZ,
                            List<RenderBox> boxes) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.debugFilledBox());
        for (RenderBox box : boxes) {
            RenderStyle style = box.style();
            if (!style.filled()) continue;
            drawFaces(buffer, pose, box, cameraX, cameraY, cameraZ,
                    RenderStyleUtil.rgba(style, false));
        }
    }

    public static void outline(PoseStack matrices, MultiBufferSource consumers,
                               double cameraX, double cameraY, double cameraZ,
                               List<RenderBox> boxes) {
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());
        for (RenderBox box : boxes) {
            RenderStyle style = box.style();
            if (!style.outline()) continue;
            float[] color = RenderStyleUtil.rgba(style, true);
            float width = Math.max(0.5f, Math.min(8.0f, style.lineWidth()));
            drawLine(buffer, pose, box.minX() - cameraX, box.minY() - cameraY, box.minZ() - cameraZ,
                    box.maxX() - cameraX, box.minY() - cameraY, box.minZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.maxX() - cameraX, box.minY() - cameraY, box.minZ() - cameraZ,
                    box.maxX() - cameraX, box.minY() - cameraY, box.maxZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.maxX() - cameraX, box.minY() - cameraY, box.maxZ() - cameraZ,
                    box.minX() - cameraX, box.minY() - cameraY, box.maxZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.minX() - cameraX, box.minY() - cameraY, box.maxZ() - cameraZ,
                    box.minX() - cameraX, box.minY() - cameraY, box.minZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.minX() - cameraX, box.maxY() - cameraY, box.minZ() - cameraZ,
                    box.maxX() - cameraX, box.maxY() - cameraY, box.minZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.maxX() - cameraX, box.maxY() - cameraY, box.minZ() - cameraZ,
                    box.maxX() - cameraX, box.maxY() - cameraY, box.maxZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.maxX() - cameraX, box.maxY() - cameraY, box.maxZ() - cameraZ,
                    box.minX() - cameraX, box.maxY() - cameraY, box.maxZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.minX() - cameraX, box.maxY() - cameraY, box.maxZ() - cameraZ,
                    box.minX() - cameraX, box.maxY() - cameraY, box.minZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.minX() - cameraX, box.minY() - cameraY, box.minZ() - cameraZ,
                    box.minX() - cameraX, box.maxY() - cameraY, box.minZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.maxX() - cameraX, box.minY() - cameraY, box.minZ() - cameraZ,
                    box.maxX() - cameraX, box.maxY() - cameraY, box.minZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.maxX() - cameraX, box.minY() - cameraY, box.maxZ() - cameraZ,
                    box.maxX() - cameraX, box.maxY() - cameraY, box.maxZ() - cameraZ, color, width);
            drawLine(buffer, pose, box.minX() - cameraX, box.minY() - cameraY, box.maxZ() - cameraZ,
                    box.minX() - cameraX, box.maxY() - cameraY, box.maxZ() - cameraZ, color, width);
        }
    }

    /** Draws a direct camera-to-target line for every rendered target. */
    public static void tracers(PoseStack matrices, MultiBufferSource consumers,
                               double cameraX, double cameraY, double cameraZ,
                               List<RenderBox> boxes) {
        if (boxes == null || boxes.isEmpty()) return;
        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());
        for (RenderBox box : boxes) {
            RenderStyle style = box.style();
            float[] color = RenderStyleUtil.rgba(style, true);
            float width = Math.max(0.5f, Math.min(8.0f, style.lineWidth()));
            drawLine(buffer, pose, 0, 0, 0,
                    (box.minX() + box.maxX()) * 0.5 - cameraX,
                    (box.minY() + box.maxY()) * 0.5 - cameraY,
                    (box.minZ() + box.maxZ()) * 0.5 - cameraZ, color, width);
        }
    }

    private static void drawFaces(VertexConsumer buffer, PoseStack.Pose pose, RenderBox box,
                                  double cameraX, double cameraY, double cameraZ, float[] color) {
        float minX = (float) (box.minX() - cameraX), minY = (float) (box.minY() - cameraY), minZ = (float) (box.minZ() - cameraZ);
        float maxX = (float) (box.maxX() - cameraX), maxY = (float) (box.maxY() - cameraY), maxZ = (float) (box.maxZ() - cameraZ);
        quad(buffer, pose, minX, minY, minZ, maxX, minY, maxZ, color);
        quad(buffer, pose, minX, maxY, minZ, maxX, maxY, maxZ, color);
        quad(buffer, pose, minX, minY, minZ, maxX, maxY, minZ, color);
        quad(buffer, pose, minX, minY, maxZ, maxX, maxY, maxZ, color);
        quad(buffer, pose, minX, minY, minZ, minX, maxY, maxZ, color);
        quad(buffer, pose, maxX, minY, minZ, maxX, maxY, maxZ, color);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2, float[] color) {
        if (x1 == x2) {
            vertex(buffer, pose, x1, y1, z1, color); vertex(buffer, pose, x1, y2, z1, color);
            vertex(buffer, pose, x1, y2, z2, color); vertex(buffer, pose, x1, y1, z2, color);
        } else if (y1 == y2) {
            vertex(buffer, pose, x1, y1, z1, color); vertex(buffer, pose, x2, y1, z1, color);
            vertex(buffer, pose, x2, y1, z2, color); vertex(buffer, pose, x1, y1, z2, color);
        } else {
            vertex(buffer, pose, x1, y1, z1, color); vertex(buffer, pose, x2, y1, z1, color);
            vertex(buffer, pose, x2, y2, z1, color); vertex(buffer, pose, x1, y2, z1, color);
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                               float x, float y, float z, float[] color) {
        buffer.addVertex(pose, x, y, z).setColor(color[0], color[1], color[2], color[3]);
    }

    private static void drawLine(VertexConsumer buffer, PoseStack.Pose pose,
                                 double x1, double y1, double z1, double x2, double y2, double z2,
                                 float[] color, float lineWidth) {
        float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0e-5f) return;
        dx /= length; dy /= length; dz /= length;
        buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(color[0], color[1], color[2], color[3])
                .setNormal(pose, dx, dy, dz).setLineWidth(lineWidth);
        buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(color[0], color[1], color[2], color[3])
                .setNormal(pose, dx, dy, dz).setLineWidth(lineWidth);
    }
}
