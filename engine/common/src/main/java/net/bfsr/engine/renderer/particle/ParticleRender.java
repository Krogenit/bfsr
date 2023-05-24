package net.bfsr.engine.renderer.particle;

import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.util.MutableInt;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface ParticleRender {
    void putToBuffer(AbstractSpriteRenderer spriteRenderer, FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation,
                     MutableInt vertexBufferIndex, MutableInt materialBufferIndex);
    void update();
    boolean isDead();
}