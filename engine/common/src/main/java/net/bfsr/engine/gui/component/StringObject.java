package net.bfsr.engine.gui.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.gui.object.SimpleGuiObject;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringCache;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.font.string.AbstractGLString;
import net.bfsr.engine.renderer.font.string.AbstractStringGeometryBuilder;
import net.bfsr.engine.renderer.font.string.AbstractStringRenderer;

import java.nio.FloatBuffer;

public class StringObject extends SimpleGuiObject {
    private final AbstractStringRenderer stringRenderer = renderer.stringRenderer;
    private final AbstractStringGeometryBuilder stringGeometryBuilder = renderer.stringGeometryBuilder;
    @Getter
    private String string;
    @Getter
    private int fontSize;
    @Getter
    private final StringCache stringCache;
    @Setter
    private StringOffsetType stringOffsetType;
    private final AbstractGLString glString = stringRenderer.createGLString();
    private boolean shadow;
    private int shadowOffsetX, shadowOffsetY;

    public StringObject(FontType font) {
        this(font, 14);
    }

    public StringObject(FontType font, String string) {
        this(font, string, 0, 0, 14);
    }

    protected StringObject(FontType font, int fontSize) {
        this(font, "", 0, 0, fontSize);
    }

    public StringObject(FontType font, int fontSize, StringOffsetType stringOffsetType) {
        this(font, "", 0, 0, fontSize, stringOffsetType);
    }

    public StringObject(FontType font, String string, int fontSize) {
        this(font, string, fontSize, StringOffsetType.DEFAULT);
    }

    public StringObject(FontType font, String string, int fontSize, StringOffsetType stringOffsetType) {
        this(font, string, 0, 0, fontSize, stringOffsetType);
    }

    public StringObject(FontType font, String string, int x, int y, int fontSize) {
        this(font, string, x, y, fontSize, StringOffsetType.DEFAULT);
    }

    public StringObject(FontType font, int fontSize, float r, float g, float b, float a) {
        this(font.getStringCache(), "", 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public StringObject(StringCache stringCache, int fontSize, float r, float g, float b, float a) {
        this(stringCache, "", 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public StringObject(FontType font, String string, int x, int y, int fontSize, StringOffsetType stringOffsetType) {
        this(font.getStringCache(), string, x, y, fontSize, 1.0f, 1.0f, 1.0f, 1.0f, stringOffsetType);
    }

    public StringObject(FontType font, String string, int fontSize, float r, float g, float b, float a) {
        this(font.getStringCache(), string, 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public StringObject(StringCache stringCache, String string, int fontSize, float r, float g, float b, float a) {
        this(stringCache, string, 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    protected StringObject(FontType font, String string, int x, int y, int fontSize, float r, float g, float b, float a) {
        this(font.getStringCache(), string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    protected StringObject(StringCache stringCache, String string, int x, int y, int fontSize, float r, float g, float b, float a,
                           StringOffsetType stringOffsetType) {
        super(x, y, 0, 0);
        this.stringCache = stringCache;
        this.string = string;
        this.fontSize = fontSize;
        this.stringOffsetType = stringOffsetType;
        this.color.set(r, g, b, a);
        this.glString.init(Math.max(string.length(), 8));
    }

    public StringObject compileAtOrigin() {
        stringGeometryBuilder.createString(glString, stringCache, string, 0, 0, fontSize, color.x, color.y, color.z, color.w,
                stringOffsetType, shadow, shadowOffsetX, shadowOffsetY);
        return this;
    }

    public StringObject compile() {
        stringGeometryBuilder.createString(glString, stringCache, string, x, y, fontSize, color.x, color.y, color.z, color.w,
                stringOffsetType, shadow, shadowOffsetX, shadowOffsetY);
        return this;
    }

    public void scale(float x, float y) {
        FloatBuffer stringVertexBuffer = glString.getVertexBuffer();
        for (int i = 0, vertexDataSize = stringVertexBuffer.remaining(); i < vertexDataSize; i += 4) {
            stringVertexBuffer.put(i, stringVertexBuffer.get(i) * x);
            stringVertexBuffer.put(i + 1, stringVertexBuffer.get(i + 1) * y);
        }
    }

    @Override
    public void updateMouseHover() {}

    @Override
    public void render() {
        stringRenderer.addString(glString, lastX, lastY, x, y, BufferType.GUI);
    }

    @Override
    public void renderNoInterpolation() {
        stringRenderer.addString(glString, BufferType.GUI);
    }

    public void render(BufferType bufferType, float lastX, float lastY, float x, float y) {
        stringRenderer.addString(glString, lastX, lastY, x, y, bufferType);
    }

    @Override
    public void onScreenResize(int width, int height) {
        super.onScreenResize(width, height);
        update();
    }

    public int getCursorPositionInLine(float mouseX) {
        return stringCache.getCursorPositionInLine(string, mouseX, fontSize);
    }

    public StringObject setString(String string) {
        this.string = string;
        return this;
    }

    public StringObject setStringAndCompile(String string) {
        this.string = string;
        return compile();
    }

    public StringObject setStringAndCompileAtOrigin(String string) {
        this.string = string;
        return compileAtOrigin();
    }

    public StringObject setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    @Override
    public StringObject setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public StringObject setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
        return this;
    }

    public StringObject setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public StringObject setShadowOffsetX(int shadowOffsetX) {
        this.shadowOffsetX = shadowOffsetX;
        return this;
    }

    public StringObject setShadowOffsetY(int shadowOffsetY) {
        this.shadowOffsetY = shadowOffsetY;
        return this;
    }

    @Override
    public int getY() {
        return y - getHeight();
    }

    @Override
    public int getYForScroll() {
        return y;
    }

    @Override
    public int getWidth() {
        return stringCache.getStringWidth(string, fontSize);
    }

    @Override
    public int getHeight() {
        return glString.getHeight();
    }
}