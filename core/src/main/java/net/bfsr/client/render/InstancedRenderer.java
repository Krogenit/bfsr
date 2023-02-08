package net.bfsr.client.render;

import lombok.Getter;
import net.bfsr.client.render.font.string.GLString;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import net.bfsr.util.MulthithreadingUtils;
import net.bfsr.util.MutableInt;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class InstancedRenderer {
    public static InstancedRenderer INSTANCE;

    public static final int VERTEX_DATA_SIZE = 16;
    public static final int MATERIAL_DATA_SIZE = 32;

    private VAO vao;
    private ExecutorService executorService;
    @Getter
    private final StoreRenderObjectTask[] storeRenderObjectTasks = new StoreRenderObjectTask[BufferType.values().length];

    public InstancedRenderer() {
        INSTANCE = this;

        storeRenderObjectTasks[BufferType.BACKGROUND.ordinal()] = new StoreRenderObjectTask(1);
        storeRenderObjectTasks[BufferType.ENTITIES_ALPHA.ordinal()] = new StoreRenderObjectTask(512);
        storeRenderObjectTasks[BufferType.ENTITIES_ADDITIVE.ordinal()] = new StoreRenderObjectTask(512);
        storeRenderObjectTasks[BufferType.GUI.ordinal()] = new StoreRenderObjectTask(1024);

        if (MulthithreadingUtils.MULTITHREADING_SUPPORTED) {
            executorService = Executors.newFixedThreadPool(MulthithreadingUtils.PARALLELISM);
        }
    }

    public void addTask(Runnable runnable, BufferType bufferType) {
        storeRenderObjectTasks[bufferType.ordinal()].setFuture(executorService.submit(runnable));
    }

    public void addTask(StoreRenderObjectTask task) {
        task.setFuture(executorService.submit(task.getRunnable()));
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

    public void render(BufferType bufferType) {
        StoreRenderObjectTask storeRenderObjectTask = storeRenderObjectTasks[bufferType.ordinal()];

        try {
            storeRenderObjectTask.getFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (storeRenderObjectTask.getObjectCount() > 0) {
            vao.updateVertexBuffer(0, storeRenderObjectTask.getVertexBuffer().limit(storeRenderObjectTask.getObjectCount() * VERTEX_DATA_SIZE), GL44C.GL_DYNAMIC_STORAGE_BIT, VERTEX_DATA_SIZE);
            vao.updateBuffer(1, storeRenderObjectTask.getMaterialBuffer().limit(storeRenderObjectTask.getObjectCount() * MATERIAL_DATA_SIZE), GL44C.GL_DYNAMIC_STORAGE_BIT);
            vao.bindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, 1);
            GL11C.glDrawArrays(GL11C.GL_QUADS, 0, storeRenderObjectTask.getObjectCount() << 2);
            Core.getCore().getRenderer().increaseDrawCalls();
            storeRenderObjectTask.reset();
        }
    }

    public void render(int count, FloatBuffer vertexBuffer, ByteBuffer materialBuffer) {
        vao.updateVertexBuffer(0, vertexBuffer.limit(count * VERTEX_DATA_SIZE), GL44C.GL_DYNAMIC_STORAGE_BIT, VERTEX_DATA_SIZE);
        vao.updateBuffer(1, materialBuffer.limit(count * MATERIAL_DATA_SIZE), GL44C.GL_DYNAMIC_STORAGE_BIT);
        vao.bindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, 1);
        GL11C.glDrawArrays(GL11C.GL_QUADS, 0, count << 2);
        Core.getCore().getRenderer().increaseDrawCalls();
    }

    public void addToRenderPipeLine(GLString glString, BufferType bufferType) {
        StoreRenderObjectTask storeRenderObjectTask = storeRenderObjectTasks[bufferType.ordinal()];
        storeRenderObjectTask.getVertexBuffer().put(storeRenderObjectTask.getVertexBufferIndex().getAndAdd(glString.getVertexBuffer().remaining()), glString.getVertexBuffer(), 0,
                glString.getVertexBuffer().remaining());
        storeRenderObjectTask.getMaterialBuffer().put(storeRenderObjectTask.getMaterialBufferIndex().getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        storeRenderObjectTask.addObjectCount(glString.getVertexBuffer().remaining() / VERTEX_DATA_SIZE);
    }

    public void addToRenderPipeLine(GLString glString, float x, float y, BufferType bufferType) {
        StoreRenderObjectTask storeRenderObjectTask = storeRenderObjectTasks[bufferType.ordinal()];

        int vertexDataSize = glString.getVertexBuffer().remaining();
        int startIndex = storeRenderObjectTask.getVertexBufferIndex().getAndAdd(vertexDataSize);
        storeRenderObjectTask.getVertexBuffer().put(startIndex, glString.getVertexBuffer(), 0, vertexDataSize);

        for (int i = 0; i < vertexDataSize; i += 4) {
            storeRenderObjectTask.getVertexBuffer().put(startIndex + i, glString.getVertexBuffer().get(i) + x);
            storeRenderObjectTask.getVertexBuffer().put(startIndex + i + 1, glString.getVertexBuffer().get(i + 1) + y);
        }

        storeRenderObjectTask.getMaterialBuffer().put(storeRenderObjectTask.getMaterialBufferIndex().getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        storeRenderObjectTask.addObjectCount(vertexDataSize / VERTEX_DATA_SIZE);
    }

    public void addToRenderPipeLine(TextureObject textureObject, BufferType bufferType) {
        StoreRenderObjectTask storeRenderObjectTask = storeRenderObjectTasks[bufferType.ordinal()];
        storeVertices(textureObject, Core.getCore().getRenderer().getInterpolation(), storeRenderObjectTask.getVertexBuffer(), storeRenderObjectTask.getVertexBufferIndex());
        storeColor(textureObject.getColor(), storeRenderObjectTask.getMaterialBuffer(), storeRenderObjectTask.getMaterialBufferIndex());
        storeTextureHandle(textureObject.getTexture().getTextureHandle(), storeRenderObjectTask.getMaterialBuffer(), storeRenderObjectTask.getMaterialBufferIndex());
        storeRenderObjectTask.incrementObjectCount();
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        StoreRenderObjectTask storeRenderObjectTask = storeRenderObjectTasks[bufferType.ordinal()];
        addToRenderPipeLine(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, r, g, b, a, texture, Core.getCore().getRenderer().getInterpolation(),
                storeRenderObjectTask.getVertexBuffer(), storeRenderObjectTask.getVertexBufferIndex(), storeRenderObjectTask.getMaterialBuffer(),
                storeRenderObjectTask.getMaterialBufferIndex());
        storeRenderObjectTask.incrementObjectCount();
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        storeVertices(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, interpolation, vertexBuffer, vertexBufferIndex);
        storeColor(r, g, b, a, materialBuffer, materialBufferIndex);
        storeTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLine(float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        StoreRenderObjectTask storeRenderObjectTask = storeRenderObjectTasks[bufferType.ordinal()];
        storeVertices(x, y, LUT.sin(rotation), LUT.cos(rotation), scaleX * 0.5f, scaleY * 0.5f, storeRenderObjectTask.getVertexBuffer(), storeRenderObjectTask.getVertexBufferIndex());
        storeColor(r, g, b, a, storeRenderObjectTask.getMaterialBuffer(), storeRenderObjectTask.getMaterialBufferIndex());
        storeTextureHandle(texture.getTextureHandle(), storeRenderObjectTask.getMaterialBuffer(), storeRenderObjectTask.getMaterialBufferIndex());
        storeRenderObjectTask.incrementObjectCount();
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        addGUIElementToRenderPipeLine(x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle(), bufferType);
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle, BufferType bufferType) {
        StoreRenderObjectTask storeRenderObjectTask = storeRenderObjectTasks[bufferType.ordinal()];
        storeVertices(x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, y, x, y, storeRenderObjectTask.getVertexBuffer(), storeRenderObjectTask.getVertexBufferIndex());
        storeColor(r, g, b, a, storeRenderObjectTask.getMaterialBuffer(), storeRenderObjectTask.getMaterialBufferIndex());
        storeTextureHandle(textureHandle, storeRenderObjectTask.getMaterialBuffer(), storeRenderObjectTask.getMaterialBufferIndex());
        storeRenderObjectTask.incrementObjectCount();
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float rotation, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle, FloatBuffer vertexBuffer,
                                              MutableInt vertexBufferIndex, ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        storeGuiElementVertices(x, y, LUT.sin(rotation), LUT.cos(rotation), sizeX, sizeY, vertexBuffer, vertexBufferIndex);
        storeColor(r, g, b, a, materialBuffer, materialBufferIndex);
        storeTextureHandle(textureHandle, materialBuffer, materialBufferIndex);
    }

    private void storeVertices(TextureObject textureObject, float interpolation, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        storeVertices(textureObject.getLastPosition().x, textureObject.getLastPosition().y, textureObject.getPosition().x, textureObject.getPosition().y, textureObject.getLastRotation(),
                textureObject.getRotation(), textureObject.getLastScale().x, textureObject.getLastScale().y, textureObject.getScale().x, textureObject.getScale().y, interpolation,
                floatBuffer, bufferIndex);
    }

    private void storeVertices(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                               float interpolation, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float sizeX = 0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation);
        final float sizeY = 0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation);
        final float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation;
        final float sin = LUT.sin(interpolatedRotation);
        final float cos = LUT.cos(interpolatedRotation);
        final float positionX = lastX + (x - lastX) * interpolation;
        final float positionY = lastY + (y - lastY) * interpolation;
        storeVertices(positionX, positionY, sin, cos, sizeX, sizeY, floatBuffer, bufferIndex);
    }

    private void storeVertices(float x, float y, float sizeX, float sizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        storeVertices(-sizeX + x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, -sizeY + y, -sizeX + x, -sizeY + y, floatBuffer, bufferIndex);
    }

    private void storeVertices(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, FloatBuffer floatBuffer, MutableInt bufferIndex) {
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

    private void storeVertices(float x, float y, float sin, float cos, float sizeX, float sizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float minusSizeX = -sizeX;
        final float minusSizeY = -sizeY;

        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;

        final float sinSizeX = sin * sizeX;
        final float cosSizeX = cos * sizeX;
        final float sinSizeY = sin * sizeY;
        final float cosSizeY = cos * sizeY;

        final float x1 = cos * minusSizeX - sinSizeY + x;
        final float x2 = cosSizeX - sinSizeY + x;
        final float x3 = cosSizeX - sin * minusSizeY + x;
        final float y3 = sinSizeX + cos * minusSizeY + y;
        final float y1 = sin * minusSizeX + cosSizeY + y;
        final float y2 = sinSizeX + cosSizeY + y;

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
        floatBuffer.put(bufferIndex.getAndIncrement(), x1 + (x3 - x2));
        floatBuffer.put(bufferIndex.getAndIncrement(), y3 - (y2 - y1));
        floatBuffer.put(bufferIndex.getAndIncrement(), u4);
        floatBuffer.put(bufferIndex.getAndIncrement(), v4);
    }

    private void storeGuiElementVertices(float x, float y, float sin, float cos, float sizeX, float sizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float u1 = 0.0f;
        final float v1 = 1.0f;
        final float u2 = 1.0f;
        final float v2 = 1.0f;
        final float u3 = 1.0f;
        final float v3 = 0.0f;
        final float u4 = 0.0f;
        final float v4 = 0.0f;

        final float sinSizeX = sin * sizeX;
        final float cosSizeX = cos * sizeX;
        final float sinSizeY = sin * sizeY;
        final float cosSizeY = cos * sizeY;

        final float x1 = -sinSizeY + x;
        final float x2 = cosSizeX - sinSizeY + x;
        final float x3 = cosSizeX + x;
        final float y3 = sinSizeX + y;
        final float y1 = cosSizeY + y;
        final float y2 = sinSizeX + cosSizeY + y;

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
        floatBuffer.put(bufferIndex.getAndIncrement(), x1 + (x3 - x2));
        floatBuffer.put(bufferIndex.getAndIncrement(), y3 - (y2 - y1));
        floatBuffer.put(bufferIndex.getAndIncrement(), u4);
        floatBuffer.put(bufferIndex.getAndIncrement(), v4);
    }

    private void storeColor(Vector4f color, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        storeColor(color.x, color.y, color.z, color.w, byteBuffer, bufferIndex);
    }

    private void storeColor(float r, float g, float b, float a, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), r);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), g);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), b);
        byteBuffer.putFloat(bufferIndex.getAndAdd(4), a);
    }

    private void storeTextureHandle(long textureHandle, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        byteBuffer.putLong(bufferIndex.getAndAdd(8), textureHandle);
        byteBuffer.putLong(bufferIndex.getAndAdd(8), 0);//padding
    }

    public void clear() {
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
