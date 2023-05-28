package net.bfsr.engine.renderer.particle;

import net.bfsr.engine.util.MutableInt;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface ParticleRender {
    void putToBuffer(FloatBuffer vertexBuffer, ByteBuffer materialBuffer, float interpolation,
                     MutableInt vertexBufferIndex, MutableInt materialBufferIndex);

    void update();

    boolean isDead();
}