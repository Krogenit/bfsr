package net.bfsr.client.particle;

import net.bfsr.util.MutableInt;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.List;

public class ParticlesStoreTask implements Runnable {
    private float interpolation;
    private final MutableInt backgroundAlphaBufferIndex = new MutableInt();
    private final MutableInt backgroundAdditiveBufferIndex = new MutableInt();
    private final MutableInt alphaBufferIndex = new MutableInt();
    private final MutableInt additiveBufferIndex = new MutableInt();
    private int shipWrecksStartIndex, shipWrecksEndIndex;
    private int shipWrecksEffectsStartIndex, shipWrecksEffectsEndIndex;
    private int backgroundAlphaParticlesStartIndex, backgroundAlphaParticlesEndIndex;
    private int backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex;
    private int alphaParticlesStartIndex, alphaParticlesEndIndex;
    private int additiveParticlesStartIndex, additiveParticlesEndIndex;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Runnable[] runnables = new Runnable[4];

    public void init(List<ParticleWreck> particlesWrecks, List<Particle>[] particlesByRenderLayer, ByteBuffer[] buffers) {
        ByteBuffer defaultAlphaBlendedBuffer = buffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()];
        ByteBuffer defaultAdditiveBlendedBuffer = buffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()];
        ByteBuffer backgroundAlphaBlendedBuffer = buffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()];
        ByteBuffer backgroundAdditiveBlendedBuffer = buffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()];
        runnables[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()] = () -> {
            storeParticlesWrecks(particlesWrecks, defaultAlphaBlendedBuffer, interpolation, shipWrecksStartIndex, shipWrecksEndIndex, alphaBufferIndex);
            storeParticles(particlesByRenderLayer[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()], defaultAlphaBlendedBuffer, interpolation, alphaParticlesStartIndex, alphaParticlesEndIndex,
                    alphaBufferIndex);
        };
        runnables[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()] = () -> storeParticles(particlesByRenderLayer[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()], backgroundAlphaBlendedBuffer,
                interpolation, backgroundAlphaParticlesStartIndex, backgroundAlphaParticlesEndIndex, backgroundAlphaBufferIndex);
        runnables[RenderLayer.DEFAULT_ADDITIVE.ordinal()] = () -> {
            storeParticlesWrecksEffects(particlesWrecks, defaultAdditiveBlendedBuffer, interpolation, shipWrecksEffectsStartIndex, shipWrecksEffectsEndIndex, additiveBufferIndex);
            storeParticles(particlesByRenderLayer[RenderLayer.DEFAULT_ADDITIVE.ordinal()], defaultAdditiveBlendedBuffer, interpolation, additiveParticlesStartIndex, additiveParticlesEndIndex,
                    additiveBufferIndex);
        };
        runnables[RenderLayer.BACKGROUND_ADDITIVE.ordinal()] = () -> storeParticles(particlesByRenderLayer[RenderLayer.BACKGROUND_ADDITIVE.ordinal()], backgroundAdditiveBlendedBuffer,
                interpolation, backgroundAdditiveParticlesStartIndex, backgroundAdditiveParticlesEndIndex, backgroundAdditiveBufferIndex);
    }

    public void update(float interpolation, int backgroundAlphaBufferIndex, int backgroundAdditiveBufferIndex, int alphaBufferIndex, int additiveBufferIndex,
                       int shipWrecksStartIndex, int shipWrecksEndIndex, int shipWrecksEffectsStartIndex, int shipWrecksEffectsEndIndex,
                       int backgroundAlphaParticlesStartIndex, int backgroundAlphaParticlesEndIndex,
                       int backgroundAdditiveParticlesStartIndex, int backgroundAdditiveParticlesEndIndex,
                       int alphaParticlesStartIndex, int alphaParticlesEndIndex,
                       int additiveParticlesStartIndex, int additiveParticlesEndIndex) {
        this.interpolation = interpolation;
        this.backgroundAlphaBufferIndex.set(backgroundAlphaBufferIndex);
        this.backgroundAdditiveBufferIndex.set(backgroundAdditiveBufferIndex);
        this.alphaBufferIndex.set(alphaBufferIndex);
        this.additiveBufferIndex.set(additiveBufferIndex);
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

    public void storeParticlesWrecks(List<ParticleWreck> particles, ByteBuffer buffer, float interpolation, int start, int end, MutableInt bufferIndex) {
        for (int i = start; i < end; i++) {
            ParticleWreck p = particles.get(i);
            storeParticle(p, buffer, interpolation, bufferIndex);
        }
    }

    private void storeParticlesWrecksEffects(List<ParticleWreck> particles, ByteBuffer buffer, float interpolation, int start, int end, MutableInt bufferIndex) {
        int index = 0;
        int size = particles.size();
        for (int i = 0; i < size && index < end; i++) {
            ParticleWreck particleWreck = particles.get(i);
            if (particleWreck.getColorFire() != null && particleWreck.getColorFire().w > 0) {
                if (index >= start) {
                    storeMatrix(particleWreck.getModelMatrixType().getMatrix(particleWreck, interpolation, modelMatrix), buffer, bufferIndex);
                    storeColor(particleWreck.getColorFire(), buffer, bufferIndex);
                    storeTextureHandle(particleWreck.getTextureFire().getTextureHandle(), buffer, bufferIndex);
                }
                index++;
            }

            if (particleWreck.getColorLight() != null && particleWreck.getColorLight().w > 0) {
                if (index >= start && index < end) {
                    storeMatrix(particleWreck.getModelMatrixType().getMatrix(particleWreck, interpolation, modelMatrix), buffer, bufferIndex);
                    storeColor(particleWreck.getColorLight(), buffer, bufferIndex);
                    storeTextureHandle(particleWreck.getTextureLight().getTextureHandle(), buffer, bufferIndex);
                }
                index++;
            }
        }
    }

    private void storeParticles(List<Particle> particles, ByteBuffer buffer, float interpolation, int start, int end, MutableInt bufferIndex) {
        for (int i = start; i < end; i++) {
            storeParticle(particles.get(i), buffer, interpolation, bufferIndex);
        }
    }

    private void storeParticle(Particle particle, ByteBuffer buffer, float interpolation, MutableInt index) {
        storeMatrix(particle.getModelMatrixType().getMatrix(particle, interpolation, modelMatrix), buffer, index);
        storeParticleData(particle, buffer, index);
    }

    private void storeParticleData(Particle particle, ByteBuffer buffer, MutableInt index) {
        storeColor(particle.getColor(), buffer, index);
        storeTextureHandle(particle.getTexture().getTextureHandle(), buffer, index);
    }

    private void storeMatrix(Matrix4f modelMatrix, ByteBuffer buffer, MutableInt index) {
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m00());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m01());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m02());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m03());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m10());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m11());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m12());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m13());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m20());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m21());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m22());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m23());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m30());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m31());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m32());
        buffer.putFloat(index.getAndAdd(4), modelMatrix.m33());
    }

    private void storeColor(Vector4f color, ByteBuffer buffer, MutableInt index) {
        buffer.putFloat(index.getAndAdd(4), color.x);
        buffer.putFloat(index.getAndAdd(4), color.y);
        buffer.putFloat(index.getAndAdd(4), color.z);
        buffer.putFloat(index.getAndAdd(4), color.w);
    }

    private void storeTextureHandle(long textureHandle, ByteBuffer buffer, MutableInt index) {
        buffer.putLong(index.getAndAdd(8), textureHandle);
    }
}
