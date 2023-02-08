package net.bfsr.client.render.font.string;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.gui.AbstractGuiObject;
import net.bfsr.client.render.BufferType;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.StringCache;
import net.bfsr.client.render.font.StringOffsetType;
import net.bfsr.core.Core;
import org.joml.Vector4f;

public class StringObject extends AbstractGuiObject {
    @Getter
    private String string;
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
        this.x = x;
        this.y = y;
    }

    public void update(String string) {
        this.string = string;
        compile();
    }

    public StringObject compile() {
        Core.getCore().getRenderer().getStringRenderer().createString(glString, stringCache, string, x, y, fontSize, color.x, color.y, color.z, color.w, stringOffsetType);
        return this;
    }

    public StringObject setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    @Override
    public StringObject setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        compile();
        return this;
    }

    @Override
    public void setX(int x) {
        this.x = x;
        compile();
    }

    @Override
    public void setY(int y) {
        this.y = y;
        compile();
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    public int getCursorPositionInLine(float mouseX) {
        return stringCache.getCursorPositionInLine(string, mouseX);
    }

    public int sizeStringToWidth(int width) {
        return stringCache.sizeStringToWidth(string, width);
    }

    public String trimStringToWidth(int width) {
        return stringCache.trimStringToWidth(string, width);
    }

    public int getStringWidth() {
        return stringCache.getStringWidth(string, fontSize);
    }

    @Override
    public void render() {
        InstancedRenderer.INSTANCE.addToRenderPipeLine(glString, BufferType.GUI);
    }

    @Override
    public int getY() {
        return (int) y;
    }

    @Override
    public int getHeight() {
        return glString.getHeight();
    }
}
