package net.bfsr.engine.renderer.font.stb;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.stb.STBTruetype.stbtt_GetPackedQuad;

@Getter
@AllArgsConstructor
class STBTrueTypeFont {
    private final STBTrueTypeGlyphsBuilder trueTypeGlyphsBuilder;
    private final int bitmapWidth;
    private final int bitmapHeight;
    private final ByteBuffer fontByteBuffer;
    private final int fontSize;
    private final List<STBBitMap> stbBitMaps = new ArrayList<>();
    private final Int2ObjectOpenHashMap<STBBitMap> bitmapByCharMap = new Int2ObjectOpenHashMap<>();
    private final String fontName;

    STBTrueTypeFont(String fontName, STBTrueTypeGlyphsBuilder trueTypeGlyphsBuilder, STBTTFontinfo fontInfo, ByteBuffer fontByteBuffer,
                    int bitmapWidth, int bitmapHeight, int fontSize) {
        this.fontName = fontName;
        this.trueTypeGlyphsBuilder = trueTypeGlyphsBuilder;
        this.fontByteBuffer = fontByteBuffer;
        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;
        this.fontSize = fontSize;

        STBBitMap stbBitMap = new STBBitMap(bitmapWidth, bitmapHeight);
        stbBitMaps.add(stbBitMap);
        stbBitMap.beginPack();

        char startChar = 32;
        char endChar = 256;
        CharList charList = new CharArrayList(64);
        for (int i = startChar; i < endChar; i++) {
            char charCode = (char) i;
            if (isCharCodeSupported(charCode)) {
                charList.add(charCode);
            }
        }

        STBPackResult stbPackResult = stbBitMap.packChars(fontName, fontInfo, charList, fontSize, fontByteBuffer, 0);
        addPackedCharsBitMapToMap(stbPackResult.getPackedCharsList(), stbBitMap);
        if (!stbPackResult.isAllCharsPacked() && stbPackResult.getUnpackedCharsList().size() > 0) {
            stbBitMap.endPack();
            packToNewBitMap(fontInfo, stbPackResult.getUnpackedCharsList());
        }
    }

    private void packToNewBitMap(STBTTFontinfo fontInfo, CharList charList) {
        STBBitMap stbBitMap = new STBBitMap(bitmapWidth, bitmapHeight);
        stbBitMaps.add(stbBitMap);
        stbBitMap.beginPack();

        STBPackResult packResult = stbBitMap.packChars(fontName, fontInfo, charList, fontSize, fontByteBuffer, stbBitMaps.size() - 1);
        addPackedCharsBitMapToMap(packResult.getPackedCharsList(), stbBitMap);
        if (!packResult.isAllCharsPacked() && charList.size() != packResult.getUnpackedCharsList().size()) {
            stbBitMap.endPack();
            packToNewBitMap(fontInfo, packResult.getUnpackedCharsList());
        }
    }

    private void addPackedCharsBitMapToMap(CharList packedCharsList, STBBitMap stbBitMap) {
        for (int i = 0; i < packedCharsList.size(); i++) {
            bitmapByCharMap.put(packedCharsList.getChar(i), stbBitMap);
        }
    }

    void packNewChars(STBTTFontinfo fontInfo, String text) {
        CharList charList = new CharArrayList();
        for (int i = 0, to = text.length(); i < to; i++) {
            char charCode = text.charAt(i);
            STBBitMap stbBitMap = bitmapByCharMap.get(charCode);
            if (stbBitMap == null || stbBitMap.getCharIndex(charCode) == -1) {
                if (isCharCodeSupported(charCode) && !charList.contains(charCode)) {
                    charList.add(charCode);
                }
            }
        }

        if (charList.size() > 0) {
            STBBitMap stbBitMap = stbBitMaps.get(stbBitMaps.size() - 1);
            STBPackResult packResult = stbBitMap.packChars(fontName, fontInfo, charList, fontSize, fontByteBuffer, stbBitMaps.size() - 1);
            addPackedCharsBitMapToMap(packResult.getPackedCharsList(), stbBitMap);
            if (!packResult.isAllCharsPacked()) {
                stbBitMap.endPack();
                packToNewBitMap(fontInfo, packResult.getUnpackedCharsList());
            }
        }
    }

    private boolean isCharCodeSupported(char charCode) {
        return trueTypeGlyphsBuilder.isCharCodeSupported(charCode);
    }

    private int getCharIndex(char charCode) {
        return bitmapByCharMap.get(charCode).getCharIndex(charCode);
    }

    void getPackedQuad(STBBitMap bitMap, char charCode, FloatBuffer x, FloatBuffer y, STBTTAlignedQuad stbttAlignedQuad) {
        stbtt_GetPackedQuad(bitMap.getPackedChars(), bitmapWidth, bitmapHeight, getCharIndex(charCode), x, y, stbttAlignedQuad, true);
    }

    STBBitMap getBitMapByChar(char charCode) {
        return bitmapByCharMap.get(charCode);
    }
}
