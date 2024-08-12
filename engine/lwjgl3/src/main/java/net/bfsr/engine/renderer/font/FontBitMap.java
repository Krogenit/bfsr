package net.bfsr.engine.renderer.font;

import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class FontBitMap {
    protected final int width;
    protected final int height;
    protected final ByteBuffer bitmap;
    protected AbstractTexture bitmapTexture;
    protected final Char2IntOpenHashMap packedCharMap = new Char2IntOpenHashMap();
    private int packedCharIndex;

    public FontBitMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.bitmap = BufferUtils.createByteBuffer(width * height);
    }

    public int getCharIndex(char charCode) {
        return packedCharMap.get(charCode);
    }

    public long getTextureHandle() {
        return bitmapTexture.getTextureHandle();
    }

    protected int getNextCharIndex() {
        return packedCharIndex++;
    }
}
