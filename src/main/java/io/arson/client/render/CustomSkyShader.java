package io.arson.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.arson.client.ArsonClient;
import io.arson.client.module.CustomSkyModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/** GPU-backed sky dome using Arson's registered core shader. */
public final class CustomSkyShader {
    private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("arson", "pipeline/custom_sky"))
                    .withVertexShader("arson:core/custom_sky")
                    .withFragmentShader("arson:core/custom_sky")
                    .withDepthWrite(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLE_FAN)
                    .build()
    );
    private static GpuBuffer domeBuffer;

    private CustomSkyShader() {}

    /**
     * @return true when the custom shader drew the sky, false when vanilla should draw it.
     */
    public static boolean render(int vanillaSkyColor) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || ArsonClient.getInstance() == null) return false;
        if (!(ArsonClient.getInstance().modules().get("custom-sky") instanceof CustomSkyModule module)
                || !module.enabled() || !module.customSkyEnabled()) return false;

        GpuBuffer buffer = domeBuffer();
        if (buffer == null) return false;

        int tint = module.skyTintArgb();
        int sky = ARGB.multiply(vanillaSkyColor, tint);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(), ARGB.vector4fFromARGB32(sky),
                new Vector3f(), new Matrix4f());
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Arson custom sky shader",
                client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
                client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, buffer);
            pass.draw(0, 66);
        }
        return true;
    }

    private static GpuBuffer domeBuffer() {
        if (domeBuffer != null) return domeBuffer;
        int segments = 64;
        int rings = 4;
        int count = 2 + segments * rings;
        try (ByteBufferBuilder allocator = ByteBufferBuilder.exactlySized(count * DefaultVertexFormat.POSITION.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            builder.addVertex(0.0F, 512.0F, 0.0F);
            for (int ring = 1; ring <= rings; ring++) {
                float theta = (float) (Math.PI * 0.5 * ring / rings);
                float radius = (float) (512.0 * Math.cos(theta));
                float y = (float) (512.0 * Math.sin(theta));
                for (int i = 0; i <= segments; i++) {
                    float angle = (float) (Math.PI * 2.0 * i / segments);
                    builder.addVertex((float) (Math.cos(angle) * radius), y, (float) (Math.sin(angle) * radius));
                }
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                domeBuffer = RenderSystem.getDevice().createBuffer(() -> "Arson custom sky dome", 32, mesh.vertexBuffer());
            }
        } catch (RuntimeException exception) {
            return null;
        }
        return domeBuffer;
    }
}
