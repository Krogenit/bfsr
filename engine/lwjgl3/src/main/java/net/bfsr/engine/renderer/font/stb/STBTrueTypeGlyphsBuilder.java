package net.bfsr.engine.renderer.font.stb;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.glyph.Glyph;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.glyph.GlyphsData;
import net.bfsr.engine.util.PathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.stb.STBTruetype.stbtt_FindGlyphIndex;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointKernAdvance;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForMappingEmToPixels;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;

public class STBTrueTypeGlyphsBuilder extends GlyphsBuilder {
    private final STBTTFontinfo fontInfo;
    private final ByteBuffer fontByteBuffer;
    private final Int2ObjectMap<STBTrueTypeFont> fontsBySize = new Int2ObjectOpenHashMap<>();
    private final int ascent;
    private final int descent;
    private final int lineGap;

    private final int bitmapWidth = 256;
    private final int bitmapHeight = 256;

    private final String fontFile;

    public STBTrueTypeGlyphsBuilder(String fontFile) {
        this.fontFile = fontFile;
        try {
            byte[] bytes = Files.readAllBytes(PathHelper.FONT.resolve(fontFile));
            fontByteBuffer = BufferUtils.createByteBuffer(bytes.length);
            fontByteBuffer.put(bytes);
            fontByteBuffer.flip();

            fontInfo = STBTTFontinfo.create();
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
                lineGap = pLineGap.get(0);
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't load font file " + fontFile, e);
        }
    }

    @Override
    public GlyphsData getGlyphsData(String text, int fontSize) {
        STBTrueTypeFont stbTrueTypeFont = getFontBySize(fontSize);
        stbTrueTypeFont.packNewChars(fontInfo, text);
        float scale = scale(fontSize);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            LongBuffer textureBuffer = stack.mallocLong(1);
            List<Glyph> glyphs = new ArrayList<>(32);

            for (int i = 0, to = text.length(); i < to; i++) {
                char charCode = text.charAt(i);
                if (!getPackedQuad(stbTrueTypeFont, charCode, fontSize, x, y, quad, textureBuffer)) {
                    continue;
                }

                x.put(0, x.get(0));

                if (i + 1 < to) {
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(fontInfo, charCode, text.charAt(i + 1)) * scale);
                }

                if (quad.x1() - quad.x0() > 0.0f) {
                    glyphs.add(new Glyph(quad.x0(), quad.y0(), quad.x1(), quad.y1(), quad.s0(), quad.t0(), quad.s1(), quad.t1(),
                            textureBuffer.get(0)));
                }
            }

            return new GlyphsData(glyphs, (int) x.get(0));
        }
    }

    private boolean getPackedQuad(STBTrueTypeFont stbTrueTypeFont, char charCode, int fontSize, FloatBuffer x, FloatBuffer y,
                                  STBTTAlignedQuad quad, LongBuffer textureBuffer) {
        STBBitMap bitMap = stbTrueTypeFont.getBitMapByChar(charCode);
        if (bitMap == null) {
            STBTrueTypeGlyphsBuilder trueTypeGlyphsBuilder = findFontSupportedChar(charCode);
            if (trueTypeGlyphsBuilder == null) return false;
            STBTrueTypeFont stbTrueTypeFont1 = trueTypeGlyphsBuilder.getFontBySize(fontSize);
            stbTrueTypeFont1.packNewChars(fontInfo, "" + charCode);
            bitMap = stbTrueTypeFont1.getBitMapByChar(charCode);

            stbTrueTypeFont1.getPackedQuad(bitMap, charCode, x, y, quad);
        } else {
            stbTrueTypeFont.getPackedQuad(bitMap, charCode, x, y, quad);
        }

        textureBuffer.put(0, bitMap.getTextureHandle());

        return true;
    }

    @Nullable
    private STBTrueTypeGlyphsBuilder findFontSupportedChar(char charCode) {
        Font[] fonts = Font.values();
        for (int i = 0; i < fonts.length; i++) {
            GlyphsBuilder glyphsBuilder = fonts[i].getGlyphsBuilder();
            if (glyphsBuilder instanceof STBTrueTypeGlyphsBuilder trueTypeGlyphsBuilder) {
                if (trueTypeGlyphsBuilder.isCharCodeSupported(charCode)) {
                    return trueTypeGlyphsBuilder;
                }
            }
        }

        return null;
    }

    boolean isCharCodeSupported(char charCode) {
        return stbtt_FindGlyphIndex(fontInfo, charCode) != 0;
    }

    private STBTrueTypeFont getFontBySize(int fontSize) {
        STBTrueTypeFont stbTrueTypeFont = fontsBySize.get(fontSize);
        if (stbTrueTypeFont == null) {
            stbTrueTypeFont = new STBTrueTypeFont(fontFile, this, fontInfo, fontByteBuffer, bitmapWidth, bitmapHeight, fontSize);
            fontsBySize.put(fontSize, stbTrueTypeFont);
        }

        return stbTrueTypeFont;
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

        /* Avoid splitting individual words if breakAtSpaces set; same test condition as in Minecraft's FontRenderer */
        if (index < string.length() && lastIndex >= 0) {
            index = lastIndex;
        }

        /* The string index of the last glyph that wouldn't fit gives the total desired length of the string in characters */
        return index < string.length() ? index : string.length();
    }

    @Override
    public int getWidth(String string, int fontSize) {
        STBTrueTypeFont stbTrueTypeFont = getFontBySize(fontSize);
        float scale = stbtt_ScaleForMappingEmToPixels(fontInfo, fontSize);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            LongBuffer textureBuffer = stack.mallocLong(1);

            for (int i = 0; i < string.length(); i++) {
                char cp = string.charAt(i);
                getPackedQuad(stbTrueTypeFont, cp, fontSize, x, y, quad, textureBuffer);

                if (i + 1 < string.length()) {
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(fontInfo, cp, string.charAt(i + 1)) * scale);
                }
            }

            return (int) x.get(0);
        }
    }

    @Override
    public float getHeight(String string, int fontSize) {
        float scale = scale(fontSize);
        return (int) ((ascent - descent + lineGap) * scale);
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
        return lineGap * scale(fontSize);
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
