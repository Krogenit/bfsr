package net.bfsr.engine.renderer.font.glyph;

import lombok.Setter;

@Setter
public abstract class GlyphsBuilder {
    public static final char NEW_LINE = '\n';
    protected static final char SPACE = ' ';

    public abstract GlyphsData getGlyphsData(String text, int fontSize);

    public abstract int getWidth(String string, int fontSize, int maxWidth, boolean breakAtSpaces);

    public abstract int getWidth(String string, int fontSize);

    protected abstract float getHeight(String string, int fontSize);

    public float getHeight(String string, int fontSize, int maxWidth) {
        if (maxWidth > 0) {
            return getTrimmedStringHeight(string, fontSize, maxWidth);
        } else {
            return getHeight(string, fontSize);
        }
    }

    private float getTrimmedStringHeight(String string, int fontSize, int maxWidth) {
        float height = 0.0f;

        do {
            String temp = trimStringToWidthSaveWords(string, fontSize, maxWidth);
            string = string.replace(temp, "").trim();
            height += getHeight(temp, fontSize);
        } while (!string.isEmpty());

        return height;
    }

    private String trimStringToWidthSaveWords(String string, int fontSize, int width) {
        return string.substring(0, getWidth(string, fontSize, width, true));
    }

    public abstract float getLineHeight(int fontSize);

    public abstract float getAscent(String string, int fontSize);

    public abstract float getDescent(String string, int fontSize);

    public abstract float getLeading(String string, int fontSize);

    public abstract int getCenteredOffsetY(String string, int height, int fontSize);

    public abstract int getCursorPositionInLine(String string, float mouseX, int fontSize);

    public abstract float getTopOffset(String string, int fontSize);
}
