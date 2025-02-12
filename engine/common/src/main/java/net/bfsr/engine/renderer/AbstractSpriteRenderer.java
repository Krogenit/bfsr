package net.bfsr.engine.renderer;

import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.primitive.AbstractVAO;
import net.bfsr.engine.renderer.primitive.GeometryBuffer;
import org.joml.Vector4f;

import java.util.concurrent.Future;

public interface AbstractSpriteRenderer extends GeometryBuffer {
    int VERTEX_DATA_SIZE = 4;
    int MODEL_DATA_SIZE = 6;
    int MODEL_DATA_SIZE_IN_BYTES = MODEL_DATA_SIZE << 2;
    int COMMAND_SIZE = 5;
    int COMMAND_SIZE_IN_BYTES = COMMAND_SIZE << 2;
    int MATERIAL_DATA_SIZE = 12;
    int LAST_UPDATE_MATERIAL_DATA_SIZE = 8;
    int MATERIAL_DATA_SIZE_IN_BYTES = MATERIAL_DATA_SIZE << 2;
    int LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES = LAST_UPDATE_MATERIAL_DATA_SIZE << 2;

    int QUAD_INDEX_COUNT = 6;

    int FOUR_BYTES_ELEMENT_SHIFT = 2;

    int CENTERED_QUAD_BASE_VERTEX = 0;
    int SIMPLE_QUAD_BASE_VERTEX = 4;

    int INSTANCE_COUNT_OFFSET = 4;
    int FIRST_INDEX_OFFSET = 8;
    int BASE_VERTEX_OFFSET = 12;
    int BASE_INSTANCE_OFFSET = 16;

    void init(AbstractRenderer renderer);

    AbstractVAO createVAO();

    AbstractBuffersHolder[] createBuffersHolderArray(int length);
    AbstractBuffersHolder createBuffersHolder(int capacity, boolean persistent);

    void updateBuffers();
    void updateBuffers(AbstractBuffersHolder[] buffersHolderArray);
    void waitForLockedRange();
    void waitForLockedRange(AbstractBuffersHolder[] buffersHolderArray);

    void addTask(Runnable runnable, BufferType bufferType);
    Future<?> addTask(Runnable runnable);

    void addDrawCommand(long commandBufferAddress, int count, BufferType bufferType);
    void addDrawCommand(long commandBufferAddress, int count, AbstractBuffersHolder buffersHolder);
    void addDrawCommand(int id, BufferType bufferType);
    void addDrawCommand(int id, int baseVertex, BufferType bufferType);
    void addDrawCommand(int id, int baseVertex, AbstractBuffersHolder buffersHolder);
    void setIndexCount(int id, int count, AbstractBuffersHolder buffersHolder);

    void syncAndRender(BufferType bufferType);
    void render(BufferType bufferType);
    void render(int objectCount, AbstractBuffersHolder buffersHolder);
    void updateCommandBufferAndRender(int mode, int renderObjects, AbstractBuffersHolder buffersHolder);
    void render(int mode, int objectCount, AbstractBuffersHolder buffersHolder);

    int add(float x, float y, float width, float height, float r, float g, float b, float a, BufferType bufferType);
    int add(float x, float y, float width, float height, float r, float g, float b, float a,
            long textureHandle, BufferType bufferType);
    int add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle,
            float zoomFactor, BufferType bufferType);
    int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a, long textureHandle,
            BufferType bufferType);
    int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a, long textureHandle,
            MaterialType materialType, BufferType bufferType);
    int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
            long textureHandle, long maskTextureHandle, BufferType bufferType);
    int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a, long textureHandle,
            long maskTextureHandle, MaterialType materialType, BufferType bufferType);
    int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
            long textureHandle, AbstractBuffersHolder buffersHolder);
    int add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle, MaterialType materialType,
            AbstractBuffersHolder buffersHolder);
    int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
            long textureHandle, MaterialType materialType, AbstractBuffersHolder buffersHolder);

    void addMaterialData(float r, float g, float b, float a, long textureHandle, MaterialType materialType, int offset,
                         AbstractBuffersHolder buffersHolder);
    void addModelData(float x, float y, float sin, float cos, float width, float height, int offset,
                      AbstractBuffersHolder buffersHolder);

    void setPosition(int id, BufferType bufferType, float x, float y);
    void setPosition(int id, AbstractBuffersHolder buffersHolder, float x, float y);
    void setRotation(int id, BufferType bufferType, float sin, float cos);
    void setRotation(int id, AbstractBuffersHolder buffersHolder, float sin, float cos);
    void setSize(int id, BufferType bufferType, float width, float height);
    void setSize(int id, AbstractBuffersHolder buffersHolder, float width, float height);
    void setColor(int id, BufferType bufferType, Vector4f color);
    void setColor(int id, AbstractBuffersHolder buffersHolder, Vector4f color);
    void setColorAlpha(int id, BufferType bufferType, float a);
    void setColorAlpha(int id, AbstractBuffersHolder buffersHolder, float a);
    void setTexture(int id, BufferType bufferType, long textureHandle);
    void setTexture(int id, AbstractBuffersHolder buffersHolder, long textureHandle);
    void setFireAmount(int id, BufferType bufferType, float value);
    void setFireUVAnimation(int id, BufferType bufferType, float value);
    void setZoomFactor(int id, BufferType bufferType, float value);

    void setLastPosition(int id, BufferType bufferType, float x, float y);
    void setLastPosition(int id, AbstractBuffersHolder buffersHolder, float x, float y);
    void setLastRotation(int id, BufferType bufferType, float sin, float cos);
    void setLastRotation(int id, AbstractBuffersHolder buffersHolder, float sin, float cos);
    void setLastSize(int id, BufferType bufferType, float width, float height);
    void setLastSize(int id, AbstractBuffersHolder buffersHolder, float width, float height);
    void setLastColor(int id, BufferType bufferType, Vector4f color);
    void setLastColor(int id, AbstractBuffersHolder buffersHolder, Vector4f color);
    void setLastColorAlpha(int id, BufferType bufferType, float a);
    void setLastColorAlpha(int id, AbstractBuffersHolder buffersHolder, float a);
    void setLastFireAmount(int id, BufferType bufferType, float value);
    void setLastFireUVAnimation(int id, BufferType bufferType, float value);

    void setPersistentMappedBuffers(boolean value);

    AbstractBuffersHolder getBuffersHolder(BufferType bufferType);

    void removeObject(int id, BufferType bufferType);

    void clear();
}