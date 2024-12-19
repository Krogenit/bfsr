package net.bfsr.engine.gui.renderer;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.StringGeometryBuilder;
import net.bfsr.engine.renderer.font.glyph.GlyphData;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.string.StringGeometry;

import java.nio.IntBuffer;
import java.util.List;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.COMMAND_SIZE;
import static net.bfsr.engine.renderer.AbstractSpriteRenderer.FOUR_BYTES_ELEMENT_SHIFT;

public class LabelRenderer extends GuiObjectRenderer {
    private final AbstractSpriteRenderer spriteRenderer = Engine.renderer.spriteRenderer;
    private final StringGeometryBuilder stringGeometryBuilder = Engine.renderer.stringGeometryBuilder;
    @Getter
    private final StringGeometry stringGeometry = new StringGeometry();
    private final Label label;
    private final GlyphsBuilder glyphsBuilder;

    private IntBuffer commandBuffer;
    private long commandBufferAddress;

    private final BufferType bufferType;

    public LabelRenderer(Label label, GlyphsBuilder glyphsBuilder, BufferType bufferType) {
        super(label);
        this.label = label;
        this.glyphsBuilder = glyphsBuilder;
        this.bufferType = bufferType;
    }

    public void packGlyphs(float x, float y) {
        stringGeometry.clear();

        int topOffset = Math.round(glyphsBuilder.getTopOffset(label.getString(), label.getFontSize()));
        if (label.getMaxWidth() > 0) {
            stringGeometryBuilder.createString(stringGeometry, glyphsBuilder, label.getString(), x, y + label.getHeight() - topOffset,
                    label.getFontSize(), color.x, color.y, color.z, color.w, label.getMaxWidth(), label.getOffsetType(), 0,
                    label.isShadow(), label.getShadowOffsetX(), label.getShadowOffsetY(), spriteRenderer);
        } else {
            stringGeometryBuilder.createString(stringGeometry, glyphsBuilder, label.getString(), x, y + label.getHeight() - topOffset,
                    label.getFontSize(), color.x, color.y, color.z, color.w, label.getOffsetType(), label.isShadow(),
                    label.getShadowOffsetX(), label.getShadowOffsetY(), spriteRenderer);
        }
    }

    private void putCommandData(int offset, int value) {
        Engine.renderer.putValue(commandBufferAddress + ((offset & 0xFFFF_FFFFL) << FOUR_BYTES_ELEMENT_SHIFT), value);
    }

    @Override
    protected void create() {
        create(bufferType);
    }

    public void create(BufferType bufferType) {
        create(spriteRenderer.getBuffersHolder(bufferType));
    }

    public void create(AbstractBuffersHolder buffersHolder) {
        create(buffersHolder, label.getSceneX(), label.getSceneY());
    }

    public void create(float x, float y) {
        create(spriteRenderer.getBuffersHolder(bufferType), x, y);
    }

    public void create(AbstractBuffersHolder buffersHolder, float x, float y) {
        List<GlyphData> glyphsData = stringGeometry.getGlyphsData();
        if (glyphsData.size() > 0) {
            commandBuffer = Engine.renderer.createIntBuffer(glyphsData.size() * COMMAND_SIZE);
            commandBufferAddress = Engine.renderer.getAddress(commandBuffer);

            for (int i = 0, commandDataOffset = 0; i < glyphsData.size(); i++, commandDataOffset += COMMAND_SIZE) {
                GlyphData glyphData = glyphsData.get(i);
                int id = spriteRenderer.add(x + glyphData.getX(), y + glyphData.getY(), glyphData.getWidth(),
                        glyphData.getHeight(), glyphData.getR(), glyphData.getG(), glyphData.getB(), glyphData.getA(),
                        glyphData.getTextureHandle(), 1, buffersHolder);
                glyphData.setBaseInstance(id);
                putCommandData(commandDataOffset, AbstractSpriteRenderer.QUAD_INDEX_COUNT);
                putCommandData(commandDataOffset + AbstractSpriteRenderer.INSTANCE_COUNT_OFFSET, 1);
                putCommandData(commandDataOffset + AbstractSpriteRenderer.FIRST_INDEX_OFFSET, 0);
                putCommandData(commandDataOffset + AbstractSpriteRenderer.BASE_VERTEX_OFFSET, glyphData.getBaseVertex());
                putCommandData(commandDataOffset + AbstractSpriteRenderer.BASE_INSTANCE_OFFSET, id);
                idList.add(id);
            }
        }
    }

    @Override
    public void render() {
        if (idList.size() > 0) {
            spriteRenderer.addDrawCommand(commandBuffer, stringGeometry.getGlyphsCount(), bufferType);
        }
    }

    @Override
    protected void setLastUpdateValues() {
        List<GlyphData> glyphsData = stringGeometry.getGlyphsData();
        for (int i = 0; i < glyphsData.size(); i++) {
            GlyphData glyphData = glyphsData.get(i);
            guiRenderer.setLastPosition(glyphData.getBaseInstance(), lastX + glyphData.getX(), lastY + glyphData.getY());
        }
    }

    public void updateLastPosition(float x, float y) {
        List<GlyphData> glyphsData = stringGeometry.getGlyphsData();
        for (int i = 0; i < glyphsData.size(); i++) {
            GlyphData glyphData = glyphsData.get(i);
            spriteRenderer.setLastPosition(glyphData.getBaseInstance(), bufferType, x + glyphData.getX(), y + glyphData.getY());
        }
    }

    @Override
    public void updatePosition() {
        updatePosition(guiObject.getSceneX(), guiObject.getSceneY());
    }

    @Override
    public void updatePosition(int x, int y) {
        updatePosition((float) x, (float) y);
    }

    public void updatePosition(float x, float y) {
        List<GlyphData> glyphsData = stringGeometry.getGlyphsData();
        for (int i = 0; i < glyphsData.size(); i++) {
            GlyphData glyphData = glyphsData.get(i);
            spriteRenderer.setPosition(glyphData.getBaseInstance(), bufferType, x + glyphData.getX(), y + glyphData.getY());
        }
    }

    public void scale(float sx, float sy) {
        List<GlyphData> glyphsData = stringGeometry.getGlyphsData();
        for (int i = 0; i < glyphsData.size(); i++) {
            GlyphData glyphData = glyphsData.get(i);
            glyphData.scale(sx, sy);
        }
    }

    @Override
    public void remove() {
        super.remove();

        if (commandBuffer != null) {
            Engine.renderer.memFree(commandBuffer);
            commandBuffer = null;
        }
    }

    @Override
    protected void removeRenderIds() {
        while (idList.size() > 0) {
            guiRenderer.removeObject(idList.removeInt(0), bufferType);
        }
    }
}
