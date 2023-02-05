package net.bfsr.client.particle;

import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import net.bfsr.util.MutableInt;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public class ParticlesStoreTask implements Runnable {
    private float interpolation;
    private final MutableInt backgroundAlphaBufferIndex = new MutableInt();
    private final MutableInt backgroundAdditiveBufferIndex = new MutableInt();
    private final MutableInt alphaBufferIndex = new MutableInt();
    private final MutableInt additiveBufferIndex = new MutableInt();
    private final MutableInt backgroundAlphaVertexBufferIndex = new MutableInt();
    private final MutableInt backgroundAdditiveVertexBufferIndex = new MutableInt();
    private final MutableInt alphaVertexBufferIndex = new MutableInt();
    private final MutableInt additiveVertexBufferIndex = new MutableInt();
    private int shipWrecksStartIndex, shipWrecksEndIndex;
    private int shipWrecksEffectsStartIndex, shipWrecksEffectsEndIndex;
    private int backgroundAlphaParticlesStartIndex, backgroundAlphaParticlesEndIndex;
    private int backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex;
    private int alphaParticlesStartIndex, alphaParticlesEndIndex;
    private int additiveParticlesStartIndex, additiveParticlesEndIndex;
    private final Runnable[] runnables = new Runnable[4];

    public void init(List<ParticleWreck> particlesWrecks, List<Particle>[] particlesByRenderLayer, FloatBuffer[] vertexBuffers, ByteBuffer[] materialBuffers) {
        FloatBuffer defaultAlphaBlendedVertexBuffer = vertexBuffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()];
        FloatBuffer defaultAdditiveBlendedVertexBuffer = vertexBuffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()];
        FloatBuffer backgroundAlphaBlendedVertexBuffer = vertexBuffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()];
        FloatBuffer backgroundAdditiveBlendedVertexBuffer = vertexBuffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()];
        ByteBuffer defaultAlphaBlendedBuffer = materialBuffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()];
        ByteBuffer defaultAdditiveBlendedBuffer = materialBuffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()];
        ByteBuffer backgroundAlphaBlendedBuffer = materialBuffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()];
        ByteBuffer backgroundAdditiveBlendedBuffer = materialBuffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()];
        runnables[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()] = () -> {
            storeParticlesWrecks(particlesWrecks, defaultAlphaBlendedVertexBuffer, defaultAlphaBlendedBuffer, interpolation, shipWrecksStartIndex, shipWrecksEndIndex, alphaVertexBufferIndex,
                    alphaBufferIndex);
            storeParticles(particlesByRenderLayer[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()], defaultAlphaBlendedVertexBuffer, defaultAlphaBlendedBuffer, interpolation, alphaParticlesStartIndex, alphaParticlesEndIndex,
                    alphaVertexBufferIndex, alphaBufferIndex);
        };
        runnables[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()] = () -> storeParticles(particlesByRenderLayer[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()], backgroundAlphaBlendedVertexBuffer,
                backgroundAlphaBlendedBuffer, interpolation, backgroundAlphaParticlesStartIndex, backgroundAlphaParticlesEndIndex, backgroundAlphaVertexBufferIndex, backgroundAlphaBufferIndex);
        runnables[RenderLayer.DEFAULT_ADDITIVE.ordinal()] = () -> {
            storeParticlesWrecksEffects(particlesWrecks, defaultAdditiveBlendedVertexBuffer, defaultAdditiveBlendedBuffer, interpolation, shipWrecksEffectsStartIndex, shipWrecksEffectsEndIndex,
                    additiveVertexBufferIndex, additiveBufferIndex);
            storeParticles(particlesByRenderLayer[RenderLayer.DEFAULT_ADDITIVE.ordinal()], defaultAdditiveBlendedVertexBuffer, defaultAdditiveBlendedBuffer, interpolation,
                    additiveParticlesStartIndex, additiveParticlesEndIndex, additiveVertexBufferIndex, additiveBufferIndex);
        };
        runnables[RenderLayer.BACKGROUND_ADDITIVE.ordinal()] = () -> storeParticles(particlesByRenderLayer[RenderLayer.BACKGROUND_ADDITIVE.ordinal()], backgroundAdditiveBlendedVertexBuffer,
                backgroundAdditiveBlendedBuffer, interpolation, backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex, backgroundAdditiveVertexBufferIndex,
                backgroundAdditiveBufferIndex);
    }

    public void update(float interpolation, int backgroundAlphaBufferIndex, int backgroundAdditiveBufferIndex, int alphaBufferIndex, int additiveBufferIndex,
                       int shipWrecksStartIndex, int shipWrecksEndIndex, int shipWrecksEffectsStartIndex, int shipWrecksEffectsEndIndex,
                       int backgroundAlphaParticlesStartIndex, int backgroundAlphaParticlesEndIndex,
                       int backgroundAdditiveParticlesStartIndex, int backgroundAdditiveParticlesEndIndex,
                       int alphaParticlesStartIndex, int alphaParticlesEndIndex,
                       int additiveParticlesStartIndex, int additiveParticlesEndIndex) {
        this.interpolation = interpolation;
        this.backgroundAlphaVertexBufferIndex.set(backgroundAlphaBufferIndex);
        this.backgroundAlphaBufferIndex.set(backgroundAlphaBufferIndex >> 1 << 2);
        this.backgroundAdditiveVertexBufferIndex.set(backgroundAdditiveBufferIndex);
        this.backgroundAdditiveBufferIndex.set(backgroundAdditiveBufferIndex >> 1 << 2);
        this.alphaVertexBufferIndex.set(alphaBufferIndex);
        this.alphaBufferIndex.set(alphaBufferIndex >> 1 << 2);
        this.additiveVertexBufferIndex.set(additiveBufferIndex);
        this.additiveBufferIndex.set(additiveBufferIndex >> 1 << 2);
        this.shipWrecksStartIndex = shipWrecksStartIndex;
        this.shipWrecksEndIndex = shipWrecksEndIndex;
        this.shipWrecksEffectsStartIndex = shipWrecksEffectsStartIndex;
        this.shipWrecksEffectsEndIndex = shipWrecksEffectsEndIndex;
        this.backgroundAlphaParticlesStartIndex = backgroundAlphaParticlesStartIndex;
        this.backgroundAlphaParticlesEndIndex = backgroundAlphaParticlesEndIndex;
        this.backgroundAdditiveParticlesStartIndex = backgroundAdditiveParticlesStartIndex;
        this.backgroundAdditiveParticlesEndIndex = backgroundAdditiveParticlesEndIndex;
        this.alphaParticlesStartIndex = alphaParticlesStartIndex;
        this.alphaParticlesEndIndex = alphaParticlesEndIndex;
        this.additiveParticlesStartIndex = additiveParticlesStartIndex;
        this.additiveParticlesEndIndex = additiveParticlesEndIndex;
    }

    @Override
    public void run() {
        for (int i = 0; i < runnables.length; i++) {
            runnables[i].run();
        }
    }

    public void storeParticlesWrecks(List<ParticleWreck> particles, FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation, int start, int end, MutableInt vertexBufferIndex,
                                     MutableInt materialBufferIndex) {
        for (int i = start; i < end; i++) {
            ParticleWreck p = particles.get(i);
            storeParticle(p, vertexBuffer, materialBuffer, interpolation, vertexBufferIndex, materialBufferIndex);
        }
    }

    private void storeParticlesWrecksEffects(List<ParticleWreck> particles, FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation, int start, int end,
                                             MutableInt vertexBufferIndex, MutableInt materialBufferIndex) {
        int index = 0;
        int size = particles.size();
        for (int i = 0; i < size && index < end; i++) {
            ParticleWreck particleWreck = particles.get(i);
            if (particleWreck.getColorFire() != null && particleWreck.getColorFire().w > 0) {
                if (index >= start) {
                    storeVertices(particleWreck, interpolation, vertexBuffer, vertexBufferIndex);
                    storeColor(particleWreck.getLastColorFire(), particleWreck.getColorFire(), materialBuffer, materialBufferIndex, interpolation);
                    storeTextureHandle(particleWreck.getTextureFire().getTextureHandle(), materialBuffer, materialBufferIndex);
                }
                index++;
            }

            if (particleWreck.getColorLight() != null && particleWreck.getColorLight().w > 0) {
                if (index >= start && index < end) {
                    storeVertices(particleWreck, interpolation, vertexBuffer, vertexBufferIndex);
                    storeColor(particleWreck.getLastColorLight(), particleWreck.getColorLight(), materialBuffer, materialBufferIndex, interpolation);
                    storeTextureHandle(particleWreck.getTextureLight().getTextureHandle(), materialBuffer, materialBufferIndex);
                }
                index++;
            }
        }
    }

    private void storeParticles(List<Particle> particles, FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation, int start, int end, MutableInt vertexBufferIndex,
                                MutableInt materialBufferIndex) {
        for (int i = start; i < end; i++) {
            storeParticle(particles.get(i), vertexBuffer, materialBuffer, interpolation, vertexBufferIndex, materialBufferIndex);
        }
    }

    private void storeParticle(Particle particle, FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation, MutableInt vertexBufferIndex, MutableInt materialBufferIndex) {
        storeVertices(particle.getLastPosition().x, particle.getLastPosition().y, particle.getPosition().x, particle.getPosition().y, particle.getLastRotation(),
                particle.getRotation(), particle.getLastScale().x, particle.getLastScale().y, particle.getScale().x, particle.getScale().y, interpolation, vertexBuffer, vertexBufferIndex);
        storeParticleData(particle, materialBuffer, materialBufferIndex, interpolation);
    }

    private void storeParticleData(Particle particle, ByteBuffer materialBuffer, MutableInt index, float interpolation) {
        storeColor(particle.getLastColor(), particle.getColor(), materialBuffer, index, interpolation);
        storeTextureHandle(particle.getTexture().getTextureHandle(), materialBuffer, index);
    }

    private void storeVertices(Particle particle, float interpolation, FloatBuffer vertexBuffer, MutableInt index) {
        storeVertices(particle.getLastPosition().x, particle.getLastPosition().y, particle.getPosition().x, particle.getPosition().y, particle.getLastRotation(),
                particle.getRotation(), particle.getLastScale().x, particle.getLastScale().y, particle.getScale().x, particle.getScale().y, interpolation, vertexBuffer, index);
    }

    private void storeVertices(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                               float interpolation, FloatBuffer vertexBuffer, MutableInt index) {
        final float sizeX = 0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation);
        final float sizeY = 0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation);
        final float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation;
        final float sin = LUT.sin(interpolatedRotation);
        final float cos = LUT.cos(interpolatedRotation);
        final float positionX = lastX + (x - lastX) * interpolation;
        final float positionY = lastY + (y - lastY) * interpolation;
        storeVertices(positionX, positionY, sin, cos, sizeX, sizeY, vertexBuffer, index);
    }

    private void storeVertices(float x, float y, float sizeX, float sizeY, FloatBuffer vertexBuffer, MutableInt index) {
        final float x1 = -sizeX + x;
        final float x2 = sizeX + x;
        final float x3 = sizeX + x;
        final float x4 = -sizeX + x;
        final float y1 = sizeY + y;
        final float y2 = sizeY + y;
        final float y3 = -sizeY + y;
        final float y4 = -sizeY + y;

        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;

        vertexBuffer.put(index.getAndAdd(1), x1);
        vertexBuffer.put(index.getAndAdd(1), y1);
        vertexBuffer.put(index.getAndAdd(1), u1);
        vertexBuffer.put(index.getAndAdd(1), v1);
        vertexBuffer.put(index.getAndAdd(1), x2);
        vertexBuffer.put(index.getAndAdd(1), y2);
        vertexBuffer.put(index.getAndAdd(1), u2);
        vertexBuffer.put(index.getAndAdd(1), v2);
        vertexBuffer.put(index.getAndAdd(1), x3);
        vertexBuffer.put(index.getAndAdd(1), y3);
        vertexBuffer.put(index.getAndAdd(1), u3);
        vertexBuffer.put(index.getAndAdd(1), v3);
        vertexBuffer.put(index.getAndAdd(1), x4);
        vertexBuffer.put(index.getAndAdd(1), y4);
        vertexBuffer.put(index.getAndAdd(1), u4);
        vertexBuffer.put(index.getAndAdd(1), v4);
    }

    private void storeVertices(float x, float y, float sin, float cos, float sizeX, float sizeY, FloatBuffer vertexBuffer, MutableInt index) {
        final float minusSizeX = -sizeX;
        final float minusSizeY = -sizeY;

        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;

        final float sinSizeX = sin * sizeX;
        final float cosSizeX = cos * sizeX;
        final float sinSizeY = sin * sizeY;
        final float cosSizeY = cos * sizeY;

        final float x1 = cos * minusSizeX - sinSizeY + x;
        final float x2 = cosSizeX - sinSizeY + x;
        final float x3 = cosSizeX - sin * minusSizeY + x;
        final float y3 = sinSizeX + cos * minusSizeY + y;
        final float y1 = sin * minusSizeX + cosSizeY + y;
        final float y2 = sinSizeX + cosSizeY + y;

        vertexBuffer.put(index.getAndAdd(1), x1);
        vertexBuffer.put(index.getAndAdd(1), y1);
        vertexBuffer.put(index.getAndAdd(1), u1);
        vertexBuffer.put(index.getAndAdd(1), v1);
        vertexBuffer.put(index.getAndAdd(1), x2);
        vertexBuffer.put(index.getAndAdd(1), y2);
        vertexBuffer.put(index.getAndAdd(1), u2);
        vertexBuffer.put(index.getAndAdd(1), v2);
        vertexBuffer.put(index.getAndAdd(1), x3);
        vertexBuffer.put(index.getAndAdd(1), y3);
        vertexBuffer.put(index.getAndAdd(1), u3);
        vertexBuffer.put(index.getAndAdd(1), v3);
        vertexBuffer.put(index.getAndAdd(1), x1 + (x3 - x2));
        vertexBuffer.put(index.getAndAdd(1), y3 - (y2 - y1));
        vertexBuffer.put(index.getAndAdd(1), u4);
        vertexBuffer.put(index.getAndAdd(1), v4);
    }

    private void storeColor(Vector4f lastColor, Vector4f color, ByteBuffer buffer, MutableInt index, float interpolation) {
        buffer.putFloat(index.getAndAdd(4), color.x);
        buffer.putFloat(index.getAndAdd(4), color.y);
        buffer.putFloat(index.getAndAdd(4), color.z);
        buffer.putFloat(index.getAndAdd(4), lastColor.w + (color.w - lastColor.w) * interpolation);
    }

    private void storeTextureHandle(long textureHandle, ByteBuffer buffer, MutableInt index) {
        buffer.putLong(index.getAndAdd(8), textureHandle);
        buffer.putLong(index.getAndAdd(8), 0);//padding
    }
}
