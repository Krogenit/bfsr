package net.bfsr.client.renderer.particle;

import net.bfsr.client.particle.Particle;
import net.bfsr.client.particle.RenderLayer;
import net.bfsr.core.Core;
import net.bfsr.util.MutableInt;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public class ParticlesStoreTask implements Runnable {
    private float interpolation;
    private final MutableInt alphaBufferIndex = new MutableInt();
    private final MutableInt additiveBufferIndex = new MutableInt();
    private final MutableInt alphaVertexBufferIndex = new MutableInt();
    private final MutableInt additiveVertexBufferIndex = new MutableInt();
    private int alphaParticlesStartIndex, alphaParticlesEndIndex;
    private int additiveParticlesStartIndex, additiveParticlesEndIndex;
    private final Runnable[] runnables = new Runnable[2];
    private final List<Particle>[] particlesByRenderLayer;
    private final RenderLayer renderLayer;

    public ParticlesStoreTask(List<Particle>[] particlesByRenderLayer, RenderLayer renderLayer) {
        this.particlesByRenderLayer = particlesByRenderLayer;
        this.renderLayer = renderLayer;
    }

    public void init(FloatBuffer[] vertexBuffers, ByteBuffer[] materialBuffers) {
        FloatBuffer defaultAlphaBlendedVertexBuffer = vertexBuffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()];
        FloatBuffer defaultAdditiveBlendedVertexBuffer = vertexBuffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()];
        FloatBuffer backgroundAlphaBlendedVertexBuffer = vertexBuffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()];
        FloatBuffer backgroundAdditiveBlendedVertexBuffer = vertexBuffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()];
        ByteBuffer defaultAlphaBlendedBuffer = materialBuffers[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()];
        ByteBuffer defaultAdditiveBlendedBuffer = materialBuffers[RenderLayer.DEFAULT_ADDITIVE.ordinal()];
        ByteBuffer backgroundAlphaBlendedBuffer = materialBuffers[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()];
        ByteBuffer backgroundAdditiveBlendedBuffer = materialBuffers[RenderLayer.BACKGROUND_ADDITIVE.ordinal()];
        if (renderLayer == RenderLayer.DEFAULT_ALPHA_BLENDED) {
            runnables[0] = () -> storeParticles(particlesByRenderLayer[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()], defaultAlphaBlendedVertexBuffer, defaultAlphaBlendedBuffer, interpolation,
                    alphaParticlesStartIndex, alphaParticlesEndIndex, alphaVertexBufferIndex, alphaBufferIndex);
            runnables[1] = () -> storeParticles(particlesByRenderLayer[RenderLayer.DEFAULT_ADDITIVE.ordinal()], defaultAdditiveBlendedVertexBuffer, defaultAdditiveBlendedBuffer, interpolation,
                    additiveParticlesStartIndex, additiveParticlesEndIndex, additiveVertexBufferIndex, additiveBufferIndex);
        } else {
            runnables[0] = () -> storeParticles(particlesByRenderLayer[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()],
                    backgroundAlphaBlendedVertexBuffer, backgroundAlphaBlendedBuffer, interpolation, alphaParticlesStartIndex, alphaParticlesEndIndex, alphaVertexBufferIndex, alphaBufferIndex);
            runnables[1] = () -> storeParticles(particlesByRenderLayer[RenderLayer.BACKGROUND_ADDITIVE.ordinal()], backgroundAdditiveBlendedVertexBuffer,
                    backgroundAdditiveBlendedBuffer, interpolation, additiveParticlesStartIndex, additiveParticlesEndIndex, additiveVertexBufferIndex, additiveBufferIndex);
        }
    }

    public void update(int alphaBufferIndex, int additiveBufferIndex, int alphaParticlesStartIndex, int alphaParticlesEndIndex, int additiveParticlesStartIndex, int additiveParticlesEndIndex) {
        this.interpolation = Core.get().getRenderer().getInterpolation();
        this.alphaVertexBufferIndex.set(alphaBufferIndex);
        this.alphaBufferIndex.set(alphaBufferIndex >> 1 << 2);
        this.additiveVertexBufferIndex.set(additiveBufferIndex);
        this.additiveBufferIndex.set(additiveBufferIndex >> 1 << 2);
        this.alphaParticlesStartIndex = alphaParticlesStartIndex;
        this.alphaParticlesEndIndex = alphaParticlesEndIndex;
        this.additiveParticlesStartIndex = additiveParticlesStartIndex;
        this.additiveParticlesEndIndex = additiveParticlesEndIndex;
    }

    @Override
    public void run() {
        runnables[0].run();
        runnables[1].run();
    }

    private void storeParticles(List<Particle> particles, FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation, int start, int end, MutableInt vertexBufferIndex,
                                MutableInt materialBufferIndex) {
        for (int i = start; i < end; i++) {
            particles.get(i).putToBuffer(vertexBuffer, materialBuffer, interpolation, vertexBufferIndex, materialBufferIndex);
        }
    }
}
