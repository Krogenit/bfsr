package net.bfsr.engine.gui.component;

import lombok.Getter;
import net.bfsr.engine.gui.renderer.LabelRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringCache;
import net.bfsr.engine.renderer.font.StringOffsetType;

public class Label extends GuiObject {
    @Getter
    private String string;
    @Getter
    private int fontSize;
    @Getter
    private final StringCache stringCache;
    @Getter
    private StringOffsetType offsetType;
    @Getter
    private boolean shadow;
    @Getter
    private int shadowOffsetX, shadowOffsetY;
    private final LabelRenderer labelRenderer;
    private int ascent;

    public Label(FontType font) {
        this(font, 14);
    }

    public Label(FontType font, String string) {
        this(font, string, 0, 0, 14);
    }

    protected Label(FontType font, int fontSize) {
        this(font, "", 0, 0, fontSize);
    }

    public Label(FontType font, int fontSize, StringOffsetType offsetType) {
        this(font, "", 0, 0, fontSize, offsetType);
    }

    public Label(FontType font, String string, int fontSize) {
        this(font, string, fontSize, StringOffsetType.DEFAULT);
    }

    public Label(FontType font, String string, int fontSize, StringOffsetType offsetType) {
        this(font, string, 0, 0, fontSize, offsetType);
    }

    public Label(FontType font, String string, int x, int y, int fontSize) {
        this(font, string, x, y, fontSize, StringOffsetType.DEFAULT);
    }

    public Label(FontType font, int fontSize, float r, float g, float b, float a) {
        this(font.getStringCache(), "", 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public Label(StringCache stringCache, int fontSize, float r, float g, float b, float a) {
        this(stringCache, "", 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public Label(FontType font, String string, int x, int y, int fontSize, StringOffsetType offsetType) {
        this(font.getStringCache(), string, x, y, fontSize, 1.0f, 1.0f, 1.0f, 1.0f, offsetType);
    }

    public Label(FontType font, String string, int fontSize, float r, float g, float b, float a) {
        this(font.getStringCache(), string, 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public Label(StringCache stringCache, String string, int fontSize, float r, float g, float b, float a) {
        this(stringCache, string, 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    protected Label(FontType font, String string, int x, int y, int fontSize, float r, float g, float b, float a) {
        this(font.getStringCache(), string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    protected Label(StringCache stringCache, String string, int x, int y, int fontSize, float r, float g, float b, float a,
                    StringOffsetType offsetType) {
        super(x, y, stringCache.getStringWidth(string, fontSize), 0);
        this.stringCache = stringCache;
        this.string = string;
        this.fontSize = fontSize;
        this.offsetType = offsetType;
        this.color.set(r, g, b, a);
        this.ascent = stringCache.getAscent(string, fontSize);
        this.setCanBeHovered(false);
        setRenderer(this.labelRenderer = new LabelRenderer(this));
    }

    public Label compileAtOrigin() {
        labelRenderer.compileAtOrigin();
        return this;
    }

    public Label compile() {
        labelRenderer.compile();
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
        setWidth(stringCache.getStringWidth(string, fontSize));
        ascent = stringCache.getAscent(string, fontSize);
        return this;
    }

    public Label setStringAndCompile(String string) {
        this.string = string;
        setWidth(stringCache.getStringWidth(string, fontSize));
        ascent = stringCache.getAscent(string, fontSize);
        return compile();
    }

    public Label setStringAndCompileAtOrigin(String string) {
        this.string = string;
        setWidth(stringCache.getStringWidth(string, fontSize));
        ascent = stringCache.getAscent(string, fontSize);
        return compileAtOrigin();
    }

    public Label setFontSize(int fontSize) {
        this.fontSize = fontSize;
        setWidth(stringCache.getStringWidth(string, fontSize));
        ascent = stringCache.getAscent(string, fontSize);
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
        return this;
    }

    public Label setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public Label setShadowOffsetX(int shadowOffsetX) {
        this.shadowOffsetX = shadowOffsetX;
        return this;
    }

    public Label setShadowOffsetY(int shadowOffsetY) {
        this.shadowOffsetY = shadowOffsetY;
        return this;
    }

    public Label setOffsetType(StringOffsetType offsetType) {
        this.offsetType = offsetType;
        return this;
    }

    int getCursorPositionInLine(float mouseX) {
        return stringCache.getCursorPositionInLine(string, mouseX, fontSize);
    }

    @Override
    public int getY() {
        return y - ascent;
    }

    @Override
    public int getSceneY() {
        return getY() + parent.getSceneY();
    }
}