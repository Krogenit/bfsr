package net.bfsr.client.render.font.string;

import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.StringOffsetType;

public class StaticString extends StringObject {
    public StaticString(FontType font) {
        super(font);
    }

    public StaticString(FontType font, String string) {
        super(font, string);
    }

    protected StaticString(FontType font, int fontSize) {
        super(font, fontSize);
    }

    public StaticString(FontType font, String string, int fontSize) {
        super(font, string, fontSize);
    }

    public StaticString(FontType font, String string, int fontSize, StringOffsetType stringOffsetType) {
        super(font, string, fontSize, stringOffsetType);
    }

    public StaticString(FontType font, String string, float x, float y, int fontSize) {
        super(font, string, x, y, fontSize);
    }

    protected StaticString(FontType font, String string, float x, float y, int fontSize, StringOffsetType stringOffsetType) {
        super(font, string, x, y, fontSize, stringOffsetType);
    }

    public StaticString(FontType font, String string, int fontSize, float r, float g, float b, float a) {
        super(font, string, 0, 0, fontSize, r, g, b, a);
    }

    protected StaticString(FontType font, String string, float x, float y, int fontSize, float r, float g, float b, float a) {
        super(font, string, x, y, fontSize, r, g, b, a);
    }

    protected StaticString(FontType font, String string, float x, float y, int fontSize, float r, float g, float b, float a, StringOffsetType stringOffsetType) {
        super(font, string, x, y, fontSize, r, g, b, a, stringOffsetType);
    }

    @Override
    protected GLString createGLString() {
        return new GLString();
    }
}
