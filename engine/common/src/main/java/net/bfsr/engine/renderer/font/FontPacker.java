package net.bfsr.engine.renderer.font;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.bfsr.engine.renderer.font.glyph.DynamicFont;

import java.util.ArrayList;
import java.util.List;

public abstract class FontPacker<BITMAP extends FontBitMap> {
    private final DynamicFont<?> glyphsBuilder;
    protected final int bitmapWidth;
    protected final int bitmapHeight;
    protected final int fontSize;
    protected final List<BITMAP> bitMaps = new ArrayList<>();
    protected BITMAP currentBitMap;
    protected final Int2ObjectOpenHashMap<BITMAP> bitmapByCharMap = new Int2ObjectOpenHashMap<>();
    protected final String fontName;

    public FontPacker(DynamicFont<?> glyphsBuilder, int bitmapWidth, int bitmapHeight, int fontSize, String fontName) {
        this.glyphsBuilder = glyphsBuilder;
        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;
        this.fontSize = fontSize;
        this.fontName = fontName;
    }

    public void init() {
        char startChar = 32;
        char endChar = 256;
        CharList charList = new CharArrayList(64);
        for (int i = startChar; i < endChar; i++) {
            char charCode = (char) i;
            if (isCharCodeSupported(charCode)) {
                charList.add(charCode);
            }
        }

        currentBitMap = createBitMap(bitmapWidth, bitmapHeight);
        packNewChars(charList);
    }

    public void packNewChars(String text) {
        CharList charList = new CharArrayList();
        for (int i = 0, to = text.length(); i < to; i++) {
            char charCode = text.charAt(i);
            BITMAP stbBitMap = bitmapByCharMap.get(charCode);
            if (stbBitMap == null || stbBitMap.getCharIndex(charCode) == -1) {
                if (isCharCodeSupported(charCode) && !charList.contains(charCode)) {
                    charList.add(charCode);
                }
            }
        }

        if (charList.size() > 0) {
            packNewChars(charList);
        }
    }

    protected abstract void packNewChars(CharList charList);

    protected void addPackedCharsBitMapToMap(CharList packedCharsList, BITMAP bitmap) {
        for (int i = 0; i < packedCharsList.size(); i++) {
            bitmapByCharMap.put(packedCharsList.getChar(i), bitmap);
        }
    }

    protected abstract BITMAP createBitMap(int bitmapWidth, int bitmapHeight);

    public BITMAP getBitMapByChar(char charCode) {
        return bitmapByCharMap.get(charCode);
    }

    private boolean isCharCodeSupported(char charCode) {
        return glyphsBuilder.isCharCodeSupported(charCode);
    }

    public void clear() {
        for (int i = 0; i < bitMaps.size(); i++) {
            bitMaps.get(i).clear();
        }

        bitMaps.clear();
        bitmapByCharMap.clear();
    }
}
