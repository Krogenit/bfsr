package net.bfsr.client.render.font.string;

import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.StringOffsetType;

public class DynamicString extends StringObject {
    public DynamicString(FontType font) {
        super(font);
    }

    public DynamicString(FontType font, int fontSize) {
        super(font, fontSize);
    }

    public DynamicString(FontType font, int fontSize, StringOffsetType stringOffsetType) {
        super(font, fontSize, stringOffsetType);
    }

    public DynamicString(FontType font, String string, int fontSize, StringOffsetType stringOffsetType) {
        super(font, string, fontSize, stringOffsetType);
    }

    public DynamicString(FontType font, String string, float x, float y, int fontSize) {
        super(font, string, x, y, fontSize);
    }

    public DynamicString(FontType font, String string, float x, float y, int fontSize, StringOffsetType stringOffsetType) {
        super(font, string, x, y, fontSize, stringOffsetType);
    }

    public DynamicString(FontType font, int fontSize, float r, float g, float b, float a) {
        super(font, "", fontSize, r, g, b, a);
    }

    public DynamicString(FontType font, String string, int fontSize, float r, float g, float b, float a) {
        super(font, string, fontSize, r, g, b, a);
    }

    public DynamicString(FontType font, String string, float x, float y, int fontSize, float r, float g, float b, float a) {
        super(font, string, x, y, fontSize, r, g, b, a);
    }

    public DynamicString(FontType font, String string, float x, float y, int fontSize, float r, float g, float b, float a, StringOffsetType stringOffsetType) {
        super(font, string, x, y, fontSize, r, g, b, a, stringOffsetType);
    }

    @Override
    protected GLString createGLString() {
        return new DynamicGLString();
    }
}
