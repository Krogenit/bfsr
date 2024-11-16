package net.bfsr.engine.renderer.font.truetype;

import it.unimi.dsi.fastutil.chars.CharList;
import lombok.Getter;
import net.bfsr.engine.renderer.font.DynamicGlyphsBuilder;
import net.bfsr.engine.renderer.font.FontPackResult;
import net.bfsr.engine.renderer.font.FontPacker;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

import static org.lwjgl.util.freetype.FreeType.FT_Err_Ok;
import static org.lwjgl.util.freetype.FreeType.FT_Load_Char;
import static org.lwjgl.util.freetype.FreeType.FT_Render_Glyph;

class TrueTypeFontPacker extends FontPacker<TrueTypeBitMap> {
    private final FT_Face ftFace;
    @Getter
    private int ascender;
    @Getter
    private int descender;

    TrueTypeFontPacker(DynamicGlyphsBuilder<?> glyphsBuilder, FT_Face ftFace, int bitmapWidth, int bitmapHeight, int fontSize,
                       String fontName) {
        super(glyphsBuilder, bitmapWidth, bitmapHeight, fontSize, fontName);
        this.ftFace = ftFace;
    }

    @Override
    public void init() {
        super.init();

        // Calculates ascender
        for (char charCode = 32; charCode < 127; charCode++) {
            if (FT_Load_Char(ftFace, charCode, FreeType.FT_LOAD_RENDER) != FT_Err_Ok) {
                throw new RuntimeException("Could not load character " + charCode);
            }

            FT_GlyphSlot glyph = ftFace.glyph();
            if (FT_Render_Glyph(glyph, FreeType.FT_RENDER_MODE_NORMAL) != FT_Err_Ok) {
                throw new RuntimeException("Could not render glyph " + charCode);
            }

            if (glyph.bitmap_top() > ascender) {
                ascender = glyph.bitmap_top();
            }
        }

        descender = (int) (ftFace.size().metrics().descender() >> 6);
    }

    @Override
    protected TrueTypeBitMap createBitMap(int bitmapWidth, int bitmapHeight) {
        TrueTypeBitMap bitMap = new TrueTypeBitMap(bitmapWidth, bitmapHeight);
        bitMaps.add(bitMap);
        return bitMap;
    }

    @Override
    protected void packNewChars(CharList charList) {
        FontPackResult packResult = currentBitMap.packChars(fontName, charList, ftFace, fontSize, bitMaps.size() - 1);
        addPackedCharsBitMapToMap(packResult.getPackedCharsList(), currentBitMap);
        if (!packResult.isAllCharsPacked() && packResult.getUnpackedCharsList().size() > 0 &&
                charList.size() != packResult.getUnpackedCharsList().size()) {
            currentBitMap = createBitMap(bitmapWidth, bitmapHeight);
            packNewChars(packResult.getUnpackedCharsList());
        }
    }
}
