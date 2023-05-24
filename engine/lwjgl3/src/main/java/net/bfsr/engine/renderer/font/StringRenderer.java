package net.bfsr.engine.renderer.font;

import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.string.AbstractGLString;
import net.bfsr.engine.renderer.font.string.AbstractStringGeometryBuilder;
import net.bfsr.engine.renderer.font.string.AbstractStringRenderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public final class StringRenderer extends AbstractStringRenderer {
    private static final int INITIAL_QUADS_COUNT = 128;

    private AbstractRenderer renderer;
    private AbstractSpriteRenderer spriteRenderer;
    private AbstractStringGeometryBuilder stringGeometryBuilder;
    private final GLString glString = createGLString();

    @Override
    public void init() {
        this.renderer = Engine.renderer;
        this.stringGeometryBuilder = Engine.renderer.stringGeometryBuilder;
        this.spriteRenderer = Engine.renderer.spriteRenderer;
        glString.init(INITIAL_QUADS_COUNT);
    }

    @Override
    public GLString createGLString() {
        return new GLString();
    }

    public void render(AbstractGLString glString, BufferType bufferType) {
        addString(glString, bufferType);
    }

    @Override
    public int render(String string, StringCache stringCache, int fontSize, int x, int y, float r, float g, float b, float a, int maxWidth, int indent, BufferType bufferType) {
        stringGeometryBuilder.createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, maxWidth, indent);
        render(glString, bufferType);
        return glString.getHeight();
    }

    @Override
    public void addString(AbstractGLString glString, BufferType bufferType) {
        AbstractBuffersHolder buffersHolder = spriteRenderer.getBuffersHolder(bufferType);
        buffersHolder.checkBuffersSize(glString.getVertexBuffer().remaining() / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
        buffersHolder.getVertexBuffer().put(buffersHolder.getVertexBufferIndex().getAndAdd(glString.getVertexBuffer().remaining()), glString.getVertexBuffer(), 0,
                glString.getVertexBuffer().remaining());
        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex().getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        buffersHolder.addObjectCount(glString.getVertexBuffer().remaining() / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
    }

    @Override
    public void addString(AbstractGLString glString, float x, float y, BufferType bufferType) {
        AbstractBuffersHolder buffersHolder = spriteRenderer.getBuffersHolder(bufferType);

        int vertexDataSize = glString.getVertexBuffer().remaining();
        int objectCount = vertexDataSize / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES << 1;
        buffersHolder.checkBuffersSize(objectCount);
        int startIndex = buffersHolder.getVertexBufferIndex().getAndAdd(vertexDataSize);
        buffersHolder.getVertexBuffer().put(startIndex, glString.getVertexBuffer(), 0, vertexDataSize);

        for (int i = 0; i < vertexDataSize; i += 4) {
            buffersHolder.getVertexBuffer().put(startIndex + i, glString.getVertexBuffer().get(i) + x);
            buffersHolder.getVertexBuffer().put(startIndex + i + 1, glString.getVertexBuffer().get(i + 1) + y);
        }

        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex().getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        buffersHolder.addObjectCount(vertexDataSize / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
    }

    @Override
    public void addString(AbstractGLString glString, float lastX, float lastY, float x, float y, float scaleX, float scaleY, BufferType bufferType) {
        float interpolation = renderer.getInterpolation();
        addString(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, scaleX, scaleY, bufferType);
    }

    @Override
    public void addStringWithShadow(AbstractGLString glString, float lastX, float lastY, float x, float y, float scaleX, float scaleY, float shadowOffsetX, float shadowOffsetY,
                                    BufferType bufferType) {
        float interpolation = renderer.getInterpolation();
        addStringWithShadow(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, scaleX, scaleY, shadowOffsetX, shadowOffsetY, bufferType);
    }

    @Override
    public void addStringInterpolated(AbstractGLString glString, float lastX, float lastY, float x, float y, BufferType bufferType) {
        float interpolation = renderer.getInterpolation();
        addString(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, bufferType);
    }

    @Override
    public void addString(AbstractGLString glString, float x, float y, float scaleX, float scaleY, BufferType bufferType) {
        AbstractBuffersHolder buffersHolder = spriteRenderer.getBuffersHolder(bufferType);

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
        buffersHolder.addObjectCount(vertexDataSize / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
    }

    private void addStringWithShadow(AbstractGLString glString, float x, float y, float scaleX, float scaleY, float shadowOffsetX, float shadowOffsetY, BufferType bufferType) {
        AbstractBuffersHolder buffersHolder = spriteRenderer.getBuffersHolder(bufferType);
        FloatBuffer stringVertexBuffer = glString.getVertexBuffer();
        int vertexDataSize = stringVertexBuffer.remaining();
        int objectCount = vertexDataSize / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES << 1;
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

        for (int i = 0; i < materialDataSize; i += AbstractSpriteRenderer.MATERIAL_DATA_SIZE_IN_BYTES) {
            materialBuffer.putFloat(materialStartIndex + i, 0.0f);
            materialBuffer.putFloat(materialStartIndex + i + 4, 0.0f);
            materialBuffer.putFloat(materialStartIndex + i + 8, 0.0f);
        }

        materialBuffer.put(buffersHolder.getMaterialBufferIndex().getAndAdd(stringMaterialBuffer.remaining()), stringMaterialBuffer, 0, stringMaterialBuffer.remaining());
        buffersHolder.addObjectCount(objectCount);
    }
}