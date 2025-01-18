package net.bfsr.engine.renderer.buffer;

import net.bfsr.engine.renderer.primitive.AbstractVAO;

import java.nio.IntBuffer;

public abstract class AbstractBuffersHolder {
    public abstract void checkBuffersSize(int objectCount);

    public abstract void updateBuffers(int modelBufferIndex, int materialBufferIndex, int lastUpdateModelBufferIndex,
                                       int lastUpdateMaterialBufferIndex);

    public abstract void markAllBuffersDirty();

    public abstract void putModelData(int offset, float value);
    public abstract void putMaterialData(int offset, float value);
    public abstract void putMaterialData(int offset, int value);
    public abstract void putMaterialData(int offset, long value);
    public abstract void putCommandData(int offset, int value);
    public abstract void putLastUpdateModelData(int offset, float value);
    public abstract void putLastUpdateMaterialData(int offset, float value);
    public abstract void putLastUpdateMaterialData(int offset, int value);

    public abstract void setModelBufferDirty(boolean value);
    public abstract void setMaterialBufferDirty(boolean value);
    public abstract void setLastUpdateModelBufferDirty(boolean value);
    public abstract void setLastUpdateMaterialBufferDirty(boolean value);

    public abstract IntBuffer getCommandBuffer();

    public abstract long getCommandBufferAddress();

    public abstract void setRenderObjects(int count);
    public abstract int getAndIncrementRenderObjects();
    public abstract int getAndIncrementRenderObjects(int count);
    public abstract int getRenderObjects();
    public abstract AbstractVAO getVao();
    public abstract int getNextBaseInstanceId();

    public abstract void clear();
}