package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.string.AbstractGLString;
import net.bfsr.engine.renderer.font.string.AbstractStringGeometryBuilder;
import net.bfsr.engine.renderer.font.string.AbstractStringRenderer;

import java.nio.FloatBuffer;

public class LabelRenderer extends GuiObjectRenderer {
    private final AbstractStringRenderer stringRenderer = Engine.renderer.stringRenderer;
    private final AbstractStringGeometryBuilder stringGeometryBuilder = Engine.renderer.stringGeometryBuilder;
    private final AbstractGLString glString = stringRenderer.createGLString();
    private final Label label;
    private final GlyphsBuilder glyphsBuilder;

    public LabelRenderer(Label label, GlyphsBuilder glyphsBuilder) {
        super(label);
        this.label = label;
        this.glyphsBuilder = glyphsBuilder;
        this.glString.init(Math.max(label.getString().length(), 8));
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        stringRenderer.addString(glString, lastX, lastY, x, y, BufferType.GUI);
    }

    public void render(BufferType bufferType, float lastX, float lastY, float x, float y) {
        stringRenderer.addString(glString, lastX, lastY, x, y, bufferType);
    }

    public void packGlyphs(int x, int y) {
        if (label.getMaxWidth() > 0) {
            stringGeometryBuilder.createString(glString, glyphsBuilder, label.getString(), x,
                    Math.round(glyphsBuilder.getTopOffset(label.getString(), label.getFontSize())) + y,
                    label.getFontSize(), color.x, color.y, color.z, color.w, label.getMaxWidth(), label.getOffsetType(), 0,
                    label.isShadow(), label.getShadowOffsetX(), label.getShadowOffsetY());
        } else {
            stringGeometryBuilder.createString(glString, glyphsBuilder, label.getString(), x,
                    Math.round(glyphsBuilder.getTopOffset(label.getString(), label.getFontSize())) + y,
                    label.getFontSize(), color.x, color.y, color.z, color.w, label.getOffsetType(), label.isShadow(),
                    label.getShadowOffsetX(), label.getShadowOffsetY());
        }

        label.setHeight(glString.getHeight());
    }

    public void scale(float x, float y) {
        FloatBuffer stringVertexBuffer = glString.getVertexBuffer();
        for (int i = 0, vertexDataSize = stringVertexBuffer.remaining(); i < vertexDataSize; i += 4) {
            stringVertexBuffer.put(i, stringVertexBuffer.get(i) * x);
            stringVertexBuffer.put(i + 1, stringVertexBuffer.get(i + 1) * y);
        }
    }
}
