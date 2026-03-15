package net.bfsr.engine.renderer.particle;

import java.util.List;

public class ParticlesStoreRunnable implements Runnable {
    private int alphaParticlesStartIndex, alphaParticlesEndIndex;
    private int additiveParticlesStartIndex, additiveParticlesEndIndex;
    private final Runnable[] runnables = new Runnable[2];
    private final List<ParticleRender>[] particlesByType;

    ParticlesStoreRunnable(List<ParticleRender>[] particlesByType) {
        this.particlesByType = particlesByType;
    }

    public void init() {
        runnables[0] = () -> storeParticles(particlesByType[ParticleType.ALPHA_BLENDED.ordinal()],
                alphaParticlesStartIndex, alphaParticlesEndIndex);
        runnables[1] = () -> storeParticles(particlesByType[ParticleType.ADDITIVE.ordinal()],
                additiveParticlesStartIndex, additiveParticlesEndIndex);
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