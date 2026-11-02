package io.arson.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;

/** Shared billboard text renderer with configurable, distance-faded background padding. */
public final class BillboardLabelRenderer {
    private BillboardLabelRenderer() {}

    public static void draw(Font font, PoseStack matrices, MultiBufferSource consumers,
                            String text, int textColor, int backgroundColor,
                            float padding, int packedLight) {
        int width = font.width(text);
        float safePadding = Float.isFinite(padding) ? Math.max(0.0f, Math.min(8.0f, padding)) : 0.0f;

        if (((backgroundColor >>> 24) & 0xFF) != 0) {
            drawBackground(font, matrices, consumers, width, backgroundColor, safePadding);
        }

        font.drawInBatch(Component.literal(text), -width / 2.0f, 0.0f, textColor, false,
                matrices.last().pose(), consumers, Font.DisplayMode.NORMAL, 0, packedLight);
    }

    private static void drawBackground(Font font, PoseStack matrices, MultiBufferSource consumers,
                                       int textWidth, int argb, float padding) {
        float left = -textWidth / 2.0f - padding;
        float right = textWidth / 2.0f + padding;
        float top = -padding;
        float bottom = font.lineHeight + padding;
        float z = -0.001f;
        float red = ((argb >>> 16) & 0xFF) / 255.0f;
        float green = ((argb >>> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;
        float alpha = ((argb >>> 24) & 0xFF) / 255.0f;

        PoseStack.Pose pose = matrices.last();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.debugFilledBox());
        buffer.addVertex(pose, left, top, z).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, left, bottom, z).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, right, bottom, z).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, right, top, z).setColor(red, green, blue, alpha);
    }
}
