package net.bfsr.engine.renderer.font.truetype;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import net.bfsr.engine.Engine;
import net.bfsr.engine.pack.RectanglesPackingAlgorithm;
import net.bfsr.engine.pack.maxrects.MaxRectanglesBinPack;
import net.bfsr.engine.pack.maxrects.Rectangle;
import net.bfsr.engine.renderer.font.FontBitMap;
import net.bfsr.engine.renderer.font.FontPackResult;
import net.bfsr.engine.renderer.font.glyph.Glyph;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.util.IOUtils;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

import static org.lwjgl.util.freetype.FreeType.FT_Err_Ok;
import static org.lwjgl.util.freetype.FreeType.FT_Load_Char;
import static org.lwjgl.util.freetype.FreeType.FT_Render_Glyph;
import static org.lwjgl.util.freetype.FreeType.FT_Set_Pixel_Sizes;

public class TrueTypeBitMap extends FontBitMap {
    private final RectanglesPackingAlgorithm rectanglesPackingAlgorithm;
    private final Char2ObjectMap<Glyph> glyphsByCharCodeMap = new Char2ObjectOpenHashMap<>();

    TrueTypeBitMap(int width, int height) {
        super(width, height);
        this.rectanglesPackingAlgorithm = new MaxRectanglesBinPack(width, height, false);
        this.bitmapTexture = Engine.assetsManager.createTexture(width, height);
        Engine.renderer.uploadTexture(bitmapTexture, GL.GL_R8, GL.GL_RED, GL.GL_CLAMP_TO_BORDER, GL.GL_NEAREST, bitmap);
    }

    public FontPackResult packChars(String fontName, CharList charList, FT_Face ftFace, int fontSize,
                                    int index) {
        if (FT_Set_Pixel_Sizes(ftFace, 0, fontSize) != FT_Err_Ok) {
            throw new RuntimeException("Can't change font pixel size for font " + fontName);
        }

        CharList packedCharsList = new CharArrayList();
        CharList unpackedCharsList = new CharArrayList();
        boolean allCharsPacked = true;

        for (int i = 0; i < charList.size(); i++) {
            char charCode = charList.getChar(i);

            if (FT_Load_Char(ftFace, charCode, FreeType.FT_LOAD_RENDER) != FT_Err_Ok) {
                throw new RuntimeException("Could not load character " + charCode);
            }

            FT_GlyphSlot glyph = ftFace.glyph();
            if (FT_Render_Glyph(glyph, FreeType.FT_RENDER_MODE_NORMAL) != FT_Err_Ok) {
                throw new RuntimeException("Could not render glyph " + charCode);
            }

            FT_Bitmap glyphBitMap = glyph.bitmap();

            if (glyphBitMap.width() == 0) {
                glyphsByCharCodeMap.put(charCode, new Glyph(0, 0, 0, 0, 0, 0, 0, 0, 0, (int) (glyph.advance().x() >> 6), charCode, true));
                packedCharsList.add(charCode);
                continue;
            }

            Rectangle placement = rectanglesPackingAlgorithm.insert(glyphBitMap.width(), glyphBitMap.rows());

            if (placement == null) {
                allCharsPacked = false;

                for (int j = i; j < charList.size(); j++) {
                    unpackedCharsList.add(charList.getChar(j));
                }

                break;
            } else {
                float x1 = glyph.bitmap_left();
                int y1 = -glyph.bitmap_top();
                float x2 = x1 + glyphBitMap.width();
                int y2 = y1 + glyphBitMap.rows();
                float u1 = (float) placement.getX() / width;
                float v1 = (float) placement.getY() / height;
                float u2 = (float) (placement.getX() + placement.getWidth()) / width;
                float v2 = (float) (placement.getY() + placement.getHeight()) / height;

                glyphsByCharCodeMap.put(charCode, new Glyph(x1, y1, x2, y2, u1, v1, u2, v2, bitmapTexture.getTextureHandle(),
                        (int) (glyph.advance().x() >> 6), charCode, false));
                packedCharsList.add(charCode);

                // TODO: optimize calls to one
                ByteBuffer buffer = glyphBitMap.buffer(glyphBitMap.width() * glyphBitMap.rows());
                Engine.renderer.subImage2D(bitmapTexture.getId(), placement.getX(), placement.getY(), placement.getWidth(),
                        placement.getHeight(), GL.GL_RED, buffer);

                // Debug
                for (int y = 0; y < glyphBitMap.rows(); y++) {
                    for (int x = 0; x < glyphBitMap.width(); x++) {
                        int pixelIndex = (placement.getX() + x) + (placement.getY() + y) * height;
                        bitmap.put(pixelIndex, buffer.get(x + y * glyphBitMap.pitch()));
                    }
                }
            }
        }

        // Debug
        IOUtils.writePNGGrayScale(bitmap, width, height, "truetype_" + fontName + "_atlas_" + fontSize + "_" + index);

        return new FontPackResult(allCharsPacked, packedCharsList, unpackedCharsList);
    }

    Glyph getGlyph(char charCode) {
        return glyphsByCharCodeMap.get(charCode);
    }
}
