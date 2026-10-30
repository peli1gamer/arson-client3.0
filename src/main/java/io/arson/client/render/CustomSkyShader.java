package io.arson.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
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
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/** GPU-backed gradient dome and equirectangular sky-texture renderer. */
public final class CustomSkyShader {
    private static final RenderPipeline GRADIENT_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("arson", "pipeline/custom_sky"))
                    .withVertexShader("arson:core/custom_sky")
                    .withFragmentShader("arson:core/custom_sky")
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLE_FAN)
                    .build()
    );
    private static final RenderPipeline TEXTURE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("arson", "pipeline/custom_sky_texture"))
                    .withVertexShader("arson:core/custom_sky_texture")
                    .withFragmentShader("arson:core/custom_sky_texture")
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                    .withSampler("Sampler0")
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES)
                    .build()
    );
    private static final int LONGITUDE_SEGMENTS = 64;
    private static final int LATITUDE_SEGMENTS = 32;
    private static GpuBuffer gradientDomeBuffer;
    private static GpuBuffer textureDomeBuffer;

    private CustomSkyShader() {}

    /** @return true when the custom renderer drew the sky, false when vanilla should draw it. */
    public static boolean render(int vanillaSkyColor) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || ArsonClient.getInstance() == null) return false;
        if (!(ArsonClient.getInstance().modules().get("custom-sky") instanceof CustomSkyModule module)
                || !module.enabled() || !module.customSkyEnabled()) return false;

        try {
            if (module.skyMode() == CustomSkyModule.SkyMode.GRADIENT) {
                return renderGradient(client, module, vanillaSkyColor);
            }
            return renderTexture(client, module);
        } catch (RuntimeException exception) {
            // A failed custom pass must never prevent vanilla from rendering the sky.
            return false;
        }
    }

    private static boolean renderGradient(Minecraft client, CustomSkyModule module, int vanillaSkyColor) {
        GpuBuffer buffer = gradientDomeBuffer();
        if (buffer == null) return false;
        int sky = ARGB.multiply(vanillaSkyColor, module.skyTintArgb());
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(), ARGB.vector4fFromARGB32(sky),
                new Vector3f(), new Matrix4f());
        try (RenderPass pass = createPass(client, "Arson custom sky gradient")) {
            pass.setPipeline(GRADIENT_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, buffer);
            pass.draw(0, 66);
        }
        return true;
    }

    private static boolean renderTexture(Minecraft client, CustomSkyModule module) {
        Identifier textureId = resolveTextureId(module);
        if (textureId == null || client.getResourceManager().getResource(textureId).isEmpty()) return false;
        AbstractTexture texture = client.getTextureManager().getTexture(textureId);
        GpuBuffer buffer = textureDomeBuffer();
        if (buffer == null) return false;

        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(), ARGB.vector4fFromARGB32(module.skyTintArgb()),
                new Vector3f(), new Matrix4f());
        try (RenderPass pass = createPass(client, "Arson custom sky texture")) {
            pass.setPipeline(TEXTURE_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
            pass.setVertexBuffer(0, buffer);
            pass.draw(0, LATITUDE_SEGMENTS * LONGITUDE_SEGMENTS * 6);
        }
        return true;
    }

    private static RenderPass createPass(Minecraft client, String name) {
        return RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> name,
                client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
                client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty());
    }

    private static Identifier resolveTextureId(CustomSkyModule module) {
        String raw = module.skyMode() == CustomSkyModule.SkyMode.PANORAMA
                ? module.panoramaId() : module.textureId();
        try {
            return Identifier.tryParse(raw);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static GpuBuffer gradientDomeBuffer() {
        if (gradientDomeBuffer != null) return gradientDomeBuffer;
        int count = 66;
        try (ByteBufferBuilder allocator = ByteBufferBuilder.exactlySized(
                count * DefaultVertexFormat.POSITION.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            builder.addVertex(0.0F, 512.0F, 0.0F);
            for (int i = 0; i <= 64; i++) {
                float angle = (float) (Math.PI * 2.0 * i / 64);
                builder.addVertex((float) (Math.cos(angle) * 512.0), 0.0F, (float) (Math.sin(angle) * 512.0F));
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                gradientDomeBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Arson custom sky gradient dome", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            }
        } catch (RuntimeException exception) {
            return null;
        }
        return gradientDomeBuffer;
    }

    private static GpuBuffer textureDomeBuffer() {
        if (textureDomeBuffer != null) return textureDomeBuffer;
        int count = LATITUDE_SEGMENTS * LONGITUDE_SEGMENTS * 6;
        try (ByteBufferBuilder allocator = ByteBufferBuilder.exactlySized(
                count * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(allocator, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
            for (int lat = 0; lat < LATITUDE_SEGMENTS; lat++) {
                double latitude0 = (Math.PI * 0.5) * lat / LATITUDE_SEGMENTS;
                double latitude1 = (Math.PI * 0.5) * (lat + 1) / LATITUDE_SEGMENTS;
                for (int lon = 0; lon < LONGITUDE_SEGMENTS; lon++) {
                    float u0 = (float) lon / LONGITUDE_SEGMENTS;
                    float u1 = (float) (lon + 1) / LONGITUDE_SEGMENTS;
                    double longitude0 = Math.PI * 2.0 * lon / LONGITUDE_SEGMENTS;
                    double longitude1 = Math.PI * 2.0 * (lon + 1) / LONGITUDE_SEGMENTS;
                    addSkyVertex(builder, longitude0, latitude0, u0, 0.5F - (float) (latitude0 / Math.PI));
                    addSkyVertex(builder, longitude1, latitude0, u1, 0.5F - (float) (latitude0 / Math.PI));
                    addSkyVertex(builder, longitude1, latitude1, u1, 0.5F - (float) (latitude1 / Math.PI));
                    addSkyVertex(builder, longitude0, latitude0, u0, 0.5F - (float) (latitude0 / Math.PI));
                    addSkyVertex(builder, longitude1, latitude1, u1, 0.5F - (float) (latitude1 / Math.PI));
                    addSkyVertex(builder, longitude0, latitude1, u0, 0.5F - (float) (latitude1 / Math.PI));
                }
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                textureDomeBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Arson custom sky texture dome", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            }
        } catch (RuntimeException exception) {
            return null;
        }
        return textureDomeBuffer;
    }

    /** Releases cached GPU buffers when the client shuts down. */
    public static void release() {
        if (gradientDomeBuffer != null) {
            gradientDomeBuffer.close();
            gradientDomeBuffer = null;
        }
        if (textureDomeBuffer != null) {
            textureDomeBuffer.close();
            textureDomeBuffer = null;
        }
    }

    private static void addSkyVertex(BufferBuilder builder, double longitude, double latitude, float u, float v) {
        float radius = 512.0F;
        float horizontal = (float) Math.cos(latitude) * radius;
        float x = (float) Math.cos(longitude) * horizontal;
        float y = (float) Math.sin(latitude) * radius;
        float z = (float) Math.sin(longitude) * horizontal;
        builder.addVertex(x, y, z).setUv(u, v);
    }
}
