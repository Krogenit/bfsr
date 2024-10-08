package net.bfsr.engine.renderer.font;

import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.stb.STBTrueTypeGlyphsBuilder;
import net.bfsr.engine.renderer.font.string.AbstractGLString;
import net.bfsr.engine.renderer.font.string.AbstractStringGeometryBuilder;
import net.bfsr.engine.renderer.font.string.AbstractStringRenderer;
import net.bfsr.engine.renderer.font.truetype.TrueTypeGlyphsBuilder;

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
    public int render(String string, GlyphsBuilder glyphsBuilder, int fontSize, int x, int y, float r, float g, float b, float a,
                      int maxWidth, int indent, BufferType bufferType) {
        stringGeometryBuilder.createString(glString, glyphsBuilder, string, x, y, fontSize, r, g, b, a, maxWidth, indent);
        render(glString, bufferType);
        return glString.getHeight();
    }

    @Override
    public void addString(AbstractGLString glString, BufferType bufferType) {
        AbstractBuffersHolder buffersHolder = spriteRenderer.getBuffersHolder(bufferType);
        buffersHolder.checkBuffersSize(glString.getVertexBuffer().remaining() / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
        buffersHolder.getVertexBuffer().put(buffersHolder.getVertexBufferIndex()
                        .getAndAdd(glString.getVertexBuffer().remaining()), glString.getVertexBuffer(), 0,
                glString.getVertexBuffer().remaining());
        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex()
                        .getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        buffersHolder.addObjectCount(glString.getVertexBuffer().remaining() / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
    }

    /**
     * TODO: optimize string rendering with custom position
     *
     * @param glString   the glString
     * @param x          the x coordinate where to render a string
     * @param y          the y coordinate where to render a string
     * @param bufferType the type of buffer to put the string data in
     */
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

        buffersHolder.getMaterialBuffer().put(buffersHolder.getMaterialBufferIndex()
                        .getAndAdd(glString.getMaterialBuffer().remaining()), glString.getMaterialBuffer(), 0,
                glString.getMaterialBuffer().remaining());
        buffersHolder.addObjectCount(vertexDataSize / AbstractSpriteRenderer.VERTEX_DATA_SIZE_IN_BYTES);
    }

    @Override
    public void addString(AbstractGLString glString, float lastX, float lastY, float x, float y,
                          BufferType bufferType) {
        float interpolation = renderer.getInterpolation();
        addString(glString, lastX + (x - lastX) * interpolation, lastY + (y - lastY) * interpolation, bufferType);
    }

    @Override
    public GlyphsBuilder createSTBTrueTypeGlyphsBuilder(String fontFile) {
        return new STBTrueTypeGlyphsBuilder(fontFile);
    }

    @Override
    public GlyphsBuilder createTrueTypeGlyphsBuilder(String fontFile) {
        return new TrueTypeGlyphsBuilder(fontFile);
    }
}