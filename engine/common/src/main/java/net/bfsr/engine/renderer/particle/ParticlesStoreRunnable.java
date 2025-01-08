package net.bfsr.engine.renderer.particle;

import java.util.List;

public class ParticlesStoreRunnable implements Runnable {
    private int alphaParticlesStartIndex, alphaParticlesEndIndex;
    private int additiveParticlesStartIndex, additiveParticlesEndIndex;
    private final Runnable[] runnables = new Runnable[2];
    private final List<ParticleRender>[] particlesByRenderLayer;
    private final RenderLayer renderLayer;

    ParticlesStoreRunnable(List<ParticleRender>[] particlesByRenderLayer, RenderLayer renderLayer) {
        this.particlesByRenderLayer = particlesByRenderLayer;
        this.renderLayer = renderLayer;
    }

    public void init() {
        if (renderLayer == RenderLayer.DEFAULT_ALPHA_BLENDED) {
            runnables[0] = () -> storeParticles(particlesByRenderLayer[RenderLayer.DEFAULT_ALPHA_BLENDED.ordinal()],
                    alphaParticlesStartIndex, alphaParticlesEndIndex);
            runnables[1] = () -> storeParticles(particlesByRenderLayer[RenderLayer.DEFAULT_ADDITIVE.ordinal()],
                    additiveParticlesStartIndex, additiveParticlesEndIndex);
        } else {
            runnables[0] = () -> storeParticles(particlesByRenderLayer[RenderLayer.BACKGROUND_ALPHA_BLENDED.ordinal()],
                    alphaParticlesStartIndex, alphaParticlesEndIndex);
            runnables[1] = () -> storeParticles(particlesByRenderLayer[RenderLayer.BACKGROUND_ADDITIVE.ordinal()],
                    additiveParticlesStartIndex, additiveParticlesEndIndex);
        }
    }

    public void update(int alphaParticlesStartIndex, int alphaParticlesEndIndex, int additiveParticlesStartIndex,
                       int additiveParticlesEndIndex) {
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

    private void storeParticles(List<ParticleRender> particles, int start, int end) {
        for (int i = start; i < end; i++) {
            particles.get(i).putToBuffer(i);
        }
    }
}