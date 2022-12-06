package net.bfsr.client.texture;

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
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Random;

public class TextureGenerator {

    public static Texture generateSpaceTexture(int width, int height, float density, float brightness, Random random) {
        int count = Math.round(width * height * density);

        byte[] data = new byte[width * height * 3];

        for (int i = 0; i < count; i++) {
            int r = (int) Math.floor(random.nextDouble() * width * height);
            byte c = (byte) Math.round(255 * Math.log(1 - random.nextDouble()) * -brightness);
            data[r * 3] = c;
            data[r * 3 + 1] = c;
            data[r * 3 + 2] = c;
        }

        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 3);
        buffer.put(data);
        buffer.flip();

        Texture texture = new Texture(width, height);
        OpenGLHelper.bindTexture(texture.getId());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);

        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float amount = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
        }

        MemoryUtil.memFree(buffer);

        return texture;
    }

    public static Texture generateNoiseTexture(Random random, int size) {
        Texture texture = new Texture(size, size);
        int l = size * size;
        byte[] data = new byte[l * 2];
        for (int i = 0; i < l; i++) {
            data[i * 2] = (byte) Math.round(0.5 * (1.0 + random.nextDouble()) * 255);
            data[i * 2 + 1] = (byte) Math.round(0.5 * (1.0 + random.nextDouble()) * 255);
        }

        ByteBuffer buffer = MemoryUtil.memAlloc(l * 2);
        buffer.put(data);
        buffer.flip();

        OpenGLHelper.bindTexture(texture.getId());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RG8, size, size, 0, GL30.GL_RG, GL11.GL_UNSIGNED_BYTE, buffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        MemoryUtil.memFree(buffer);

        return texture;
    }

//	static FrameBuffer buffer;
//	static boolean init = false;
//	static NebulaShader nebulaShader;
//	static StarsShader starsShader;
//	static int lastTexture;
//	
//	static long lastTime;
//	static Texture stars, noise;

    public static Texture generateNebulaTexture(int width, int height, Random random) {
        FrameBuffer buffer = new FrameBuffer();
        buffer.generate();
        buffer.generateTexture(2, width, height);

        NebulaShader nebulaShader = new NebulaShader();
        nebulaShader.load();
        nebulaShader.init();
        StarsShader starsShader = new StarsShader();
        starsShader.load();
        starsShader.init();
//		if(!init) {
//			buffer = new FrameBuffer();
//			buffer.generate();
//			buffer.generateTexture(2, width, height);
//			init = true;
//			nebulaShader = new NebulaShader();
//			starsShader = new StarsShader();
//		}
//		nebulaShader = new NebulaShader();
//		starsShader = new StarsShader();
//		long time = System.currentTimeMillis();
//		if(time - lastTime < 1000 && init) {
//			return buffer.getTexture(lastTexture);
//		}
//		
//		if(stars != null) {
//			stars.delete();
//		}

        Vector2f baseScale = new Vector2f(1f, 1f);
        Vector2f starsCountByTextureSize = new Vector2f(width / 2560f, height / 1440f);

        buffer.deleteTextures();
        buffer.generateTexture(2, width, height);

        int count = Math.round(random.nextFloat() * 4 + 1);
//		lastTime = time;

        float starsDensity = RandomHelper.randomFloat(random, 0.0002f, 0.0075f);
        float starsBrighness = RandomHelper.randomFloat(random, 0.2f, 0.95f);
        Texture stars = generateSpaceTexture(width, height, starsDensity, starsBrighness, random);
        int noiseSize = 256;

        int activeBuffer = 0;
        nebulaShader.enable();
        nebulaShader.setNoiseSize(noiseSize);

        float densityMin = 0.0f;
        float deinsityMax = 0.2f;
        float falloffMin = 3f;
        float falloffMax = 5f;

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
            Vector2f offset = new Vector2f(random.nextFloat() * 10f, random.nextFloat() * 10f);
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
                float sNoiseMinPower = 20f / baseScale.x;
                float sNoiseMaxPower = 60f / baseScale.x;
                float sNoisePower = RandomHelper.randomFloat(random, sNoiseMinPower, sNoiseMaxPower);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(sNoisePower, 0, 0, 0));
            } else if (noiseType == 1) {
                float cNoiseMinPower = 0.9f / baseScale.x;
                float cNoiseMaxPower = 2.2f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, 0, 0, 0));
            } else if (noiseType == 4) {
                float cNoiseMinPower = 3f / baseScale.x;
                float cNoiseMaxPower = 4.5f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, (float) (random.nextFloat() * Math.PI * 2.0), 0, 0));
            } else if (noiseType == 5) {
                float cNoiseMinPower = 4f / baseScale.x;
                float cNoiseMaxPower = 5f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, (float) (random.nextFloat() * Math.PI * 2.0), 0, 0));
            } else if (noiseType == 6) {
                float cNoiseMinPower = 4f / baseScale.x;
                float cNoiseMaxPower = 5f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                float offsetX = RandomHelper.randomFloat(random, 0.8f, 3f);
                float offsetY = RandomHelper.randomFloat(random, 0.8f, 3f);
                nebulaShader.setPNoiseRepeatVector(new Vector4f(cNoisePower, (float) (random.nextFloat() * Math.PI * 2.0), offsetX, offsetY));
            } else if (noiseType == 7) {
                float cNoiseMinPower = 4f / baseScale.x;
                float cNoiseMaxPower = 5f / baseScale.x;
                float cNoisePower = RandomHelper.randomFloat(random, cNoiseMinPower, cNoiseMaxPower);
                float offsetX = RandomHelper.randomFloat(random, 0.8f, 3f);
                float offsetY = RandomHelper.randomFloat(random, 0.8f, 3f);
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
            Renderer.quad.render();
            nebulaTexture = buffer.getTexture(activeBuffer);
            noise.delete();
        }

        nebulaShader.disable();
        starsShader.enable();

        int starsCount = (int) (random.nextInt((int) (25 * starsCountByTextureSize.x)) + 10 * starsCountByTextureSize.x);
        for (int i = 0; i < starsCount; i++) {
            OpenGLHelper.activateTexture0();
            OpenGLHelper.bindTexture(nebulaTexture.getId());

            Vector2f center = new Vector2f(random.nextFloat(), random.nextFloat());
            float haloFalloff = random.nextFloat() * 0.512f + 0.064f;
            Vector3f haloColor = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            Vector2f resolution = new Vector2f(width, height);
            float scale = 1f;
            float coreRadius = RandomHelper.randomFloat(random, 0.1f * baseScale.x, 2f * baseScale.x);

            starsShader.setCenter(center);
            starsShader.setCoreRadius(coreRadius);
            starsShader.setCoreColor(new Vector3f(1, 1, 1));
            starsShader.sethaloColor(haloColor);
            starsShader.setFalloff(haloFalloff);
            starsShader.setResolution(resolution);
            starsShader.setScale(scale);

            activeBuffer = 1 - activeBuffer;
            buffer.drawBuffer(activeBuffer);
            Renderer.quad.render();
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
            float scale = 1f;
            float coreRadius = RandomHelper.randomFloat(random, 40f * baseScale.x, 55f * baseScale.x);

            starsShader.setCenter(center);
            starsShader.setCoreRadius(coreRadius);
            starsShader.setCoreColor(new Vector3f(1, 1, 1));
            starsShader.sethaloColor(haloColor);
            starsShader.setFalloff(haloFalloff);
            starsShader.setResolution(resolution);
            starsShader.setScale(scale);

            activeBuffer = 1 - activeBuffer;
            buffer.drawBuffer(activeBuffer);
            Renderer.quad.render();
            nebulaTexture = buffer.getTexture(activeBuffer);
        }

        starsShader.disable();
//		lastTexture = activeBuffer;

        OpenGLHelper.bindFrameBuffer(0);
        GL11.glViewport(0, 0, Core.getCore().getWidth(), Core.getCore().getHeight());
        OpenGLHelper.activateTexture0();

        buffer.delete();
        stars.delete();
        buffer.deleteTexture(1 - activeBuffer);

        Texture texture = nebulaTexture;
        texture.bind();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float amount = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
        }

        return texture;
    }
}
