package net.bfsr.engine.gui.renderer;

import lombok.Getter;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.MaterialType;
import net.bfsr.engine.renderer.buffer.AbstractBuffersHolder;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.font.glyph.GlyphData;
import net.bfsr.engine.renderer.font.string.StringGeometry;
import net.bfsr.engine.renderer.font.string.StringGeometryBuilder;

import java.nio.ByteBuffer;
import java.util.List;

import static net.bfsr.engine.renderer.AbstractSpriteRenderer.COMMAND_SIZE_IN_BYTES;

public class LabelRenderer extends GuiObjectRenderer {
    private final AbstractSpriteRenderer spriteRenderer = renderer.getSpriteRenderer();
    private final StringGeometryBuilder stringGeometryBuilder = renderer.getStringGeometryBuilder();
    @Getter
    private final StringGeometry stringGeometry = new StringGeometry();
    private final Label label;
    private final Font font;

    private ByteBuffer commandBuffer;
    private long commandBufferAddress;

    private final BufferType bufferType;

    public LabelRenderer(Label label, Font font, BufferType bufferType) {
        super(label);
        this.label = label;
        this.font = font;
        this.bufferType = bufferType;
    }

    public void packGlyphs(float x, float y) {
        stringGeometry.clear();

        int topOffset = Math.round(font.getTopOffset(label.getString(), label.getFontSize()));
        stringGeometryBuilder.createString(stringGeometry, font, label.getString(), x, y + label.getHeight() - topOffset,
                label.getFontSize(), color.x, color.y, color.z, color.w, label.getMaxWidth(), label.getOffsetType(),
                label.isShadow(), label.getShadowOffsetX(), label.getShadowOffsetY(), spriteRenderer);
    }

    private void putCommandData(int offset, int value) {
        renderer.putValue(commandBufferAddress + (offset & 0xFFFF_FFFFL), value);
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
            commandBuffer = renderer.createByteBuffer(glyphsData.size() * COMMAND_SIZE_IN_BYTES);
            commandBufferAddress = renderer.getAddress(commandBuffer);

            for (int i = 0, commandDataOffset = 0; i < glyphsData.size(); i++, commandDataOffset += COMMAND_SIZE_IN_BYTES) {
                GlyphData glyphData = glyphsData.get(i);
                int id = spriteRenderer.add(x + glyphData.getX(), y + glyphData.getY(), glyphData.getWidth(),
                        glyphData.getHeight(), glyphData.getR(), glyphData.getG(), glyphData.getB(), glyphData.getA(),
                        glyphData.getTextureHandle(), MaterialType.FONT, buffersHolder);
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
    public void render(int mouseX, int mouseY) {
        if (idList.size() > 0) {
            spriteRenderer.addDrawCommand(commandBufferAddress, idList.size(), bufferType);
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
            renderer.memFree(commandBuffer);
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
