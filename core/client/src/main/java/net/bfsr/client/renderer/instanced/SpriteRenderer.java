package net.bfsr.client.renderer.instanced;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.TextureObject;
import net.bfsr.client.renderer.font.string.GLString;
import net.bfsr.client.renderer.primitive.VAO;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.math.LUT;
import net.bfsr.math.MathUtils;
import net.bfsr.util.MultithreadingUtils;
import net.bfsr.util.MutableInt;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL44C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.*;

public class SpriteRenderer {
    public static SpriteRenderer INSTANCE;

    public static final int VERTEX_DATA_SIZE = 16;
    public static final int MATERIAL_DATA_SIZE = 32;

    private VAO vao;
    private ExecutorService executorService;
    @Getter
    private final BuffersHolder[] buffersHolders = new BuffersHolder[BufferType.values().length];

    public SpriteRenderer() {
        INSTANCE = this;

        buffersHolders[BufferType.BACKGROUND.ordinal()] = new BuffersHolder(1);
        buffersHolders[BufferType.ENTITIES_ALPHA.ordinal()] = new BuffersHolder(512);
        buffersHolders[BufferType.ENTITIES_ADDITIVE.ordinal()] = new BuffersHolder(512);
        buffersHolders[BufferType.GUI.ordinal()] = new BuffersHolder(512);

        if (MultithreadingUtils.MULTITHREADING_SUPPORTED) {
            executorService = Executors.newFixedThreadPool(MultithreadingUtils.PARALLELISM);
        }
    }

    public void addTask(Runnable runnable, BufferType bufferType) {
        buffersHolders[bufferType.ordinal()].setFuture(executorService.submit(runnable));
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
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];

        try {
            buffersHolder.getFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (buffersHolder.getObjectCount() > 0) {
            render(buffersHolder.getObjectCount(), buffersHolder.getVertexBuffer(), buffersHolder.getMaterialBuffer());
            buffersHolder.reset();
        }
    }

    public void render(BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        if (buffersHolder.getObjectCount() > 0) {
            render(buffersHolder.getObjectCount(), buffersHolder.getVertexBuffer(), buffersHolder.getMaterialBuffer());
            buffersHolder.reset();
        }
    }

    public void render(int count, FloatBuffer vertexBuffer, ByteBuffer materialBuffer) {
        vao.updateVertexBuffer(0, vertexBuffer.limit(count * VERTEX_DATA_SIZE), GL44C.GL_DYNAMIC_STORAGE_BIT, VERTEX_DATA_SIZE);
        vao.updateBuffer(1, materialBuffer.limit(count * MATERIAL_DATA_SIZE), GL44C.GL_DYNAMIC_STORAGE_BIT);
        vao.bindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, 1);
        GL11C.glDrawArrays(GL11C.GL_QUADS, 0, count << 2);
        Core.get().getRenderer().increaseDrawCalls();
    }

    public void addToRenderPipeLine(GLString glString, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(glString.getVertexBuffer().remaining() / VERTEX_DATA_SIZE);
        buffersHolder.getVertexBuffer().put(buffersHolder.getVertexBufferIndex().getAndAdd(glString.getVertexBuffer().remaining()), glString.getVertexBuffer(), 0,
                glString.getVertexBuffer().remaining());
        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex().getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        buffersHolder.addObjectCount(glString.getVertexBuffer().remaining() / VERTEX_DATA_SIZE);
    }

    public void addToRenderPipeLine(GLString glString, float x, float y, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];

        int vertexDataSize = glString.getVertexBuffer().remaining();
        int startIndex = buffersHolder.getVertexBufferIndex().getAndAdd(vertexDataSize);
        buffersHolder.getVertexBuffer().put(startIndex, glString.getVertexBuffer(), 0, vertexDataSize);

        for (int i = 0; i < vertexDataSize; i += 4) {
            buffersHolder.getVertexBuffer().put(startIndex + i, glString.getVertexBuffer().get(i) + x);
            buffersHolder.getVertexBuffer().put(startIndex + i + 1, glString.getVertexBuffer().get(i + 1) + y);
        }

        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex().getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        buffersHolder.addObjectCount(vertexDataSize / VERTEX_DATA_SIZE);
    }

    public void renderString(GLString glString, float lastX, float lastY, float x, float y, float scaleX, float scaleY, BufferType bufferType) {
        float interpolation = Core.get().getRenderer().getInterpolation();
        renderString(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, scaleX, scaleY, bufferType);
    }

    public void renderStringWithShadow(GLString glString, float lastX, float lastY, float x, float y, float scaleX, float scaleY, float shadowOffsetX, float shadowOffsetY,
                                       BufferType bufferType) {
        float interpolation = Core.get().getRenderer().getInterpolation();
        renderStringWithShadow(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, scaleX, scaleY, shadowOffsetX, shadowOffsetY, bufferType);
    }

    public void renderString(GLString glString, float x, float y, float scaleX, float scaleY, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];

        FloatBuffer stringVertexBuffer = glString.getVertexBuffer();
        int vertexDataSize = stringVertexBuffer.remaining();
        int startIndex = buffersHolder.getVertexBufferIndex().getAndAdd(vertexDataSize);
        FloatBuffer vertexBuffer = buffersHolder.getVertexBuffer();
        vertexBuffer.put(startIndex, stringVertexBuffer, 0, vertexDataSize);

        for (int i = 0; i < vertexDataSize; i += 4) {
            vertexBuffer.put(startIndex + i, stringVertexBuffer.get(i) * scaleX + x);
            vertexBuffer.put(startIndex + i + 1, stringVertexBuffer.get(i + 1) * scaleY + y);
        }

        ByteBuffer stringMaterialBuffer = glString.getMaterialBuffer();
        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex().getAndAdd(stringMaterialBuffer.remaining()), stringMaterialBuffer, 0, stringMaterialBuffer.remaining());
        buffersHolder.addObjectCount(vertexDataSize / VERTEX_DATA_SIZE);
    }

    public void renderStringWithShadow(GLString glString, float x, float y, float scaleX, float scaleY, float shadowOffsetX, float shadowOffsetY, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        FloatBuffer stringVertexBuffer = glString.getVertexBuffer();
        int vertexDataSize = stringVertexBuffer.remaining();
        int objectCount = vertexDataSize / VERTEX_DATA_SIZE << 1;
        buffersHolder.checkBuffersSize(objectCount);
        int startIndex = buffersHolder.getVertexBufferIndex().getAndAdd(vertexDataSize);

        FloatBuffer vertexBuffer = buffersHolder.getVertexBuffer();
        vertexBuffer.put(startIndex, stringVertexBuffer, 0, vertexDataSize);

        for (int i = 0; i < vertexDataSize; i += 4) {
            vertexBuffer.put(startIndex + i, stringVertexBuffer.get(i) * scaleX + x + shadowOffsetX);
            vertexBuffer.put(startIndex + i + 1, stringVertexBuffer.get(i + 1) * scaleY + y + shadowOffsetY);
        }

        startIndex = buffersHolder.getVertexBufferIndex().getAndAdd(vertexDataSize);

        vertexBuffer.put(startIndex, stringVertexBuffer, 0, vertexDataSize);

        for (int i = 0; i < vertexDataSize; i += 4) {
            vertexBuffer.put(startIndex + i, stringVertexBuffer.get(i) * scaleX + x);
            vertexBuffer.put(startIndex + i + 1, stringVertexBuffer.get(i + 1) * scaleY + y);
        }

        ByteBuffer materialBuffer = buffersHolder.getMaterialBuffer();
        ByteBuffer stringMaterialBuffer = glString.getMaterialBuffer();
        int materialStartIndex = buffersHolder.getMaterialBufferIndex().getAndAdd(stringMaterialBuffer.remaining());
        materialBuffer.put(materialStartIndex, stringMaterialBuffer, 0, stringMaterialBuffer.remaining());

        int materialDataSize = stringMaterialBuffer.remaining();

        for (int i = 0; i < materialDataSize; i += 32) {
            materialBuffer.putFloat(materialStartIndex + i, 0.0f);
            materialBuffer.putFloat(materialStartIndex + i + 4, 0.0f);
            materialBuffer.putFloat(materialStartIndex + i + 8, 0.0f);
        }

        materialBuffer.put(buffersHolder.getMaterialBufferIndex().getAndAdd(stringMaterialBuffer.remaining()), stringMaterialBuffer, 0, stringMaterialBuffer.remaining());
        buffersHolder.addObjectCount(objectCount);
    }

    public void addToRenderPipeLine(TextureObject textureObject, BufferType bufferType) {
//        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
//        putVertices(textureObject, Core.get().getRenderer().getInterpolation(), buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
//        putColor(textureObject.getColor(), buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
//        putTextureHandle(textureObject.getTexture().getTextureHandle(), buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
//        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float lastScaleX, float lastScaleY, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLineSinCos(lastX, lastY, x, y, lastSin, lastCos, sin, cos, lastScaleX, lastScaleY, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
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
        addToRenderPipeLineSinCos(lastX, lastY, x, y, sin, cos, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLine(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLine(lastX, lastY, x, y, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float rotation, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLine(lastX, lastY, x, y, rotation, scaleX, scaleY, r, g, b, a, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                                    Vector4f lastColor, Vector4f color, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        addToRenderPipeLine(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, lastColor, color, texture, Core.get().getRenderer().getInterpolation(),
                buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex(), buffersHolder.getMaterialBuffer(),
                buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, interpolation, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, 0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float rotation, float scaleX, float scaleY,
                                    float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, LUT.sin(rotation), LUT.cos(rotation), 0.5f * scaleX, 0.5f * scaleY, vertexBuffer,
                vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float lastScaleX, float lastScaleY, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation), 0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation), vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float scaleX,
                                          float scaleY, Vector4f lastColor, Vector4f color, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, lastSin + (sin - lastSin) * interpolation, lastCos + (cos - lastCos) * interpolation,
                0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLineSinCos(float lastX, float lastY, float x, float y, float sin, float cos, float scaleX,
                                          float scaleY, float r, float g, float b, float a, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                          ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, sin, cos, 0.5f * scaleX, 0.5f * scaleY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLine(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                                    Vector4f lastColor, Vector4f color, Texture texture, float interpolation, FloatBuffer vertexBuffer, MutableInt vertexBufferIndex,
                                    ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putVertices(lastX, lastY, x, y, lastRotation, rotation, lastScaleX, lastScaleY, scaleX, scaleY, interpolation, vertexBuffer, vertexBufferIndex);
        putColor(lastColor, color, materialBuffer, materialBufferIndex, interpolation);
        putTextureHandle(texture.getTextureHandle(), materialBuffer, materialBufferIndex);
    }

    public void addToRenderPipeLine(float x, float y, float scaleX, float scaleY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        putVertices(x, y, scaleX * 0.5f, scaleY * 0.5f, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putTextureHandle(texture.getTextureHandle(), buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addToRenderPipeLine(float x, float y, float rotation, float scaleX, float scaleY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        putVertices(x, y, LUT.sin(rotation), LUT.cos(rotation), scaleX * 0.5f, scaleY * 0.5f, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putTextureHandle(texture.getTextureHandle(), buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, Texture texture, BufferType bufferType) {
        addGUIElementToRenderPipeLine(x, y, sizeX, sizeY, r, g, b, a, texture.getTextureHandle(), bufferType);
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle, BufferType bufferType) {
        BuffersHolder buffersHolder = buffersHolders[bufferType.ordinal()];
        putVertices(x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, y, x, y, buffersHolder.getVertexBuffer(), buffersHolder.getVertexBufferIndex());
        putColor(r, g, b, a, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        putTextureHandle(textureHandle, buffersHolder.getMaterialBuffer(), buffersHolder.getMaterialBufferIndex());
        buffersHolder.incrementObjectCount();
    }

    public void addGUIElementToRenderPipeLine(float x, float y, float rotation, float sizeX, float sizeY, float r, float g, float b, float a, long textureHandle, FloatBuffer vertexBuffer,
                                              MutableInt vertexBufferIndex, ByteBuffer materialBuffer, MutableInt materialBufferIndex) {
        putGuiElementVertices(x, y, LUT.sin(rotation), LUT.cos(rotation), sizeX, sizeY, vertexBuffer, vertexBufferIndex);
        putColor(r, g, b, a, materialBuffer, materialBufferIndex);
        putTextureHandle(textureHandle, materialBuffer, materialBufferIndex);
    }

    private void putVertices(TextureObject textureObject, float interpolation, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        putVertices(textureObject.getLastPosition().x, textureObject.getLastPosition().y, textureObject.getPosition().x, textureObject.getPosition().y, textureObject.getLastRotation(),
                textureObject.getRotation(), textureObject.getLastScale().x, textureObject.getLastScale().y, textureObject.getScale().x, textureObject.getScale().y, interpolation,
                floatBuffer, bufferIndex);
    }

    public void putVertices(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float lastScaleX, float lastScaleY, float scaleX, float scaleY,
                            float interpolation, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        final float interpolatedRotation = lastRotation + MathUtils.lerpAngle(lastRotation, rotation) * interpolation;
        putVertices(lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, LUT.sin(interpolatedRotation), LUT.cos(interpolatedRotation),
                0.5f * (lastScaleX + (scaleX - lastScaleX) * interpolation), 0.5f * (lastScaleY + (scaleY - lastScaleY) * interpolation), floatBuffer, bufferIndex);
    }

    private void putVertices(float x, float y, float sizeX, float sizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
        putVertices(-sizeX + x, sizeY + y, sizeX + x, sizeY + y, sizeX + x, -sizeY + y, -sizeX + x, -sizeY + y, floatBuffer, bufferIndex);
    }

    private void putVertices(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, FloatBuffer floatBuffer, MutableInt bufferIndex) {
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

    private void putVertices(float x, float y, float sin, float cos, float sizeX, float sizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
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

    private void putGuiElementVertices(float x, float y, float sin, float cos, float sizeX, float sizeY, FloatBuffer floatBuffer, MutableInt bufferIndex) {
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

    private void putColor(Vector4f color, ByteBuffer byteBuffer, MutableInt bufferIndex) {
        putColor(color.x, color.y, color.z, color.w, byteBuffer, bufferIndex);
    }

    private void putColor(float r, float g, float b, float a, ByteBuffer byteBuffer, MutableInt bufferIndex) {
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
        byteBuffer.putInt(bufferIndex.getAndAdd(4), 0);//padding
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
