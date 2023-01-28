package net.bfsr.client.render.texture;

import net.bfsr.client.render.FrameBuffer;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.NebulaShader;
import net.bfsr.client.shader.StarsShader;
import net.bfsr.core.Core;
import net.bfsr.util.RandomHelper;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Random;

public final class TextureGenerator {
    private static Texture generateSpaceTexture(int width, int height, float density, float brightness, Random random) {
        int count = Math.round(width * height * density);

        byte[] data = new byte[width * height * 3];

        for (int i = 0; i < count; i++) {
            int r = (int) Math.floor(random.nextDouble() * width * height);
            byte c = (byte) Math.round(255 * StrictMath.log(1 - random.nextDouble()) * -brightness);
            data[r * 3] = c;
            data[r * 3 + 1] = c;
            data[r * 3 + 2] = c;
        }

        Texture texture = new Texture(width, height);
        GL45C.glTextureStorage2D(texture.getId(), 1, GL11.GL_RGB8, width, height);

        ByteBuffer byteBuffer = MemoryUtil.memAlloc(width * height * 3);
        GL45C.glTextureSubImage2D(texture.getId(), 0, 0, 0, width, height, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, byteBuffer.put(data).flip());
        MemoryUtil.memFree(byteBuffer);

        GL45C.glGenerateTextureMipmap(texture.getId());
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_NEAREST);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL45C.glTextureParameterf(texture.getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));

        return texture;
    }

    private static Texture generateNoiseTexture(Random random, int size) {
        Texture texture = new Texture(size, size);
        int l = size * size;
        byte[] data = new byte[(l << 1)];
        for (int i = 0; i < l; i++) {
            data[i * 2] = (byte) Math.round(0.5 * (1.0 + random.nextDouble()) * 255);
            data[i * 2 + 1] = (byte) Math.round(0.5 * (1.0 + random.nextDouble()) * 255);
        }

        GL45C.glTextureStorage2D(texture.getId(), 1, GL30.GL_RG8, size, size);

        ByteBuffer byteBuffer = MemoryUtil.memAlloc(l << 1);
        GL45C.glTextureSubImage2D(texture.getId(), 0, 0, 0, size, size, GL30.GL_RG, GL11.GL_UNSIGNED_BYTE, byteBuffer.put(data).flip());
        MemoryUtil.memFree(byteBuffer);

        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL45C.glTextureParameteri(texture.getId(), GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        return texture;
    }

    public static Texture generateNebulaTexture(int width, int height, Random random) {
        NebulaShader nebulaShader = new NebulaShader();
        StarsShader starsShader = new StarsShader();
        FrameBuffer buffer = new FrameBuffer();

        buffer.generate();
        nebulaShader.load();
        nebulaShader.init();
        starsShader.load();
        starsShader.init();
        buffer.generateTexture(2, width, height);

        Vector2f baseScale = new Vector2f(1.0f, 1.0f);
        Vector2f starsCountByTextureSize = new Vector2f(width / 2560.0f, height / 1440.0f);

        int count = Math.round(random.nextFloat() * 4 + 1);

        float starsDensity = RandomHelper.randomFloat(random, 0.0002f, 0.0075f);
        float starsBrighness = RandomHelper.randomFloat(random, 0.2f, 0.95f);
        Texture stars = generateSpaceTexture(width, height, starsDensity, starsBrighness, random);
        int noiseSize = 256;

        int activeBuffer = 0;
        nebulaShader.enable();
        nebulaShader.setNoiseSize(noiseSize);

        float densityMin = 0.0f;
        float deinsityMax = 0.2f;
        float falloffMin = 3.0f;
        float falloffMax = 5.0f;

        int maxNoiseType = 8;
        float colorMax = 1.0f;

        buffer.bind();
        buffer.viewPort(width, height);

        Texture nebulaTexture = stars;

        for (int i = 0; i < count; i++) {
            Texture noise = generateNoiseTexture(random, noiseSize);
            OpenGLHelper.activateTexture0();
            OpenGLHelper.bindTexture(nebulaTexture.getId());
            OpenGLHelper.activateTexture1();
            OpenGLHelper.bindTexture(noise.getId());

            float density = RandomHelper.randomFloat(random, densityMin, deinsityMax);
            float falloff = RandomHelper.randomFloat(random, falloffMin, falloffMax);
            Vector2f offset = new Vector2f(random.nextFloat() * 10.0f, random.nextFloat() * 10.0f);
            int noiseType = random.nextInt(maxNoiseType);
            Vector3f color = new Vector3f(random.nextFloat() * colorMax, random.nextFloat() * colorMax, random.nextFloat() * colorMax);

            float scaleMin = 0.000025f / baseScale.x;
            float scaleMax = 0.002f / baseScale.x;
            if (noiseType == 0) {
                scaleMin = 0.0000001f / baseScale.x;
                scaleMax = 0.00075f / baseScale.x;
            } else if (noiseType == 2) {
                float pNoisePower = RandomHelper.randomFloat(random, 1.5f, 2.25f);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(pNoisePower, 0, random.nextFloat(), random.nextFloat()));
            } else if (noiseType == 3) {
                float sNoiseMinPower = 20.0f / baseScale.x;
                float sNoiseMaxPower = 60.0f / baseScale.x;
                float sNoisePower = RandomHelper.randomFloat(random, sNoiseMinPower, sNoiseMaxPower);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(sNoisePower, 0, 0, 0));
            } else if (noiseType == 1) {
                float cNoiseMinPower = 0.9f / baseScale.x;
                float cNoiseMaxPower = 2.2f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, 0, 0, 0));
            } else if (noiseType == 4) {
                float cNoiseMinPower = 3.0f / baseScale.x;
                float cNoiseMaxPower = 4.5f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, (float) (random.nextFloat() * Math.PI * 2.0), 0, 0));
            } else if (noiseType == 5) {
                float cNoiseMinPower = 4.0f / baseScale.x;
                float cNoiseMaxPower = 5.0f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, (float) (random.nextFloat() * Math.PI * 2.0), 0, 0));
            } else if (noiseType == 6) {
                float cNoiseMinPower = 4.0f / baseScale.x;
                float cNoiseMaxPower = 5.0f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                float offsetX = RandomHelper.randomFloat(random, 0.8f, 3.0f);
                float offsetY = RandomHelper.randomFloat(random, 0.8f, 3.0f);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, (float) (random.nextFloat() * Math.PI * 2.0), offsetX, offsetY));
            } else {
                float cNoiseMinPower = 4.0f / baseScale.x;
                float cNoiseMaxPower = 5.0f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                float offsetX = RandomHelper.randomFloat(random, 0.8f, 3.0f);
                float offsetY = RandomHelper.randomFloat(random, 0.8f, 3.0f);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, (float) (random.nextFloat() * Math.PI * 2.0), offsetX, offsetY));
            }

            float scale = RandomHelper.randomFloat(random, scaleMin, scaleMax);
            nebulaShader.setScale(scale);
            nebulaShader.setDensity(density);
            nebulaShader.setFalloff(falloff);
            nebulaShader.setColor(color);
            nebulaShader.setOffset(offset);
            nebulaShader.setNoiseType(noiseType);

            activeBuffer = 1 - activeBuffer;
            buffer.drawBuffer(activeBuffer);
            Renderer.counterClockWiseCnteredQuad.renderIndexed();
            nebulaTexture = buffer.getTexture(activeBuffer);
            noise.delete();
        }

        nebulaShader.disable();
        nebulaShader.delete();
        starsShader.enable();

        int starsCount = (int) (random.nextInt((int) (25 * starsCountByTextureSize.x)) + 10 * starsCountByTextureSize.x);
        for (int i = 0; i < starsCount; i++) {
            OpenGLHelper.activateTexture0();
            OpenGLHelper.bindTexture(nebulaTexture.getId());

            Vector2f center = new Vector2f(random.nextFloat(), random.nextFloat());
            float haloFalloff = random.nextFloat() * 0.512f + 0.064f;
            Vector3f haloColor = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            Vector2f resolution = new Vector2f(width, height);
            float scale = 1.0f;
            float coreRadius = RandomHelper.randomFloat(random, 0.1f * baseScale.x, 2.0f * baseScale.x);

            starsShader.setCenter(center);
            starsShader.setCoreRadius(coreRadius);
            starsShader.setCoreColor(new Vector3f(1, 1, 1));
            starsShader.sethaloColor(haloColor);
            starsShader.setFalloff(haloFalloff);
            starsShader.setResolution(resolution);
            starsShader.setScale(scale);

            activeBuffer = 1 - activeBuffer;
            buffer.drawBuffer(activeBuffer);
            Renderer.counterClockWiseCnteredQuad.renderIndexed();
            nebulaTexture = buffer.getTexture(activeBuffer);
        }

        int mainStars = random.nextInt((int) (4 * starsCountByTextureSize.x));
        for (int i = 0; i < mainStars; i++) {
            OpenGLHelper.activateTexture0();
            OpenGLHelper.bindTexture(nebulaTexture.getId());

            Vector2f center = new Vector2f(random.nextFloat(), random.nextFloat());
            float haloFalloff = random.nextFloat() * 0.032f + 0.008f;
            Vector3f haloColor = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            Vector2f resolution = new Vector2f(width, height);
            float scale = 1.0f;
            float coreRadius = RandomHelper.randomFloat(random, 40.0f * baseScale.x, 55.0f * baseScale.x);

            starsShader.setCenter(center);
            starsShader.setCoreRadius(coreRadius);
            starsShader.setCoreColor(new Vector3f(1, 1, 1));
            starsShader.sethaloColor(haloColor);
            starsShader.setFalloff(haloFalloff);
            starsShader.setResolution(resolution);
            starsShader.setScale(scale);

            activeBuffer = 1 - activeBuffer;
            buffer.drawBuffer(activeBuffer);
            Renderer.counterClockWiseCnteredQuad.renderIndexed();
            nebulaTexture = buffer.getTexture(activeBuffer);
        }

        starsShader.disable();
        starsShader.delete();

        OpenGLHelper.bindFrameBuffer(0);
        GL11.glViewport(0, 0, Core.getCore().getWidth(), Core.getCore().getHeight());
        OpenGLHelper.activateTexture0();

        buffer.delete();
        stars.delete();
        buffer.deleteTexture(1 - activeBuffer);

        GL45C.glTextureParameteri(nebulaTexture.getId(), GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(nebulaTexture.getId(), GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL45C.glTextureParameteri(nebulaTexture.getId(), GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL45C.glTextureParameteri(nebulaTexture.getId(), GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL45C.glTextureParameterf(nebulaTexture.getId(), EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
        long textureHandle = ARBBindlessTexture.glGetTextureHandleARB(nebulaTexture.getId());
        ARBBindlessTexture.glMakeTextureHandleResidentARB(textureHandle);
        nebulaTexture.setTextureHandle(textureHandle);

        return nebulaTexture;
    }
}