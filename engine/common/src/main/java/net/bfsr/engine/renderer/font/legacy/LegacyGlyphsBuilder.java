package net.bfsr.engine.renderer.font.legacy;

import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.font.glyph.GlyphsData;

import java.util.ArrayList;

public class LegacyGlyphsBuilder extends Font {
    private final StringCache stringCache;

    public LegacyGlyphsBuilder(String fontFile, boolean antialias) {
        this.stringCache = new StringCache(fontFile, antialias);
    }

    @Override
    public GlyphsData getGlyphsData(String text, int fontSize) {
        Entry entry = stringCache.cacheString(text, fontSize);

        Glyph[] glyphs = entry.glyphs;
        ArrayList<net.bfsr.engine.renderer.font.glyph.Glyph> glyphArrayList = new ArrayList<>(glyphs.length);
        for (int i = 0; i < glyphs.length; i++) {
            Glyph glyph = glyphs[i];
            glyphArrayList.add(new net.bfsr.engine.renderer.font.glyph.Glyph(glyph.x, glyph.y, (glyph.x + glyph.texture.width),
                    (glyph.y + glyph.texture.height), glyph.texture.u1, glyph.texture.v1, glyph.texture.u2, glyph.texture.v2,
                    glyph.texture.textureHandle, glyph.advance, text.charAt(glyph.stringIndex), false));
        }

        return new GlyphsData(glyphArrayList, entry.advance);
    }

    @Override
    public int getWidth(String string, int fontSize, int maxWidth, boolean breakAtSpaces) {
        return stringCache.getWidth(string, fontSize, maxWidth, breakAtSpaces);
    }

    @Override
    public float getHeight(String string, int fontSize) {
        float lineHeight = getLineHeight(fontSize);
        float totalHeight = lineHeight;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == NEW_LINE) {
                totalHeight += lineHeight;
            }
        }

        return totalHeight;
    }

    @Override
    public float getLineHeight(int fontSize) {
        return stringCache.getHeight("\n", fontSize);
    }

    @Override
    public int getWidth(String string, int fontSize) {
        return stringCache.getWidth(string, fontSize);
    }

    @Override
    public float getAscent(String string, int fontSize) {
        return stringCache.getAscent(string, fontSize);
    }

    @Override
    public float getDescent(String string, int fontSize) {
        return stringCache.getDescent(string, fontSize);
    }

    @Override
    public float getLeading(String string, int fontSize) {
        return stringCache.getLeading(string, fontSize);
    }

    @Override
    public int getCenteredOffsetY(String string, int height, int fontSize) {
        return stringCache.getCenteredOffsetY(string, height, fontSize);
    }

    @Override
    public int getCursorPositionInLine(String string, float mouseX, int fontSize) {
        return stringCache.getCursorPositionInLine(string, mouseX, fontSize);
    }

    @Override
    public float getTopOffset(String string, int fontSize) {
        return getAscent(string, fontSize) - getDescent(string, fontSize) + getLeading(string, fontSize);
    }

    @Override
    public void clear() {
        stringCache.clear();
    }
}
