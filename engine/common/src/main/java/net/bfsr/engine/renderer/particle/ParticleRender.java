package net.bfsr.engine.renderer.particle;

public interface ParticleRender {
    void putToBuffer(int index);

    void update();
    void postWorldUpdate();

    boolean isDead();
}