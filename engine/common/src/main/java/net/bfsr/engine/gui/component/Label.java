package net.bfsr.engine.gui.component;

import lombok.Getter;
import net.bfsr.engine.gui.renderer.LabelRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;

public class Label extends GuiObject {
    @Getter
    private String string;
    @Getter
    private int fontSize;
    @Getter
    private final GlyphsBuilder glyphsBuilder;
    @Getter
    private StringOffsetType offsetType;
    @Getter
    private boolean shadow;
    @Getter
    private int shadowOffsetX, shadowOffsetY;
    private final LabelRenderer labelRenderer;
    @Getter
    private float ascent;
    @Getter
    private int maxWidth;

    public Label(Font font) {
        this(font, 14);
    }

    public Label(Font font, String string) {
        this(font, string, 0, 0, 14);
    }

    protected Label(Font font, int fontSize) {
        this(font, "", 0, 0, fontSize);
    }

    public Label(Font font, int fontSize, StringOffsetType offsetType) {
        this(font, "", 0, 0, fontSize, offsetType);
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
        this(font, "", 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public Label(Font font, String string, int x, int y, int fontSize, StringOffsetType offsetType) {
        this(font, string, x, y, fontSize, 1.0f, 1.0f, 1.0f, 1.0f, offsetType);
    }

    public Label(Font font, String string, int fontSize, float r, float g, float b, float a) {
        this(font, string, 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    protected Label(Font font, String string, int x, int y, int fontSize, float r, float g, float b, float a) {
        this(font, string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    protected Label(Font font, String string, int x, int y, int fontSize, float r, float g, float b, float a,
                    StringOffsetType offsetType) {
        super(x, y, font.getGlyphsBuilder().getWidth(string, fontSize), 0);
        this.glyphsBuilder = font.getGlyphsBuilder();
        this.string = string;
        this.fontSize = fontSize;
        this.offsetType = offsetType;
        this.color.set(r, g, b, a);
        this.ascent = glyphsBuilder.getAscent(string, fontSize);
        this.setCanBeHovered(false);
        setRenderer(this.labelRenderer = new LabelRenderer(this, glyphsBuilder));
        packGlyphs();
    }

    public Label packGlyphs() {
        labelRenderer.packGlyphs(0, 0);
        return this;
    }

    public void scale(float x, float y) {
        labelRenderer.scale(x, y);
    }

    public void render(BufferType bufferType, float lastX, float lastY, float x, float y) {
        labelRenderer.render(bufferType, this.lastX + lastX, this.lastY + lastY, this.x + x, this.y + y);
    }

    @Override
    public Label atTopLeft(int x, int y) {
        super.atTopLeft(x, y);
        return this;
    }

    public Label setString(String string) {
        this.string = string;
        setWidth(glyphsBuilder.getWidth(string, fontSize));
        ascent = glyphsBuilder.getAscent(string, fontSize);
        return packGlyphs();
    }

    public Label setFontSize(int fontSize) {
        this.fontSize = fontSize;
        setWidth(glyphsBuilder.getWidth(string, fontSize));
        ascent = glyphsBuilder.getAscent(string, fontSize);
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