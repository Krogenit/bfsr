package net.bfsr.client.renderer.instanced;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.primitive.VAO;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import net.bfsr.util.MultithreadingUtils;
import net.bfsr.util.MutableInt;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL11C.GL_QUADS;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL44C.GL_DYNAMIC_STORAGE_BIT;

public class SpriteRenderer {
    public static final int VERTEX_DATA_SIZE = 4;
    public static final int VERTEX_DATA_SIZE_IN_BYTES = VERTEX_DATA_SIZE << 2;
    public static final int MATERIAL_DATA_SIZE = 12;
    public static final int MATERIAL_DATA_SIZE_IN_BYTES = MATERIAL_DATA_SIZE << 2;

    private VAO vao;
    private ExecutorService executorService;
    @Getter
    final BuffersHolder[] buffersHolders = new BuffersHolder[BufferType.values().length];
    private final BiConsumer<Runnable, BufferType> addTaskConsumer;

    public SpriteRenderer() {
        buffersHolders[BufferType.BACKGROUND.ordinal()] = new BuffersHolder(1);
        buffersHolders[BufferType.ENTITIES_ALPHA.ordinal()] = new BuffersHolder(512);
        buffersHolders[BufferType.ENTITIES_ADDITIVE.ordinal()] = new BuffersHolder(512);
        buffersHolders[BufferType.GUI.ordinal()] = new BuffersHolder(512);

        if (MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            executorService = Executors.newFixedThreadPool(MultithreadingUtils.PARALLELISM);
            addTaskConsumer = (runnable, bufferType) -> buffersHolders[bufferType.ordinal()].setFuture(executorService.submit(runnable));
        } else {
            addTaskConsumer = (runnable, bufferType) -> runnable.run();
        }
    }

    public void addTask(Runnable runnable, BufferType bufferType) {
        addTaskConsumer.accept(runnable, bufferType);
    }

    public Future<?> addTask(Runnable runnable) {
        return executorService.submit(runnable);
    }

    public void init() {
        vao = VAO.create(2);
        vao.createVertexBuffers();
        vao.attributeBindingAndFormat(0, 4, 0, 0);
        vao.enableAttributes(1);
    }

    public void bind() {
        vao.bind();
    }

    public void syncAndRender(BufferType bufferType) {
        syncAndRender(GL_QUADS, bufferType);
    }

    public void syncAndRender(int type, BufferType bufferType) {
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

    public void render(BufferType bufferType) {
        render(GL_QUADS, bufferType);
    }

    public void render(int type, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        if (buffersHolder.getObjectCount() > 0) {
            render(type, buffersHolder.getObjectCount(), buffersHolder.getVertexBuffer(), buffersHolder.getMaterialBuffer());
            buffersHolder.reset();
        }
    }

    public void render(int count, FloatBuffer vertexBuffer, ByteBuffer materialBuffer) {
        render(GL_QUADS, count, vertexBuffer, materialBuffer);
    }

    public void render(int type, int count, FloatBuffer vertexBuffer, ByteBuffer materialBuffer) {
        vao.updateVertexBuffer(0, vertexBuffer.limit(count * VERTEX_DATA_SIZE_IN_BYTES), GL_DYNAMIC_STORAGE_BIT, VERTEX_DATA_SIZE_IN_BYTES);
        vao.updateBuffer(1, materialBuffer.limit(count * MATERIAL_DATA_SIZE_IN_BYTES), GL_DYNAMIC_STORAGE_BIT);
        vao.bindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 1);
        glDrawArrays(type, 0, count << 2);
        Core.get().getRenderer().increaseDrawCalls();
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float lastScaleX, float lastScaleY,
                                          float scaleX, float scaleY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, lastScaleX, lastScaleY, scaleX, scaleY, r, g, b, a, texture,
                Core.get().getRenderer().getInterpolation(), buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float lastScaleX, float lastScaleY,
                                          float scaleX, float scaleY, float r, float g, float b, float a, Texture texture, DamageMaskTexture maskTexture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(1);
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, lastScaleX, lastScaleY, scaleX, scaleY, r, g, b, a, texture, maskTexture,
                Core.get().getRenderer().getInterpolation(), buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(1);
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, DamageMaskTexture maskTexture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(1);
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, scaleX, scaleY, r, g, b, a, texture, maskTexture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float scaleX, float scaleY,
                                          Vector4f lastColor, Vector4f color, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, scaleX, scaleY, lastColor, color, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float sin, float cos, float scaleX, float scaleY,
                                          float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(1);
        addToRenderPipeLineSinCos(lastX, lastY, x, y, sin, cos, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                    float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        add(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float scaleX, float scaleY,
                    float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        add(lastX, lastY, x, y, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float rotation, float scaleX, float scaleY,
                    float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        add(lastX, lastY, x, y, rotation, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                    float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, interpolation, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, materialBuffer, materialBufferIndex);
    }

    public void add(float lastX, float lastY, float x, float y, float scaleX, float scaleY,
                    float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, 0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, materialBuffer, materialBufferIndex);
    }

    public void add(float lastX, float lastY, float x, float y, float rotation, float scaleX, float scaleY,
                    float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, LUT.sin(rotation), LUT.cos(rotation), 0.5f * scaleX, 0.5f * scaleY, vertexBuffer,
                vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float lastScaleX, float lastScaleY, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation), 0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation), vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float lastScaleX, float lastScaleY,
                                          float scaleX, float scaleY, float r, float g, float b, float a, Texture texture, DamageMaskTexture maskTexture, float interpolation,
                                          FloatBuffer vertexBuffer, MutableInt vertexBufferIndex, ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation), 0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation), vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(maskTexture.getTextureHandle(), maskTexture.getFireAmount(interpolation), maskTexture.getFireUVAnimation(interpolation), materialBuffer,
                materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, DamageMaskTexture maskTexture, float interpolation,
                                          FloatBuffer vertexBuffer, MutableInt vertexBufferIndex, ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(maskTexture.getTextureHandle(), maskTexture.getFireAmount(interpolation), maskTexture.getFireUVAnimation(interpolation), materialBuffer,
                materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float scaleX,
                                          float scaleY, Vector4f lastColor, Vector4f color, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float sin, float cos, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, sin, cos, 0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
        putMaterialData(0, 0.0f, 0.0f, materialBuffer, materialBufferIndex);
    }

    public void add(float x, float y, float scaleX, float scaleY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        putVerticesCentered(x, y, scaleX * 0.5f, scaleY * 0.5f, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putTextureHandle(texture.getTextureHandle(), buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void add(float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        putVerticesCentered(x, y, LUT.sin(rotation), LUT.cos(rotation), scaleX * 0.5f, scaleY * 0.5f, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putTextureHandle(texture.getTextureHandle(), buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putMaterialData(0, 0.0f, 0.0f, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void putVertices(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                            float interpolation, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation;
        putVerticesCentered(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, LUT.sin(interpolatedRotation), LUT.cos(interpolatedRotation),
                0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation), 0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation), floatBuffer, bufferIndex);
    }

    void putVerticesCentered(float x, float y, float sizeX, float sizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        putVertices(-sizeX + x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, -sizeY + y, -sizeX + x, -sizeY + y, floatBuffer, bufferIndex);
    }

    void putVertices(float x, float y, float sin, float cos, float halfSizeX, float halfSizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float sinSizeX = sin * halfSizeX;
        final float cosSizeX = cos * halfSizeX;
        final float sinSizeY = sin * halfSizeY;
        final float cosSizeY = cos * halfSizeY;

        final float x1 = -cosSizeX - sinSizeY + x + halfSizeX;
        final float x2 = cosSizeX - sinSizeY + x + halfSizeX;
        final float x3 = cosSizeX + sinSizeY + x + halfSizeX;
        final float y3 = sinSizeX - cosSizeY + y + halfSizeY;
        final float y1 = -sinSizeX + cosSizeY + y + halfSizeY;
        final float y2 = sinSizeX + cosSizeY + y + halfSizeY;

        putVertices(x1, y1, x2, y2, x3, y3, x1 + (x3 - x2), y3 - (y2 - y1), floatBuffer, bufferIndex);
    }

    void putVerticesCentered(float x, float y, float sin, float cos, float halfSizeX, float halfSizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float sinSizeX = sin * halfSizeX;
        final float cosSizeX = cos * halfSizeX;
        final float sinSizeY = sin * halfSizeY;
        final float cosSizeY = cos * halfSizeY;

        final float x1 = -cosSizeX - sinSizeY + x;
        final float x2 = cosSizeX - sinSizeY + x;
        final float x3 = cosSizeX + sinSizeY + x;
        final float y3 = sinSizeX - cosSizeY + y;
        final float y1 = -sinSizeX + cosSizeY + y;
        final float y2 = sinSizeX + cosSizeY + y;

        putVertices(x1, y1, x2, y2, x3, y3, x1 + (x3 - x2), y3 - (y2 - y1), floatBuffer, bufferIndex);
    }

    void putVertices(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;

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

    void putColor(float r, float g, float b, float a, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), r);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), g);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), b);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), a);
    }

    public void putColor(Vector4f lastColor, Vector4f color, ByteBuffer byteBuffer, MutableInt index, float interpolation) {
        byteBuffer.putFloat(index.getAndAdd(4), color.x);
        byteBuffer.putFloat(index.getAndAdd(4), color.y);
        byteBuffer.putFloat(index.getAndAdd(4), color.z);
        byteBuffer.putFloat(index.getAndAdd(4), lastColor.w + (color.w - lastColor.w) * interpolation);
    }

    public void putTextureHandle(long textureHandle, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        byteBuffer.putLong(bufferIndex.getAndAdd(8), textureHandle);
        byteBuffer.putInt(bufferIndex.getAndAdd(4), textureHandle != 0 ? 1 : 0);
    }

    public void putMaterialData(long maskTextureHandle, float fireAmount, float fireUVAnimation, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        byteBuffer.putInt(bufferIndex.getAndAdd(4), maskTextureHandle != 0 ? 1 : 0);
        byteBuffer.putLong(bufferIndex.getAndAdd(8), maskTextureHandle);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), fireAmount);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), fireUVAnimation);
    }

    public static SpriteRenderer get() {
        return Core.get().getRenderer().getSpriteRenderer();
    }

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