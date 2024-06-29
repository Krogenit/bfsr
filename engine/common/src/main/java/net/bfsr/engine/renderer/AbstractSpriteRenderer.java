package net.bfsr.engine.renderer;

import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.texture.AbstractDamageMaskTexture;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.util.MutableInt;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.Future;

public abstract class AbstractSpriteRenderer {
    private static final int VERTEX_DATA_SIZE = 4;
    public static final int VERTEX_DATA_SIZE_IN_BYTES = VERTEX_DATA_SIZE << 2;
    private static final int MATERIAL_DATA_SIZE = 12;
    public static final int MATERIAL_DATA_SIZE_IN_BYTES = MATERIAL_DATA_SIZE << 2;

    public abstract void init();

    public abstract void bind();

    public abstract void clear();

    public abstract void addTask(Runnable runnable, BufferType bufferType);
    public abstract Future<?> addTask(Runnable runnable);

    public abstract void syncAndRender(BufferType bufferType);
    public abstract void render(BufferType bufferType);
    public abstract void render(int mode, int objectCount, FloatBuffer vertexBuffer, ByteBuffer materialBuffer);
    public abstract void render(int objectCount, FloatBuffer vertexBuffer, ByteBuffer materialBuffer);

    public abstract AbstractBuffersHolder getBuffersHolder(BufferType bufferType);

    public abstract void add(float lastX, float lastY, float x, float y, float scaleX, float scaleY, float r, float g, float b,
                             float a, AbstractTexture texture, BufferType bufferType);
    public abstract void add(float x, float y, float scaleX, float scaleY, float r, float g, float b, float a,
                             AbstractTexture texture, BufferType bufferType);

    public abstract void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos,
                                                   float sin, float cos, float lastScaleX, float lastScaleY, float scaleX,
                                                   float scaleY, float r, float g, float b, float a, AbstractTexture texture,
                                                   BufferType bufferType);
    public abstract void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos,
                                                   float sin, float cos, float scaleX, float scaleY, float r, float g, float b,
                                                   float a, AbstractTexture texture, BufferType bufferType);
    public abstract void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float sin, float cos, float scaleX,
                                                   float scaleY, float r, float g, float b, float a, AbstractTexture texture,
                                                   BufferType bufferType);
    public abstract void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos,
                                                   float sin, float cos, float scaleX, float scaleY, float r, float g, float b,
                                                   float a, AbstractTexture texture, AbstractDamageMaskTexture maskTexture,
                                                   BufferType bufferType);
    public abstract void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos,
                                                   float sin, float cos, float scaleX, float scaleY, Vector4f lastColor,
                                                   Vector4f color, AbstractTexture texture, BufferType bufferType);

    public abstract void putVertices(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                                     FloatBuffer floatBuffer, MutableInt bufferIndex);
    public abstract void putVerticesClockWise(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                                              FloatBuffer floatBuffer, MutableInt bufferIndex);
    public abstract void putVertices(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin,
                                     float cos, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                                     float interpolation, FloatBuffer floatBuffer, MutableInt bufferIndex);
    public abstract void putVerticesCentered(float x, float y, float sin, float cos, float halfSizeX, float halfSizeY,
                                             FloatBuffer floatBuffer, MutableInt bufferIndex);
    public abstract void putVerticesCenteredClockWise(float x, float y, float sin, float cos, float halfSizeX, float halfSizeY,
                                                      FloatBuffer floatBuffer, MutableInt bufferIndex);
    public abstract void putVerticesCentered(float x, float y, float sizeX, float sizeY, FloatBuffer floatBuffer,
                                             MutableInt bufferIndex);
    public abstract void putVerticesCenteredClockWise(float x, float y, float sizeX, float sizeY, FloatBuffer floatBuffer,
                                                      MutableInt bufferIndex);


    public abstract void putColor(float r, float g, float b, float a, ByteBuffer byteBuffer, MutableInt bufferIndex);
    public abstract void putColor(Vector4f lastColor, Vector4f color, ByteBuffer byteBuffer, MutableInt index,
                                  float interpolation);
    public abstract void putTextureHandle(long textureHandle, ByteBuffer byteBuffer, MutableInt bufferIndex);
    public abstract void putMaterialData(long maskTextureHandle, float fireAmount, float fireUVAnimation, ByteBuffer byteBuffer,
                                         MutableInt bufferIndex);
}