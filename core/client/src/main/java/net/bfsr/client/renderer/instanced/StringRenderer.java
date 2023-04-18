package net.bfsr.client.renderer.instanced;

import net.bfsr.client.core.Core;
import net.bfsr.client.renderer.font.StringCache;
import net.bfsr.client.renderer.font.StringGeometryBuilder;
import net.bfsr.client.renderer.font.string.GLString;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public final class StringRenderer {
    private static final int INITIAL_QUADS_COUNT = 128;

    private SpriteRenderer spriteRenderer;
    private StringGeometryBuilder stringGeometryBuilder;
    private final GLString glString = new GLString();

    public void init(StringGeometryBuilder stringGeometryBuilder, SpriteRenderer spriteRenderer) {
        this.stringGeometryBuilder = stringGeometryBuilder;
        this.spriteRenderer = spriteRenderer;
        glString.init(INITIAL_QUADS_COUNT);
    }

    public void render(GLString glString, BufferType bufferType) {
        addString(glString, bufferType);
    }

    public void render(String string, StringCache stringCache, int fontSize, int x, int y, BufferType bufferType) {
        render(string, stringCache, fontSize, x, y, 1.0f, 1.0f, 1.0f, 1.0f, bufferType);
    }

    public void render(String string, StringCache stringCache, int fontSize, int x, int y, float r, float g, float b, float a, BufferType bufferType) {
        Core.get().getRenderer().getStringGeometryBuilder().createString(glString, stringCache, string, x, y, fontSize, r, g, b, a);
        render(glString, bufferType);
    }

    public int render(String string, StringCache stringCache, int fontSize, int x, int y, float r, float g, float b, float a, int maxWidth, BufferType bufferType) {
        stringGeometryBuilder.createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, maxWidth);
        render(glString, bufferType);
        return glString.getHeight();
    }

    public int render(String string, StringCache stringCache, int fontSize, int x, int y, float r, float g, float b, float a, int maxWidth, int indent, BufferType bufferType) {
        stringGeometryBuilder.createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, maxWidth, indent);
        render(glString, bufferType);
        return glString.getHeight();
    }


    public void addString(GLString glString, BufferType bufferType) {
        BuffersHolder buffersHolder = spriteRenderer.buffersHolders[bufferType.ordinal()];
        buffersHolder.checkBuffersSize(glString.getVertexBuffer().remaining() / SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
        buffersHolder.getVertexBuffer().put(buffersHolder.getVertexBufferIndex().getAndAdd(glString.getVertexBuffer().remaining()), glString.getVertexBuffer(), 0,
                glString.getVertexBuffer().remaining());
        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex().getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        buffersHolder.addObjectCount(glString.getVertexBuffer().remaining() / SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
    }

    public void addString(GLString glString, float x, float y, BufferType bufferType) {
        BuffersHolder buffersHolder = spriteRenderer.buffersHolders[bufferType.ordinal()];

        int vertexDataSize = glString.getVertexBuffer().remaining();
        int objectCount = vertexDataSize / SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES << 1;
        buffersHolder.checkBuffersSize(objectCount);
        int startIndex = buffersHolder.getVertexBufferIndex().getAndAdd(vertexDataSize);
        buffersHolder.getVertexBuffer().put(startIndex, glString.getVertexBuffer(), 0, vertexDataSize);

        for (int i = 0; i < vertexDataSize; i += 4) {
            buffersHolder.getVertexBuffer().put(startIndex + i, glString.getVertexBuffer().get(i) + x);
            buffersHolder.getVertexBuffer().put(startIndex + i + 1, glString.getVertexBuffer().get(i + 1) + y);
        }

        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex().getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        buffersHolder.addObjectCount(vertexDataSize / SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
    }

    public void addString(GLString glString, float lastX, float lastY, float x, float y, float scaleX, float scaleY, BufferType bufferType) {
        float interpolation = Core.get().getRenderer().getInterpolation();
        addString(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, scaleX, scaleY, bufferType);
    }

    public void addStringWithShadow(GLString glString, float lastX, float lastY, float x, float y, float scaleX, float scaleY, float shadowOffsetX, float shadowOffsetY,
                                    BufferType bufferType) {
        float interpolation = Core.get().getRenderer().getInterpolation();
        addStringWithShadow(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, scaleX, scaleY, shadowOffsetX, shadowOffsetY, bufferType);
    }

    public void addStringInterpolated(GLString glString, float lastX, float lastY, float x, float y, BufferType bufferType) {
        float interpolation = Core.get().getRenderer().getInterpolation();
        addString(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, bufferType);
    }

    public void addString(GLString glString, float x, float y, float scaleX, float scaleY, BufferType bufferType) {
        BuffersHolder buffersHolder = spriteRenderer.buffersHolders[bufferType.ordinal()];

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
        buffersHolder.addObjectCount(vertexDataSize / SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
    }

    public void addStringWithShadow(GLString glString, float x, float y, float scaleX, float scaleY, float shadowOffsetX, float shadowOffsetY, BufferType bufferType) {
        BuffersHolder buffersHolder = spriteRenderer.buffersHolders[bufferType.ordinal()];
        FloatBuffer stringVertexBuffer = glString.getVertexBuffer();
        int vertexDataSize = stringVertexBuffer.remaining();
        int objectCount = vertexDataSize / SpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES << 1;
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

        for (int i = 0; i < materialDataSize; i += SpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES) {
            materialBuffer.putFloat(materialStartIndex + i, 0.0f);
            materialBuffer.putFloat(materialStartIndex + i + 4, 0.0f);
            materialBuffer.putFloat(materialStartIndex + i + 8, 0.0f);
        }

        materialBuffer.put(buffersHolder.getMaterialBufferIndex().getAndAdd(stringMaterialBuffer.remaining()), stringMaterialBuffer, 0, stringMaterialBuffer.remaining());
        buffersHolder.addObjectCount(objectCount);
    }

    public static StringRenderer get() {
        return Core.get().getRenderer().getStringRenderer();
    }
}