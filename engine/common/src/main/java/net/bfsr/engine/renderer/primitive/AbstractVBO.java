package net.bfsr.engine.renderer.primitive;

public interface AbstractVBO {
    void storeData(long address, long newDataSize, int flags);
}
