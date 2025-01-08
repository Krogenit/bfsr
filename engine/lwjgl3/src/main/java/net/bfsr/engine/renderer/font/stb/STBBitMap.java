package net.bfsr.engine.renderer.font.stb;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharSet;
import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.font.FontBitMap;
import net.bfsr.engine.renderer.font.FontPackResult;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.util.IOUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBTruetype.STBTT_POINT_SIZE;
import static org.lwjgl.stb.STBTruetype.stbtt_PackBegin;
import static org.lwjgl.stb.STBTruetype.stbtt_PackEnd;
import static org.lwjgl.stb.STBTruetype.stbtt_PackFontRanges;
import static org.lwjgl.stb.STBTruetype.stbtt_PackSetSkipMissingCodepoints;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForMappingEmToPixels;

@Getter
class STBBitMap extends FontBitMap {
    private static final CharSet INVISIBLE_PACKABLE_CHARS = new CharArraySet();

    static {
        INVISIBLE_PACKABLE_CHARS.add((char) 32);
        INVISIBLE_PACKABLE_CHARS.add((char) 160);
    }

    private STBTTPackedchar.Buffer packedChars;
    private final STBTTPackContext packContext;

    STBBitMap(int width, int height) {
        super(width, height);
        packContext = STBTTPackContext.create();
        packedCharMap.defaultReturnValue(-1);
        stbtt_PackSetSkipMissingCodepoints(packContext, true);
    }

    void beginPack() {
        if (!stbtt_PackBegin(packContext, bitmap, width, height, 0, 0)) {
            throw new RuntimeException("Failed to start pack");
        }
    }

    FontPackResult packChars(String fontName, STBTTFontinfo fontInfo, CharList charList, int fontSize, ByteBuffer fontByteBuffer,
                             int index) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            IntBuffer charBuffer = memoryStack.mallocInt(charList.size());
            for (int i = 0; i < charList.size(); i++) {
                charBuffer.put(charList.getChar(i));
            }

            charBuffer.flip();

            STBTTPackedchar.Buffer packedCharsBuffer = STBTTPackedchar.malloc(charList.size(), memoryStack);
            STBTTPackRange.Buffer ranges = STBTTPackRange.malloc(1, memoryStack);
            STBTTPackRange packRange = ranges.get(0);
            packRange.set(STBTT_POINT_SIZE(fontSize), 0, charBuffer, charBuffer.remaining(), packedCharsBuffer, (byte) 0, (byte) 0);

            boolean packed = stbtt_PackFontRanges(packContext, fontByteBuffer, 0, ranges);

            if (bitmapTexture == null) {
                bitmapTexture = Engine.assetsManager.createTexture(width, height);
                Engine.renderer.uploadTexture(bitmapTexture, GL.GL_R8, GL.GL_RED, GL.GL_CLAMP_TO_BORDER, GL.GL_NEAREST, bitmap);
            } else {
                Engine.renderer.subImage2D(bitmapTexture.getId(), 0, 0, width, height, GL.GL_RED, bitmap);
            }

            IOUtils.writePNGGrayScale(bitmap, width, height, "stb_" + fontName + "_atlas_" + fontSize + "_" + index);

            IntBuffer advance = memoryStack.mallocInt(1);
            IntBuffer leftSideBearing = memoryStack.mallocInt(1);
            float scale = stbtt_ScaleForMappingEmToPixels(fontInfo, fontSize);
            CharList packedCharsList = new CharArrayList();
            CharList unpackedCharsList = new CharArrayList();
            for (int i = 0; i < charList.size(); i++) {
                char charCode = charList.getChar(i);
                if (isPacked(packedCharsBuffer.get(i), charCode)) {
                    packedCharsList.add(charCode);
                } else {
                    unpackedCharsList.add(charCode);
                }

                // fixes x advance for invisible code points like space
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, charCode, advance, leftSideBearing);
                packedCharsBuffer.get(i).xadvance(advance.get(0) * scale);
            }

            STBTTPackedchar.Buffer packedChars = STBTTPackedchar.malloc(packedCharsList.size(), memoryStack);
            for (int i = 0; i < charList.size(); i++) {
                char charCode = charList.getChar(i);
                STBTTPackedchar packedChar = packedCharsBuffer.get(i);
                if (isPacked(packedChar, charCode)) {
                    packedChars.put(packedChar);
                    packedCharMap.put(charCode, getNextCharIndex());
                }
            }

            if (this.packedChars != null) {
                STBTTPackedchar.Buffer oldPackedChars = this.packedChars;
                this.packedChars = STBTTPackedchar.create(oldPackedChars.remaining() + packedCharsList.size());
                for (int i = 0; i < oldPackedChars.capacity(); i++) {
                    this.packedChars.put(oldPackedChars.get(i));
                }

                for (int i = 0; i < packedChars.capacity(); i++) {
                    this.packedChars.put(packedChars.get(i));
                }
            } else {
                this.packedChars = STBTTPackedchar.create(packedCharsList.size());
                for (int i = 0; i < packedChars.capacity(); i++) {
                    this.packedChars.put(packedChars.get(i));
                }
            }

            this.packedChars.flip();

            return new FontPackResult(packed, packedCharsList, unpackedCharsList);
        }
    }

    void endPack() {
        stbtt_PackEnd(packContext);
    }

    private boolean isPacked(STBTTPackedchar packedChar, char charCode) {
        return packedChar.x1() - packedChar.x0() > 0.0f || INVISIBLE_PACKABLE_CHARS.contains(charCode);
    }
}
