package net.bfsr.engine.renderer.font.stb;

import it.unimi.dsi.fastutil.chars.CharList;
import lombok.Getter;
import net.bfsr.engine.renderer.font.FontPackResult;
import net.bfsr.engine.renderer.font.FontPacker;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.stb.STBTruetype.stbtt_GetPackedQuad;

@Getter
class STBTrueTypeFontPacker extends FontPacker<STBBitMap> {
    private final STBTTFontinfo fontInfo;
    private final ByteBuffer fontByteBuffer;

    STBTrueTypeFontPacker(String fontName, STBTrueTypeFont trueTypeGlyphsBuilder, STBTTFontinfo fontInfo,
                          ByteBuffer fontByteBuffer, int bitmapWidth, int bitmapHeight, int fontSize) {
        super(trueTypeGlyphsBuilder, bitmapWidth, bitmapHeight, fontSize, fontName);
        this.fontInfo = fontInfo;
        this.fontByteBuffer = fontByteBuffer;
    }

    @Override
    protected STBBitMap createBitMap(int bitmapWidth, int bitmapHeight) {
        STBBitMap bitMap = new STBBitMap(bitmapWidth, bitmapHeight);
        bitMaps.add(bitMap);
        bitMap.beginPack();
        return bitMap;
    }

    @Override
    protected void packNewChars(CharList charList) {
        FontPackResult packResult = currentBitMap.packChars(fontName, fontInfo, charList, fontSize, fontByteBuffer, bitMaps.size() - 1);
        addPackedCharsBitMapToMap(packResult.getPackedCharsList(), currentBitMap);
        if (!packResult.isAllCharsPacked() && packResult.getUnpackedCharsList().size() > 0 &&
                charList.size() != packResult.getUnpackedCharsList().size()) {
            currentBitMap.endPack();
            currentBitMap = createBitMap(bitmapWidth, bitmapHeight);
            packNewChars(packResult.getUnpackedCharsList());
        }
    }

    private int getCharIndex(char charCode) {
        return bitmapByCharMap.get(charCode).getCharIndex(charCode);
    }

    void getPackedQuad(STBBitMap bitMap, char charCode, FloatBuffer x, FloatBuffer y, STBTTAlignedQuad stbttAlignedQuad) {
        stbtt_GetPackedQuad(bitMap.getPackedChars(), bitmapWidth, bitmapHeight, getCharIndex(charCode), x, y, stbttAlignedQuad, true);
    }
}
