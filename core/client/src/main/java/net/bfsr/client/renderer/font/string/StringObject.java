package net.bfsr.client.renderer.font.string;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.SimpleGuiObject;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringCache;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.StringRenderer;

public class StringObject extends SimpleGuiObject {
    @Getter
    private String string;
    @Getter
    private int fontSize;
    @Getter
    private final StringCache stringCache;
    @Setter
    private StringOffsetType stringOffsetType;
    protected final GLString glString = new GLString();

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

    protected StringObject(StringCache stringCache, String string, int x, int y, int fontSize, float r, float g, float b, float a, StringOffsetType stringOffsetType) {
        super(x, y, 0, 0);
        this.stringCache = stringCache;
        this.string = string;
        this.fontSize = fontSize;
        this.stringOffsetType = stringOffsetType;
        this.color.set(r, g, b, a);
        this.glString.init(Math.max(string.length(), 8));
    }

    public StringObject compile() {
        Core.get().getRenderer().getStringGeometryBuilder().createString(glString, stringCache, string, 0, 0, fontSize, color.x, color.y, color.z, color.w, stringOffsetType);
        return this;
    }

    @Override
    public void updateMouseHover() {}

    @Override
    public void render() {
        StringRenderer.get().addStringInterpolated(glString, lastX, lastY, x, y, BufferType.GUI);
    }

    @Override
    public void renderNoInterpolation() {
        StringRenderer.get().addString(glString, x, y, BufferType.GUI);
    }

    public void render(BufferType bufferType) {
        StringRenderer.get().addString(glString, bufferType);
    }

    public void render(BufferType bufferType, float x, float y) {
        StringRenderer.get().addString(glString, x, y, bufferType);
    }

    public void render(BufferType bufferType, float x, float y, float scaleX, float scaleY) {
        StringRenderer.get().addString(glString, x, y, scaleX, scaleY, bufferType);
    }

    public void render(BufferType bufferType, float lastX, float lastY, float x, float y, float scaleX, float scaleY) {
        StringRenderer.get().addString(glString, lastX, lastY, x, y, scaleX, scaleY, bufferType);
    }

    public void renderWithShadow(BufferType bufferType, float lastX, float lastY, float x, float y, float scaleX, float scaleY, float shadowOffsetX, float shadowOffsetY) {
        StringRenderer.get().addStringWithShadow(glString, lastX, lastY, x, y, scaleX, scaleY, shadowOffsetX, shadowOffsetY, bufferType);
    }

    @Override
    public void onScreenResize(int width, int height) {
        super.onScreenResize(width, height);
        update();
    }

    public int sizeStringToWidth(int width) {
        return stringCache.sizeStringToWidth(string, width);
    }

    public String trimStringToWidth(int width) {
        return stringCache.trimStringToWidth(string, width);
    }

    public int getCursorPositionInLine(float mouseX) {
        return stringCache.getCursorPositionInLine(string, mouseX, fontSize);
    }

    public void setString(String string) {
        this.string = string;
        compile();
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