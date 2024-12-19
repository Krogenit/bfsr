package net.bfsr.engine.renderer;

import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.primitive.AbstractVAO;
import net.bfsr.engine.renderer.primitive.GeometryBuffer;
import org.joml.Vector4f;

import java.nio.IntBuffer;
import java.util.concurrent.Future;

public abstract class AbstractSpriteRenderer implements GeometryBuffer {
    static final int VERTEX_DATA_SIZE = 4;
    public static final int MODEL_DATA_SIZE = 6;
    public static final int MODEL_DATA_SIZE_IN_BYTES = MODEL_DATA_SIZE << 2;
    public static final int COMMAND_SIZE = 5;
    public static final int MATERIAL_DATA_SIZE = 16;
    public static final int LAST_UPDATE_MATERIAL_DATA_SIZE = 8;
    public static final int MATERIAL_DATA_SIZE_IN_BYTES = MATERIAL_DATA_SIZE << 2;
    public static final int LAST_UPDATE_MATERIAL_DATA_SIZE_IN_BYTES = LAST_UPDATE_MATERIAL_DATA_SIZE << 2;

    public static final int QUAD_INDEX_COUNT = 6;

    public static final int FOUR_BYTES_ELEMENT_SHIFT = 2;

    public static final int CENTERED_QUAD_BASE_VERTEX = 0;
    public static final int SIMPLE_QUAD_BASE_VERTEX = 4;

    public static final int INSTANCE_COUNT_OFFSET = 1;
    public static final int FIRST_INDEX_OFFSET = 2;
    public static final int BASE_VERTEX_OFFSET = 3;
    public static final int BASE_INSTANCE_OFFSET = 4;

    public abstract AbstractVAO createVAO();

    public abstract AbstractBuffersHolder[] createBuffersHolderArray(int length);
    public abstract AbstractBuffersHolder createBuffersHolder(int capacity);

    public abstract void updateBuffers();
    public abstract void updateBuffers(AbstractBuffersHolder[] buffersHolderArray);

    public abstract void addTask(Runnable runnable, BufferType bufferType);
    public abstract Future<?> addTask(Runnable runnable);

    public abstract void addDrawCommand(IntBuffer commandBuffer, int count, BufferType bufferType);
    public abstract void addDrawCommand(IntBuffer commandBuffer, int count, AbstractBuffersHolder buffersHolder);
    public abstract void addDrawCommand(int id, BufferType bufferType);
    public abstract void addDrawCommand(int id, int baseVertex, BufferType bufferType);
    public abstract void addDrawCommand(int id, int baseVertex, AbstractBuffersHolder buffersHolder);
    public abstract void setIndexCount(int id, int count, AbstractBuffersHolder buffersHolder);

    public abstract void syncAndRender(BufferType bufferType);
    public abstract void render(BufferType bufferType);
    public abstract void render(int mode, int objectCount, AbstractBuffersHolder buffersHolder);
    public abstract void render(int objectCount, AbstractBuffersHolder buffersHolder);

    public abstract int add(float x, float y, float width, float height, float r, float g, float b, float a,
                            long textureHandle, BufferType bufferType);
    public abstract int add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle,
                            float zoomFactor, BufferType bufferType);
    public abstract int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                            long textureHandle, BufferType bufferType);
    public abstract int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                            long textureHandle, long maskTextureHandle, BufferType bufferType);
    public abstract int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                            long textureHandle, AbstractBuffersHolder buffersHolder);
    public abstract int add(float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle, int font,
                            AbstractBuffersHolder buffersHolder);
    public abstract int add(float x, float y, float sin, float cos, float width, float height, float r, float g, float b, float a,
                            long textureHandle, int font, AbstractBuffersHolder buffersHolder);

    public abstract void addMaterialData(float r, float g, float b, float a, long textureHandle, int font, int offset,
                                         AbstractBuffersHolder buffersHolder);
    public abstract void addModelData(float x, float y, float sin, float cos, float width, float height, int offset,
                                      AbstractBuffersHolder buffersHolder);

    public abstract void setPosition(int id, BufferType bufferType, float x, float y);
    public abstract void setPosition(int id, AbstractBuffersHolder buffersHolder, float x, float y);
    public abstract void setRotation(int id, BufferType bufferType, float sin, float cos);
    public abstract void setRotation(int id, AbstractBuffersHolder buffersHolder, float sin, float cos);
    public abstract void setSize(int id, BufferType bufferType, float width, float height);
    public abstract void setSize(int id, AbstractBuffersHolder buffersHolder, float width, float height);
    public abstract void setColor(int id, BufferType bufferType, Vector4f color);
    public abstract void setColor(int id, AbstractBuffersHolder buffersHolder, Vector4f color);
    public abstract void setColorAlpha(int id, BufferType bufferType, float a);
    public abstract void setColorAlpha(int id, AbstractBuffersHolder buffersHolder, float a);
    public abstract void setTexture(int id, BufferType bufferType, long textureHandle);
    public abstract void setTexture(int id, AbstractBuffersHolder buffersHolder, long textureHandle);
    public abstract void setFireAmount(int id, BufferType bufferType, float value);
    public abstract void setFireUVAnimation(int id, BufferType bufferType, float value);
    public abstract void setZoomFactor(int id, BufferType bufferType, float value);

    public abstract void setLastPosition(int id, BufferType bufferType, float x, float y);
    public abstract void setLastPosition(int id, AbstractBuffersHolder buffersHolder, float x, float y);
    public abstract void setLastRotation(int id, BufferType bufferType, float sin, float cos);
    public abstract void setLastRotation(int id, AbstractBuffersHolder buffersHolder, float sin, float cos);
    public abstract void setLastSize(int id, BufferType bufferType, float width, float height);
    public abstract void setLastSize(int id, AbstractBuffersHolder buffersHolder, float width, float height);
    public abstract void setLastColor(int id, BufferType bufferType, Vector4f color);
    public abstract void setLastColor(int id, AbstractBuffersHolder buffersHolder, Vector4f color);
    public abstract void setLastColorAlpha(int id, BufferType bufferType, float a);
    public abstract void setLastColorAlpha(int id, AbstractBuffersHolder buffersHolder, float a);
    public abstract void setLastFireAmount(int id, BufferType bufferType, float value);
    public abstract void setLastFireUVAnimation(int id, BufferType bufferType, float value);

    public abstract AbstractBuffersHolder getBuffersHolder(BufferType bufferType);

    public abstract void removeObject(int id, BufferType bufferType);

    public abstract void clear();
}