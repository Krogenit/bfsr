package net.bfsr.engine.renderer.buffer;

import net.bfsr.engine.renderer.primitive.AbstractVAO;

public interface AbstractBuffersHolder {
    void checkBuffersSize(int objectCount);

    void updateBuffers(int modelBufferIndex, int materialBufferIndex, int lastUpdateModelBufferIndex,
                       int lastUpdateMaterialBufferIndex);

    void markAllBuffersDirty();

    void putModelData(int offset, float value);
    void putMaterialData(int offset, float value);
    void putMaterialData(int offset, int value);
    void putMaterialData(int offset, long value);
    void putCommandData(int offset, int value);
    void putLastUpdateModelData(int offset, float value);
    void putLastUpdateMaterialData(int offset, float value);
    void putLastUpdateMaterialData(int offset, int value);

    void setModelBufferDirty(boolean value);
    void setMaterialBufferDirty(boolean value);
    void setLastUpdateModelBufferDirty(boolean value);
    void setLastUpdateMaterialBufferDirty(boolean value);

    void updateCommandBuffer(int count);
    void bindCommandBuffer();
    void bindCommandBufferBase(int target, int index);
    long getCommandBufferAddress();

    void lockRange();
    void waitForLockedRange();
    void switchRenderingIndex();

    void setRenderObjects(int count);
    int getAndIncrementRenderObjects();
    int getAndIncrementRenderObjects(int count);
    int getRenderObjects();
    AbstractVAO getVao();
    int getNextBaseInstanceId();

    void clear();
}