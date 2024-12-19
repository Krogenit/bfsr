package net.bfsr.engine.renderer.primitive;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface AbstractVAO {
    void bind();
    void updateBuffer(int index, ByteBuffer buffer, int flags);
    void updateBuffer(int index, IntBuffer buffer, int flags);
    void updateBuffer(int index, FloatBuffer buffer, int flags);
    void bindBufferBase(int target, int index, int bufferIndex);
    void bindBuffer(int target, int bufferIndex);
}
