package net.bfsr.engine.renderer.primitive;

public interface AbstractVBO {
    void storeData(long address, long newDataSize, int flags);
    void storeData(long address, long fullDataSize, long offset, long newDataSize, int flags);
    void bindBufferBase(int target, int index);
    void bindBuffer(int target);
}
