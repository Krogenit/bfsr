package net.bfsr.engine.gui.component;

import lombok.Getter;
import net.bfsr.engine.gui.renderer.LabelRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;

@Getter
public class Label extends GuiObject {
    private String string;
    private int fontSize;
    private final GlyphsBuilder glyphsBuilder;
    private StringOffsetType offsetType;
    private boolean shadow;
    private int shadowOffsetX, shadowOffsetY;
    private final LabelRenderer labelRenderer;
    private int maxWidth;

    public Label(Font font) {
        this(font, 14);
    }

    public Label(Font font, String string) {
        this(font, string, 0, 0, 14);
    }

    public Label(Font font, int fontSize) {
        this(font, "", 0, 0, fontSize);
    }

    public Label(Font font, int fontSize, StringOffsetType offsetType) {
        this(font, "", 0, 0, fontSize, offsetType);
    }

    public Label(Font font, int fontSize, StringOffsetType offsetType, BufferType bufferType) {
        this(font, "", fontSize, 0, 0, offsetType, bufferType);
    }

    public Label(Font font, String string, int fontSize) {
        this(font, string, fontSize, StringOffsetType.DEFAULT);
    }

    public Label(Font font, String string, int fontSize, StringOffsetType offsetType) {
        this(font, string, 0, 0, fontSize, offsetType);
    }

    public Label(Font font, String string, int x, int y, int fontSize) {
        this(font, string, x, y, fontSize, StringOffsetType.DEFAULT);
    }

    public Label(Font font, int fontSize, float r, float g, float b, float a) {
        this(font, "", 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT, BufferType.GUI);
    }

    public Label(Font font, String string, int x, int y, int fontSize, StringOffsetType offsetType) {
        this(font, string, x, y, fontSize, 1.0f, 1.0f, 1.0f, 1.0f, offsetType, BufferType.GUI);
    }

    public Label(Font font, String string, int fontSize, int x, int y, StringOffsetType offsetType, BufferType bufferType) {
        this(font, string, x, y, fontSize, 1.0f, 1.0f, 1.0f, 1.0f, offsetType, bufferType);
    }

    public Label(Font font, String string, int fontSize, float r, float g, float b, float a) {
        this(font, string, 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT, BufferType.GUI);
    }

    protected Label(Font font, String string, int x, int y, int fontSize, float r, float g, float b, float a) {
        this(font, string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT, BufferType.GUI);
    }

    protected Label(Font font, String string, int x, int y, int fontSize, float r, float g, float b, float a,
                    StringOffsetType offsetType, BufferType bufferType) {
        super(x, y, font.getGlyphsBuilder().getWidth(string, fontSize), Math.round(font.getGlyphsBuilder().getHeight(string, fontSize, 0)));
        this.glyphsBuilder = font.getGlyphsBuilder();
        this.string = string;
        this.fontSize = fontSize;
        this.offsetType = offsetType;
        this.color.set(r, g, b, a);
        this.setCanBeHovered(false);
        setRenderer(this.labelRenderer = new LabelRenderer(this, glyphsBuilder, bufferType));
        packGlyphs();
    }

    private void packGlyphs() {
        packGlyphs(0, 0);
    }

    private Label packGlyphs(float x, float y) {
        if (isOnScene) {
            renderer.remove();
            labelRenderer.packGlyphs(x, y);
            renderer.addToScene();
        } else {
            labelRenderer.packGlyphs(x, y);
        }

        return this;
    }

    public void updatePosition(float x, float y) {
        labelRenderer.updatePosition(x, y);
    }

    public void updateLastPosition(float x, float y) {
        labelRenderer.updateLastPosition(x, y);
    }

    private void updateSize() {
        setWidth(glyphsBuilder.getWidth(string, fontSize));
        setHeight(Math.round(glyphsBuilder.getHeight(string, fontSize, maxWidth)));
    }

    public void scale(float x, float y) {
        labelRenderer.scale(x, y);
    }

    public void render() {
        labelRenderer.render();
    }

    @Override
    public Label atTopLeft(int x, int y) {
        super.atTopLeft(x, y);
        return this;
    }

    @Override
    public Label atBottomLeft(int x, int y) {
        super.atBottomLeft(x, y);
        return this;
    }

    public Label setString(String string) {
        return setString(string, 0, 0);
    }

    public Label setString(String string, float x, float y) {
        this.string = string;
        updateSize();
        return packGlyphs(x, y);
    }

    public Label setFontSize(int fontSize) {
        this.fontSize = fontSize;
        updateSize();
        return this;
    }

    @Override
    public Label setPosition(int x, int y) {
        super.setPosition(x, y);
        return this;
    }

    @Override
    public Label setColor(float r, float g, float b, float a) {
        super.setColor(r, g, b, a);
        packGlyphs();
        return this;
    }

    public Label setColorAlpha(float a) {
        color.w = a;
        packGlyphs();
        return this;
    }

    public Label setShadow(boolean shadow) {
        this.shadow = shadow;
        packGlyphs();
        return this;
    }

    public Label setShadowOffsetX(int shadowOffsetX) {
        this.shadowOffsetX = shadowOffsetX;
        packGlyphs();
        return this;
    }

    public Label setShadowOffsetY(int shadowOffsetY) {
        this.shadowOffsetY = shadowOffsetY;
        packGlyphs();
        return this;
    }

    public Label setOffsetType(StringOffsetType offsetType) {
        this.offsetType = offsetType;
        packGlyphs();
        return this;
    }

    public Label setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        updateSize();
        packGlyphs();
        return this;
    }

    int getCursorPositionInLine(float mouseX) {
        return glyphsBuilder.getCursorPositionInLine(string, mouseX, fontSize);
    }

    public int getCenteredOffsetY(int height) {
        return glyphsBuilder.getCenteredOffsetY(string, height, fontSize);
    }

    public float getColorAlpha() {
        return color.w;
    }
}