package net.bfsr.engine.renderer.font.glyph;

import lombok.Setter;

@Setter
public abstract class GlyphsBuilder {
    protected static final char SPACE = ' ';
    protected static final char NEW_LINE = '\n';

    public abstract GlyphsData getGlyphsData(String text, int fontSize);

    public abstract int getWidth(String string, int fontSize, int maxWidth, boolean breakAtSpaces);

    public abstract int getWidth(String string, int fontSize);

    public abstract float getHeight(String string, int fontSize);

    public abstract float getLineHeight(int fontSize);

    public abstract float getAscent(String string, int fontSize);

    public abstract float getDescent(String string, int fontSize);

    public abstract float getLeading(String string, int fontSize);

    public abstract int getCenteredOffsetY(String string, int height, int fontSize);

    public abstract int getCursorPositionInLine(String string, float mouseX, int fontSize);

    public abstract float getTopOffset(String string, int fontSize);

    public int getHeight(String string, int fontSize, int maxWidth, int indent) {
        int offset = 0;
        int height = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == NEW_LINE) {
                height = (int) (height + getLineHeight(fontSize) * indent);
            }
        }
        return height + getTrimmedStringHeight(string.substring(offset).trim(), fontSize, maxWidth, indent);
    }

    private int getTrimmedStringHeight(String string, int fontSize, int maxWidth, int indent) {
        int height = 0;
        do {
            String temp = trimStringToWidthSaveWords(string, fontSize, maxWidth);
            string = string.replace(temp, "").trim();
            height = (int) (height + (getHeight(temp, fontSize) + indent));
        } while (!string.isEmpty());

        return height;
    }

    String trimStringToWidthSaveWords(String string, int fontSize, int width) {
        return string.substring(0, getWidth(string, fontSize, width, true));
    }
}
