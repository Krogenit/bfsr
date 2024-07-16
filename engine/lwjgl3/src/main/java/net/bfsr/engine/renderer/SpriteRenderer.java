package net.bfsr.engine.renderer;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.buffer.BuffersHolder;
import net.bfsr.engine.renderer.primitive.VAO;
import net.bfsr.engine.renderer.texture.AbstractDamageMaskTexture;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.util.MultithreadingUtils;
import net.bfsr.engine.util.MutableInt;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL11C.GL_QUADS;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL44C.GL_DYNAMIC_STORAGE_BIT;

public class SpriteRenderer extends AbstractSpriteRenderer {
    private AbstractRenderer renderer;
    private VAO vao;
    private ExecutorService executorService;
    @Getter
    final BuffersHolder[] buffersHolders = new BuffersHolder[BufferType.values().length];
    private final BiConsumer<Runnable, BufferType> addTaskConsumer;

    SpriteRenderer() {
        buffersHolders[BufferType.BACKGROUND.ordinal()] = new BuffersHolder(1);
        buffersHolders[BufferType.ENTITIES_ALPHA.ordinal()] = new BuffersHolder(512);
        buffersHolders[BufferType.ENTITIES_ADDITIVE.ordinal()] = new BuffersHolder(512);
        buffersHolders[BufferType.GUI.ordinal()] = new BuffersHolder(512);

        if (MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            executorService = Executors.newFixedThreadPool(MultithreadingUtils.PARALLELISM);
            addTaskConsumer = (runnable, bufferType) -> buffersHolders[bufferType.ordinal()].setFuture(
                    executorService.submit(runnable));
        } else {
            addTaskConsumer = (runnable, bufferType) -> runnable.run();
        }
    }

    @Override
    public void addTask(Runnable runnable, BufferType bufferType) {
        addTaskConsumer.accept(runnable, bufferType);
    }

    @Override
    public Future<?> addTask(Runnable runnable) {
        return executorService.submit(runnable);
    }

    @Override
    public void init() {
        renderer = Engine.renderer;

        vao = VAO.create(2);
        vao.createVertexBuffers();
        vao.attributeBindingAndFormat(0, 4, 0, 0);
        vao.enableAttributes(1);
    }

    @Override
    public void bind() {
        vao.bind();
    }

    @Override
    public void syncAndRender(BufferType bufferType) {
        syncAndRender(GL_QUADS, bufferType);
    }

    private void syncAndRender(int type, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];

        try {
            buffersHolder.getFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (buffersHolder.getObjectCount() > 0) {
            render(type, buffersHolder.getObjectCount(), buffersHolder.getVertexBuffer(), buffersHolder.getMaterialBuffer());
            buffersHolder.reset();
        }
    }

    @Override
    public void render(BufferType bufferType) {
        render(GL_QUADS, bufferType);
    }

    public void render(int mode, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        if (buffersHolder.getObjectCount() > 0) {
            render(mode, buffersHolder.getObjectCount(), buffersHolder.getVertexBuffer(), buffersHolder.getMaterialBuffer());
            buffersHolder.reset();
        }
    }

    @Override
    public void render(int objectCount, FloatBuffer vertexBuffer, ByteBuffer materialBuffer) {
        render(GL_QUADS, objectCount, vertexBuffer, materialBuffer);
    }

    @Override
    public void render(int mode, int objectCount, FloatBuffer vertexBuffer, ByteBuffer materialBuffer) {
        vao.updateVertexBuffer(0, vertexBuffer.limit(objectCount * VERTEX_DATA_SIZE_IN_BYTES), GL_DYNAMIC_STORAGE_BIT,
                VERTEX_DATA_SIZE_IN_BYTES);
        vao.updateBuffer(1, materialBuffer.limit(objectCount * MATERIAL_DATA_SIZE_IN_BYTES), GL_DYNAMIC_STORAGE_BIT);
        vao.bindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 1);
        glDrawArrays(mode, 0, objectCount << 2);
        renderer.increaseDrawCalls();
    }

    @Override
    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin,
                                          float cos, float lastScaleX, float lastScaleY,
                                          float scaleX, float scaleY, float r, float g, float b, float a, AbstractTexture texture,
                                          BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, lastScaleX, lastScaleY, scaleX, scaleY, r, g, b,
                a, texture, renderer.getInterpolation(), buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(),
                buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    @Override
    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin,
                                          float cos, float scaleX,
                                          float scaleY, float r, float g, float b, float a, AbstractTexture texture,
                                          BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(1);
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, scaleX, scaleY, r, g, b, a, texture,
                renderer.getInterpolation(), buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(),
                buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    @Override
    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin,
                                          float cos, float scaleX, float scaleY, float r, float g, float b, float a,
                                          AbstractTexture texture, AbstractDamageMaskTexture maskTexture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(1);
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, scaleX, scaleY, r, g, b, a, texture, maskTexture,
                renderer.getInterpolation(), buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(),
                buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    @Override
    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos,
                                          float scaleX, float scaleY, Vector4f lastColor, Vector4f color, AbstractTexture texture,
                                          BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, scaleX, scaleY, lastColor, color, texture,
                renderer.getInterpolation(), buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(),
                buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    @Override
    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float sin, float cos, float scaleX, float scaleY,
                                          float r, float g, float b, float a, AbstractTexture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(1);
        addToRenderPipeLineSinCos(lastX, lastY, x, y, sin, cos, scaleX, scaleY, r, g, b, a, texture, renderer.getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    @Override
    public void add(float lastX, float lastY, float x, float y, float scaleX, float scaleY, float r, float g, float b, float a,
                    AbstractTexture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        add(lastX, lastY, x, y, scaleX, scaleY, r, g, b, a, texture, renderer.getInterpolation(), buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float scaleX, float scaleY, float r, float g, float b, float a,
                    AbstractTexture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, 0.5f * scaleX, 0.5f * scaleY,
                vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, 0, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos,
                                          float lastScaleX, float lastScaleY, float scaleX, float scaleY, float r, float g, float b,
                                          float a, AbstractTexture texture, float interpolation, FloatBuffer vertexBuffer,
                                          MutableInt vertexBufferIndex, ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation,
                lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation),
                0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation), vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, 0, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos,
                                          float scaleX, float scaleY, float r, float g, float b, float a, AbstractTexture texture,
                                          float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation,
                lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, 0, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos,
                                          float scaleX, float scaleY, float r, float g, float b, float a, AbstractTexture texture,
                                          AbstractDamageMaskTexture maskTexture, float interpolation, FloatBuffer vertexBuffer,
                                          MutableInt vertexBufferIndex, ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation,
                lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(maskTexture.getTextureHandle(), maskTexture.getFireAmount(interpolation),
                maskTexture.getFireUVAnimation(interpolation), 0, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin,
                                          float cos, float scaleX, float scaleY, Vector4f lastColor, Vector4f color,
                                          AbstractTexture texture, float interpolation, FloatBuffer vertexBuffer,
                                          MutableInt vertexBufferIndex, ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation,
                lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, 0, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float sin, float cos, float scaleX, float scaleY,
                                          float r, float g, float b, float a, AbstractTexture texture, float interpolation,
                                          FloatBuffer vertexBuffer, MutableInt vertexBufferIndex, ByteBuffer materialBuffer,
                                          MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, sin, cos, 0.5f * scaleX,
                0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, 0, materialBuffer, materialBufferIndex);
    }

    @Override
    public void add(float x, float y, float scaleX, float scaleY, float r, float g, float b, float a, AbstractTexture texture,
                    BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        putVerticesCentered(x, y, scaleX * 0.5f, scaleY * 0.5f, buffersHolder.getVertexBuffer(),
                buffersHolder.getVertexBufferIndex());
        putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putTextureHandle(texture.getTextureHandle(), buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putMaterialData(0, 0.0f, 0.0f, 0, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    @Override
    public void putVertices(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos,
                            float lastScaleX, float lastScaleY, float scaleX, float scaleY, float interpolation, FloatBuffer floatBuffer,
                            MutableInt bufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation,
                lastSin + (sin - lastSin) * interpolation,
                lastCos + (cos - lastCos) * interpolation, 0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation),
                0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation), floatBuffer, bufferIndex);
    }

    @Override
    public void putVerticesCentered(float x, float y, float sizeX, float sizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        putVertices(-sizeX + x, -sizeY + y, sizeX + x, -sizeY + y, sizeX + x, sizeY + y, -sizeX + x, sizeY + y, floatBuffer,
                bufferIndex);
    }

    @Override
    public void putVerticesCenteredClockWise(float x, float y, float sizeX, float sizeY, FloatBuffer floatBuffer,
                                             MutableInt bufferIndex) {
        putVerticesClockWise(-sizeX + x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, -sizeY + y, -sizeX + x, -sizeY + y,
                floatBuffer, bufferIndex);
    }

    @Override
    public void putVerticesCentered(float x, float y, float sin, float cos, float halfSizeX, float halfSizeY,
                                    FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float sinSizeX = sin * halfSizeX;
        final float cosSizeX = cos * halfSizeX;
        final float sinSizeY = sin * halfSizeY;
        final float cosSizeY = cos * halfSizeY;

        final float x1 = -cosSizeX - sinSizeY + x;
        final float x2 = cosSizeX - sinSizeY + x;
        final float x3 = cosSizeX + sinSizeY + x;
        final float y1 = -sinSizeX + cosSizeY + y;
        final float y2 = sinSizeX + cosSizeY + y;
        final float y3 = sinSizeX - cosSizeY + y;

        putVertices(x1 + (x3 - x2), y3 - (y2 - y1), x3, y3, x2, y2, x1, y1, floatBuffer, bufferIndex);
    }

    @Override
    public void putVerticesCenteredClockWise(float x, float y, float sin, float cos, float halfSizeX, float halfSizeY,
                                             FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float sinSizeX = sin * halfSizeX;
        final float cosSizeX = cos * halfSizeX;
        final float sinSizeY = sin * halfSizeY;
        final float cosSizeY = cos * halfSizeY;

        final float x1 = -cosSizeX - sinSizeY + x;
        final float x2 = cosSizeX - sinSizeY + x;
        final float x3 = cosSizeX + sinSizeY + x;
        final float y1 = -sinSizeX + cosSizeY + y;
        final float y2 = sinSizeX + cosSizeY + y;
        final float y3 = sinSizeX - cosSizeY + y;

        putVerticesClockWise(x1, y1, x2, y2, x3, y3, x1 + (x3 - x2), y3 - (y2 - y1), floatBuffer, bufferIndex);
    }

    @Override
    public void putVertices(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                            FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float u1 = 0.0f;
        final float v1 = 0.0f;
        final float u2 = 1.0f;
        final float v2 = 0.0f;
        final float u3 = 1.0f;
        final float v3 = 1.0f;
        final float u4 = 0.0f;
        final float v4 = 1.0f;

        floatBuffer.put(bufferIndex.getAndIncrement(), x1);
        floatBuffer.put(bufferIndex.getAndIncrement(), y1);
        floatBuffer.put(bufferIndex.getAndIncrement(), u1);
        floatBuffer.put(bufferIndex.getAndIncrement(), v1);
        floatBuffer.put(bufferIndex.getAndIncrement(), x2);
        floatBuffer.put(bufferIndex.getAndIncrement(), y2);
        floatBuffer.put(bufferIndex.getAndIncrement(), u2);
        floatBuffer.put(bufferIndex.getAndIncrement(), v2);
        floatBuffer.put(bufferIndex.getAndIncrement(), x3);
        floatBuffer.put(bufferIndex.getAndIncrement(), y3);
        floatBuffer.put(bufferIndex.getAndIncrement(), u3);
        floatBuffer.put(bufferIndex.getAndIncrement(), v3);
        floatBuffer.put(bufferIndex.getAndIncrement(), x4);
        floatBuffer.put(bufferIndex.getAndIncrement(), y4);
        floatBuffer.put(bufferIndex.getAndIncrement(), u4);
        floatBuffer.put(bufferIndex.getAndIncrement(), v4);
    }

    @Override
    public void putVerticesClockWise(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                                     FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;
        putVertices(x1, y1, x2, y2, x3, y3, x4, y4, u1, v1, u2, v2, u3, v3, u4, v4, floatBuffer, bufferIndex);
    }

    @Override
    public void putVertices(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float u1, float v1,
                            float u2, float v2, float u3, float v3, float u4, float v4, FloatBuffer floatBuffer,
                            MutableInt bufferIndex) {
        floatBuffer.put(bufferIndex.getAndIncrement(), x1);
        floatBuffer.put(bufferIndex.getAndIncrement(), y1);
        floatBuffer.put(bufferIndex.getAndIncrement(), u1);
        floatBuffer.put(bufferIndex.getAndIncrement(), v1);
        floatBuffer.put(bufferIndex.getAndIncrement(), x2);
        floatBuffer.put(bufferIndex.getAndIncrement(), y2);
        floatBuffer.put(bufferIndex.getAndIncrement(), u2);
        floatBuffer.put(bufferIndex.getAndIncrement(), v2);
        floatBuffer.put(bufferIndex.getAndIncrement(), x3);
        floatBuffer.put(bufferIndex.getAndIncrement(), y3);
        floatBuffer.put(bufferIndex.getAndIncrement(), u3);
        floatBuffer.put(bufferIndex.getAndIncrement(), v3);
        floatBuffer.put(bufferIndex.getAndIncrement(), x4);
        floatBuffer.put(bufferIndex.getAndIncrement(), y4);
        floatBuffer.put(bufferIndex.getAndIncrement(), u4);
        floatBuffer.put(bufferIndex.getAndIncrement(), v4);
    }

    @Override
    public void putColor(float r, float g, float b, float a, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), r);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), g);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), b);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), a);
    }

    @Override
    public void putColor(Vector4f lastColor, Vector4f color, ByteBuffer byteBuffer, MutableInt index, float interpolation) {
        byteBuffer.putFloat(index.getAndAdd(4), color.x);
        byteBuffer.putFloat(index.getAndAdd(4), color.y);
        byteBuffer.putFloat(index.getAndAdd(4), color.z);
        byteBuffer.putFloat(index.getAndAdd(4), lastColor.w + (color.w - lastColor.w) * interpolation);
    }

    @Override
    public void putTextureHandle(long textureHandle, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        byteBuffer.putLong(bufferIndex.getAndAdd(8), textureHandle);
        byteBuffer.putInt(bufferIndex.getAndAdd(4), textureHandle != 0 ? 1 : 0);
    }

    @Override
    public void putMaterialData(long maskTextureHandle, float fireAmount, float fireUVAnimation, int font, ByteBuffer byteBuffer,
                                MutableInt bufferIndex) {
        byteBuffer.putInt(bufferIndex.getAndAdd(4), maskTextureHandle != 0 ? 1 : 0);
        byteBuffer.putLong(bufferIndex.getAndAdd(8), maskTextureHandle);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), fireAmount);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), fireUVAnimation);
        byteBuffer.putInt(bufferIndex.getAndAdd(4), font);

        //padding
        byteBuffer.putInt(bufferIndex.getAndAdd(4), 0);
        byteBuffer.putInt(bufferIndex.getAndAdd(4), 0);
        byteBuffer.putInt(bufferIndex.getAndAdd(4), 0);
    }

    @Override
    public BuffersHolder getBuffersHolder(BufferType bufferType) {
        return buffersHolders[bufferType.ordinal()];
    }

    @Override
    public void clear() {
        vao.clear();
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}