package net.bfsr.engine.renderer.buffer;

import net.bfsr.engine.util.MutableInt;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public abstract class AbstractBuffersHolder {
    public abstract void checkBuffersSize(int objectCount);
    public abstract void addObjectCount(int count);
    public abstract void incrementObjectCount();
    public abstract FloatBuffer getVertexBuffer();
    public abstract MutableInt getVertexBufferIndex();
    public abstract ByteBuffer getMaterialBuffer();
    public abstract MutableInt getMaterialBufferIndex();
    public abstract int getObjectCount();
    public abstract void reset();
}