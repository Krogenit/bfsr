package net.bfsr.client.renderer.font.string;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringCache;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.StringRenderer;
import org.joml.Vector4f;

public class StringObject extends AbstractGuiObject {
    @Getter
    private String string;
    private float lastX, lastY;
    private float x, y;
    @Getter
    private int fontSize;
    @Getter
    private final StringCache stringCache;
    @Getter
    private final Vector4f color = new Vector4f();
    @Setter
    private StringOffsetType stringOffsetType;
    private final GLString glString = new GLString();

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

    public StringObject(FontType font, String string, float x, float y, int fontSize) {
        this(font, string, x, y, fontSize, StringOffsetType.DEFAULT);
    }

    public StringObject(FontType font, int fontSize, float r, float g, float b, float a) {
        this(font, "", 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public StringObject(FontType font, String string, float x, float y, int fontSize, StringOffsetType stringOffsetType) {
        this(font, string, x, y, fontSize, 1.0f, 1.0f, 1.0f, 1.0f, stringOffsetType);
    }

    public StringObject(FontType font, String string, int fontSize, float r, float g, float b, float a) {
        this(font, string, 0, 0, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    protected StringObject(FontType font, String string, float x, float y, int fontSize, float r, float g, float b, float a) {
        this(font, string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    protected StringObject(FontType font, String string, float x, float y, int fontSize, float r, float g, float b, float a, StringOffsetType stringOffsetType) {
        this.stringCache = font.getStringCache();
        this.string = string;
        this.fontSize = fontSize;
        this.stringOffsetType = stringOffsetType;
        this.color.set(r, g, b, a);
        this.glString.init(Math.max(string.length(), 8));
        this.x = lastX = x;
        this.y = lastY = y;
    }

    @Override
    public void update() {
        lastX = x;
        lastY = y;
    }

    public StringObject compile() {
        Core.get().getRenderer().getStringGeometryBuilder().createString(glString, stringCache, string, 0, 0, fontSize, color.x, color.y, color.z, color.w, stringOffsetType);
        return this;
    }

    @Override
    public void render() {
        StringRenderer.get().addStringInterpolated(glString, lastX, lastY, x, y, BufferType.GUI);
    }

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
    public void resize(int width, int height) {
        super.resize(width, height);
        update();
    }

    public int sizeStringToWidth(int width) {
        return stringCache.sizeStringToWidth(string, width);
    }

    public String trimStringToWidth(int width) {
        return stringCache.trimStringToWidth(string, width);
    }

    public int getCursorPositionInLine(float mouseX) {
        return stringCache.getCursorPositionInLine(string, mouseX);
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
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    @Override
    public int getY() {
        return (int) y;
    }

    public int getWidth() {
        return stringCache.getStringWidth(string, fontSize);
    }

    @Override
    public int getHeight() {
        return glString.getHeight();
    }
}