package net.bfsr.engine.renderer.font.truetype;

import lombok.extern.log4j.Log4j2;
import net.bfsr.engine.renderer.font.DynamicGlyphsBuilder;
import net.bfsr.engine.renderer.font.glyph.Glyph;
import net.bfsr.engine.renderer.font.glyph.GlyphsData;
import net.bfsr.engine.util.IOUtils;
import net.bfsr.engine.util.PathHelper;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.freetype.FT_Face;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.util.freetype.FreeType.FT_Err_Ok;
import static org.lwjgl.util.freetype.FreeType.FT_Err_Unknown_File_Format;
import static org.lwjgl.util.freetype.FreeType.FT_Get_Char_Index;
import static org.lwjgl.util.freetype.FreeType.FT_Init_FreeType;
import static org.lwjgl.util.freetype.FreeType.FT_New_Memory_Face;

@Log4j2
public class TrueTypeGlyphsBuilder extends DynamicGlyphsBuilder<TrueTypeFontPacker> {
    private static final PointerBuffer libraryPointerBuffer;
    private static final long library;

    static {
        libraryPointerBuffer = PointerBuffer.allocateDirect(1);
        if (FT_Init_FreeType(libraryPointerBuffer) != FT_Err_Ok) {
            throw new RuntimeException("Could not initialize FreeType library");
        }

        library = libraryPointerBuffer.get(0);
    }

    // This should be keep in memory
    private final ByteBuffer byteBuffer;
    // This should be keep in memory
    private final PointerBuffer ftFacePointerBuffer;
    private final FT_Face ftFace;

    public TrueTypeGlyphsBuilder(String fontFile) {
        super(fontFile);

        byteBuffer = IOUtils.fileToByteBuffer(PathHelper.FONT.resolve(fontFile));
        ftFacePointerBuffer = PointerBuffer.allocateDirect(1);

        int error = FT_New_Memory_Face(library, byteBuffer, 0, ftFacePointerBuffer);
        if (error == FT_Err_Unknown_File_Format) {
            throw new RuntimeException("Unknown file format for file " + fontFile);
        } else if (error != FT_Err_Ok) {
            throw new RuntimeException("Could not load file " + fontFile);
        }

        ftFace = FT_Face.createSafe(ftFacePointerBuffer.get(0));
    }

    @Override
    public GlyphsData getGlyphsData(String text, int fontSize) {
        TrueTypeFontPacker trueTypeFontPacker = getFontBySize(fontSize);
        trueTypeFontPacker.packNewChars(text);

        List<Glyph> glyphs = new ArrayList<>(32);
        int width = 0;

        for (int i = 0, to = text.length(); i < to; i++) {
            char charCode = text.charAt(i);
            Glyph glyph = getGlyph(charCode, fontSize);
            if (glyph == null) {
                continue;
            }

            glyphs.add(new Glyph(glyph.getX1(), glyph.getY1(), glyph.getX2(), glyph.getY2(), glyph.getU1(), glyph.getV1(),
                    glyph.getU2(), glyph.getV2(), glyph.getTextureHandle(), glyph.getAdvance(), glyph.getCodepoint(), glyph.isEmpty()));

            width += glyph.getAdvance();
        }

        return new GlyphsData(glyphs, width);
    }

    @Override
    protected TrueTypeFontPacker createNewFont(int fontSize) {
        return new TrueTypeFontPacker(this, ftFace, bitmapWidth, bitmapHeight, fontSize, fontFile);
    }

    @Override
    public boolean isCharCodeSupported(char charCode) {
        return FT_Get_Char_Index(ftFace, charCode) != 0;
    }

    @Nullable
    private Glyph getGlyph(char charCode, int fontSize) {
        TrueTypeBitMap bitMap = getFontBySize(fontSize).getBitMapByChar(charCode);
        if (bitMap == null) {
            TrueTypeGlyphsBuilder trueTypeGlyphsBuilder = findFontSupportedChar(charCode,
                    glyphsBuilder -> glyphsBuilder instanceof TrueTypeGlyphsBuilder);
            if (trueTypeGlyphsBuilder == null) {
                return null;
            }

            TrueTypeFontPacker stbTrueTypeFontPacker1 = trueTypeGlyphsBuilder.getFontBySize(fontSize);
            stbTrueTypeFontPacker1.packNewChars("" + charCode);
            bitMap = stbTrueTypeFontPacker1.getBitMapByChar(charCode);
        }

        return bitMap.getGlyph(charCode);
    }

    @Override
    public int getWidth(String string, int fontSize, int maxWidth, boolean breakAtSpaces) {
        if (string == null || string.isEmpty()) {
            return 0;
        }

        int lastIndex = -1;
        int index = 0, advance = 0;
        while (index < string.length() && advance <= maxWidth) {
            char cp = string.charAt(index);

            if (breakAtSpaces) {
                if (cp == SPACE) {
                    lastIndex = index + 1;
                } else if (cp == NEW_LINE) {
                    lastIndex = index + 1;
                    break;
                }
            }

            Glyph glyph = getGlyph(cp, fontSize);
            if (glyph == null) {
                continue;
            }

            int nextAdvance = advance + glyph.getAdvance();
            if (nextAdvance <= maxWidth) {
                advance = nextAdvance;
                index++;
            } else {
                break;
            }
        }

        if (index < string.length() && lastIndex >= 0) {
            index = lastIndex;
        }

        return index < string.length() ? index : string.length();
    }

    @Override
    public int getWidth(String string, int fontSize) {
        int width = 0;
        for (int i = 0; i < string.length(); i++) {
            Glyph glyph = getGlyph(string.charAt(i), fontSize);
            if (glyph == null) {
                continue;
            }

            width += glyph.getAdvance();
        }

        return width;
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
        return getAscent("\n", fontSize) - getDescent("\n", fontSize);
    }

    @Override
    public float getAscent(String string, int fontSize) {
        return getFontBySize(fontSize).getAscender();
    }

    @Override
    public float getDescent(String string, int fontSize) {
        return getFontBySize(fontSize).getDescender();
    }

    @Override
    public float getLeading(String string, int fontSize) {
        return 0;
    }

    @Override
    public int getCenteredOffsetY(String string, int height, int fontSize) {
        return (int) ((height - getAscent(string, fontSize) + getDescent(string, fontSize)) / 2.0f);
    }

    @Override
    public int getCursorPositionInLine(String string, float mouseX, int fontSize) {
        if (string.isEmpty()) {
            return 0;
        }

        float advance = 0.0f;
        int index = 0;
        while (index < string.length() && advance <= mouseX) {
            Glyph glyph = getGlyph(string.charAt(index), fontSize);
            if (glyph == null) {
                continue;
            }

            float charHalfAdvance = (glyph.getAdvance() << 6) / 64.0f * 0.5f;
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

    @Override
    public float getTopOffset(String string, int fontSize) {
        return getAscent(string, fontSize);
    }
}
