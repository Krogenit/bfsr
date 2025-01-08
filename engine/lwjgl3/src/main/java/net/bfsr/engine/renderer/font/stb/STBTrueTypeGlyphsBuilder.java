package net.bfsr.engine.renderer.font.stb;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.renderer.font.DynamicGlyphsBuilder;
import net.bfsr.engine.renderer.font.glyph.Glyph;
import net.bfsr.engine.renderer.font.glyph.GlyphsData;
import net.bfsr.engine.util.IOUtils;
import net.bfsr.engine.util.PathHelper;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.stb.STBTruetype.stbtt_FindGlyphIndex;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForMappingEmToPixels;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;

@Log4j2
public class STBTrueTypeGlyphsBuilder extends DynamicGlyphsBuilder<STBTrueTypeFontPacker> {
    private final STBTTFontinfo fontInfo;
    private final ByteBuffer fontByteBuffer;
    private final int ascent;
    private final int descent;

    public STBTrueTypeGlyphsBuilder(String fontFile) {
        super(fontFile);
        this.fontByteBuffer = IOUtils.fileToByteBuffer(PathHelper.FONT.resolve(fontFile));
        this.fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontByteBuffer)) {
            throw new IllegalStateException("Failed to initialize font information.");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pAscent = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(fontInfo, pAscent, pDescent, pLineGap);
            ascent = pAscent.get(0);
            descent = pDescent.get(0);
        }
    }

    @Override
    protected STBTrueTypeFontPacker createNewFont(int fontSize) {
        return new STBTrueTypeFontPacker(fontFile, this, fontInfo, fontByteBuffer, bitmapWidth, bitmapHeight, fontSize);
    }

    @Override
    public GlyphsData getGlyphsData(String text, int fontSize) {
        STBTrueTypeFontPacker stbTrueTypeFontPacker = getFontBySize(fontSize);
        stbTrueTypeFontPacker.packNewChars(text);
        scale(fontSize);
        int width = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            LongBuffer textureBuffer = stack.mallocLong(1);
            List<Glyph> glyphs = new ArrayList<>(32);

            for (int i = 0, to = text.length(); i < to; i++) {
                char charCode = text.charAt(i);
                if (!getPackedQuad(stbTrueTypeFontPacker, charCode, fontSize, x, y, quad, textureBuffer)) {
                    continue;
                }

                int advance = (int) x.get(0);
                if (quad.x1() - quad.x0() > 0.0f) {
                    glyphs.add(new Glyph(quad.x0(), quad.y0(), quad.x1(), quad.y1(), quad.s0(), quad.t0(), quad.s1(), quad.t1(),
                            textureBuffer.get(0), advance, charCode, false));
                } else {
                    glyphs.add(new Glyph(quad.x0(), quad.y0(), quad.x1(), quad.y1(), quad.s0(), quad.t0(), quad.s1(), quad.t1(),
                            textureBuffer.get(0), advance, charCode, true));
                }

                x.put(0, 0);

                width += advance;
            }

            return new GlyphsData(glyphs, width);
        }
    }

    private boolean getPackedQuad(STBTrueTypeFontPacker stbTrueTypeFontPacker, char charCode, int fontSize, FloatBuffer x, FloatBuffer y,
                                  STBTTAlignedQuad quad, LongBuffer textureBuffer) {
        STBBitMap bitMap = stbTrueTypeFontPacker.getBitMapByChar(charCode);
        if (bitMap == null) {
            STBTrueTypeGlyphsBuilder trueTypeGlyphsBuilder = findFontSupportedChar(charCode,
                    glyphsBuilder -> glyphsBuilder instanceof STBTrueTypeGlyphsBuilder);
            if (trueTypeGlyphsBuilder == null) return false;
            STBTrueTypeFontPacker stbTrueTypeFontPacker1 = trueTypeGlyphsBuilder.getFontBySize(fontSize);
            stbTrueTypeFontPacker1.packNewChars("" + charCode);
            bitMap = stbTrueTypeFontPacker1.getBitMapByChar(charCode);

            stbTrueTypeFontPacker1.getPackedQuad(bitMap, charCode, x, y, quad);
        } else {
            stbTrueTypeFontPacker.getPackedQuad(bitMap, charCode, x, y, quad);
        }

        textureBuffer.put(0, bitMap.getTextureHandle());

        return true;
    }

    @Override
    public boolean isCharCodeSupported(char charCode) {
        return stbtt_FindGlyphIndex(fontInfo, charCode) != 0;
    }

    @Override
    public int getWidth(String string, int fontSize, int maxWidth, boolean breakAtSpaces) {
        if (string == null || string.isEmpty()) {
            return 0;
        }

        float scale = stbtt_ScaleForMappingEmToPixels(fontInfo, fontSize);

        /* Index of the last whitespace found in the string; used if breakAtSpaces is true */
        int lastIndex = -1;

        int advance = 0, index = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer advancedWidth = stack.mallocInt(1);
            IntBuffer leftSideBearing = stack.mallocInt(1);

            while (index < string.length() && advance <= maxWidth) {
                int cp = string.charAt(index);

                if (breakAtSpaces) {
                    if (cp == SPACE) {
                        lastIndex = index + 1;
                    } else if (cp == NEW_LINE) {
                        lastIndex = index + 1;
                        break;
                    }
                }

                stbtt_GetCodepointHMetrics(fontInfo, cp, advancedWidth, leftSideBearing);
                int nextAdvance = advance + (int) (advancedWidth.get(0) * scale);
                if (nextAdvance <= maxWidth) {
                    advance = nextAdvance;
                    index++;
                } else {
                    break;
                }
            }
        }

        if (index < string.length() && lastIndex >= 0) {
            index = lastIndex;
        }

        return index < string.length() ? index : string.length();
    }

    @Override
    public int getWidth(String string, int fontSize) {
        STBTrueTypeFontPacker stbTrueTypeFontPacker = getFontBySize(fontSize);
        scale(fontSize);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            LongBuffer textureBuffer = stack.mallocLong(1);

            for (int i = 0; i < string.length(); i++) {
                char cp = string.charAt(i);
                getPackedQuad(stbTrueTypeFontPacker, cp, fontSize, x, y, quad, textureBuffer);
            }

            return (int) x.get(0);
        }
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
        float scale = scale(fontSize);
        return (int) ((ascent - descent) * scale);
    }

    @Override
    public float getAscent(String string, int fontSize) {
        return ascent * scale(fontSize);
    }

    @Override
    public float getDescent(String string, int fontSize) {
        return descent * scale(fontSize);
    }

    @Override
    public float getLeading(String string, int fontSize) {
        return 0;
    }

    @Override
    public int getCenteredOffsetY(String string, int height, int fontSize) {
        return Math.round((height - getHeight(string, fontSize)) / 2.0f);
    }

    @Override
    public int getCursorPositionInLine(String string, float mouseX, int fontSize) {
        if (string.isEmpty()) {
            return 0;
        }

        float scale = stbtt_ScaleForMappingEmToPixels(fontInfo, fontSize);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer advancedWidth = stack.mallocInt(1);
            IntBuffer leftSideBearing = stack.mallocInt(1);
            float advance = 0.0f;
            int index = 0;
            while (index < string.length() && advance <= mouseX) {
                char charCode = string.charAt(index);
                stbtt_GetCodepointHMetrics(fontInfo, charCode, advancedWidth, leftSideBearing);
                int charHalfAdvance = Math.round(advancedWidth.get(0) * scale * 0.5f);
                float nextAdvance = advance + charHalfAdvance;
                if (nextAdvance <= mouseX) {
                    advance = nextAdvance + charHalfAdvance;
                    index++;
                } else {
                    break;
                }
            }

            return index < string.length() ? index : string.length();
        }
    }

    @Override
    public float getTopOffset(String string, int fontSize) {
        return getAscent(string, fontSize);
    }

    private float scale(int fontSize) {
        return stbtt_ScaleForPixelHeight(fontInfo, fontSize);
    }
}
