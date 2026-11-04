package io.arson.client.render;

import com.arson.client.render.RenderBox;
import com.arson.client.render.RenderBoxRenderer;
import io.arson.client.module.ChunkFinderModule;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.ArrayList;
import java.util.List;

/** Renders scored chunk markers and optional world-space labels. */
public final class ChunkFinderRenderStage implements WorldRenderBridge.WorldRenderStage {
    private final Minecraft client;
    private final ChunkFinderScanner scanner;
    private final ChunkFinderModule module;

    public ChunkFinderRenderStage(Minecraft client, ChunkFinderScanner scanner, ChunkFinderModule module) {
        this.client = client;
        this.scanner = scanner;
        this.module = module;
    }

    @Override
    public void render(WorldRenderContext context) {
        if (!module.enabled() || client.level == null || client.player == null) {
            scanner.clear();
            return;
        }

        List<ChunkFinderScanner.ChunkResult> results = scanner.scan(client, module);
        if (results.isEmpty()) return;

        var camera = context.worldState().cameraRenderState;
        List<RenderBox> boxes = new ArrayList<>(results.size());
        for (ChunkFinderScanner.ChunkResult result : results) {
            double minX = result.chunkX() * 16.0;
            double minZ = result.chunkZ() * 16.0;
            double y = Math.max(client.level.getMinY(), Math.min(client.level.getMaxY() - 3.0, result.centerY()));
            boxes.add(new RenderBox(minX, y - 1.0, minZ, minX + 16.0, y + 2.0, minZ + 16.0,
                    module.styleForScore(result.score())));
        }
        RenderBoxRenderer.fill(context.matrices(), context.consumers(), camera.pos.x, camera.pos.y, camera.pos.z, boxes);
        RenderBoxRenderer.outline(context.matrices(), context.consumers(), camera.pos.x, camera.pos.y, camera.pos.z, boxes);

        if (module.showLabels()) renderLabels(context, camera, results);
    }

    private void renderLabels(WorldRenderContext context, net.minecraft.client.renderer.state.CameraRenderState camera,
                              List<ChunkFinderScanner.ChunkResult> results) {
        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        Font font = client.font;
        for (ChunkFinderScanner.ChunkResult result : results) {
            double x = result.chunkX() * 16.0 + 8.0 - camera.pos.x;
            double y = result.centerY() + 3.0 - camera.pos.y;
            double z = result.chunkZ() * 16.0 + 8.0 - camera.pos.z;
            String label = "Chunk " + result.chunkX() + ", " + result.chunkZ() + " | " + result.score();
            String detail = result.signals();
            matrices.pushPose();
            matrices.translate(x, y, z);
            matrices.mulPose(camera.orientation);
            matrices.scale(-0.025f, -0.025f, 0.025f);
            Matrix4f pose = matrices.last().pose();
            int width = font.width(label);
            font.drawInBatch(Component.literal(label), -width / 2.0f, 0, 0xFFFFD45C, false, pose,
                    consumers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            if (!detail.isBlank()) {
                int detailWidth = font.width(detail);
                font.drawInBatch(Component.literal(detail), -detailWidth / 2.0f, 10, 0xFFE8E8E8, false, pose,
                        consumers, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            }
            matrices.popPose();
        }
    }
}
