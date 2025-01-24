package net.bfsr.engine.renderer.font.string;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.bfsr.engine.renderer.font.glyph.Font;
import net.bfsr.engine.renderer.font.glyph.Glyph;
import net.bfsr.engine.renderer.font.glyph.GlyphData;
import net.bfsr.engine.renderer.font.glyph.GlyphKey;
import net.bfsr.engine.renderer.font.glyph.GlyphsData;
import net.bfsr.engine.renderer.primitive.GeometryBuffer;
import net.bfsr.engine.renderer.primitive.Primitive;
import org.joml.Vector4f;

import java.util.List;

public class StringGeometryBuilder {
    private final StringXOffsetSupplier[] offsetFunctions = new StringXOffsetSupplier[3];
    private final StringParams stringParams = new StringParams();
    private final GlyphKey glyphKey = new GlyphKey();
    private final Object2ObjectMap<GlyphKey, Primitive> glyphPrimitiveMap = new Object2ObjectOpenHashMap<>();

    public StringGeometryBuilder() {
        offsetFunctions[StringOffsetType.DEFAULT.ordinal()] = (string, glyphsBuilder, fontSize) -> 0;
        offsetFunctions[StringOffsetType.CENTERED.ordinal()] = (string, glyphsBuilder, fontSize) ->
                -glyphsBuilder.getWidth(string, fontSize) / 2;
        offsetFunctions[StringOffsetType.RIGHT.ordinal()] = (string, glyphsBuilder, fontSize) -> -glyphsBuilder.getWidth(string, fontSize);
    }

    public void createString(StringGeometry stringGeometry, Font font, String string, float x, float y, int fontSize,
                             float r, float g, float b, float a, int maxWidth, StringOffsetType offsetType, boolean shadow,
                             int shadowOffsetX, int shadowOffsetY, GeometryBuffer geometryBuffer) {
        if (maxWidth > 0) {
            trimAndCreateString(stringGeometry, font, string, x, y, fontSize, stringParams, maxWidth, r, g, b, a, offsetType,
                    shadow, shadowOffsetX, shadowOffsetY, geometryBuffer);
        } else {
            createString(stringGeometry, font, string, x, y, fontSize, r, g, b, a, offsetType, shadow, shadowOffsetX,
                    shadowOffsetY, geometryBuffer);
        }
    }

    private void trimAndCreateString(StringGeometry stringGeometry, Font font, String string, float x, float y,
                                     int fontSize, StringParams stringParams, int maxWidth, float r, float g, float b, float a,
                                     StringOffsetType offsetType, boolean shadow, int shadowOffsetX, int shadowOffsetY,
                                     GeometryBuffer geometryBuffer) {
        stringParams.setColor(r, g, b, a);
        stringParams.setY(y);

        do {
            int trimSize = font.getWidth(string, fontSize, maxWidth, true);
            String subString = string.substring(0, trimSize);
            stringParams.setX(x + offsetFunctions[offsetType.ordinal()].get(subString, font, fontSize));

            if (shadow) {
                float prevX = stringParams.getX();
                float prevY = stringParams.getY();
                stringParams.setX(prevX + shadowOffsetX / 2.0f);
                stringParams.setY(prevY + shadowOffsetY / 2.0f);
                stringParams.getColor().set(0.0f, 0.0f, 0.0f, a);
                createString(stringGeometry, font, subString, fontSize, stringParams, geometryBuffer);
                stringParams.setX(prevX);
                stringParams.setY(prevY);
            }

            stringParams.getColor().set(r, g, b, a);
            createString(stringGeometry, font, subString, fontSize, stringParams, geometryBuffer);
            string = string.substring(trimSize);
        } while (!string.isEmpty());
    }

    private void createString(StringGeometry stringGeometry, Font font, String string, float x, float y, int fontSize,
                              float r, float g, float b, float a, StringOffsetType offsetType, boolean shadow,
                              int shadowOffsetX, int shadowOffsetY, GeometryBuffer geometryBuffer) {
        if (shadow) {
            createString(stringGeometry, font, string, x + shadowOffsetX / 2.0f, y + shadowOffsetY / 2.0f, fontSize, 0.0f, 0.0f,
                    0.0f, a, offsetType, geometryBuffer);
        }

        createString(stringGeometry, font, string, x, y, fontSize, r, g, b, a, offsetType, geometryBuffer);
    }

    private void createString(StringGeometry stringGeometry, Font font, String string, float x, float y, int fontSize,
                              float r, float g, float b, float a, StringOffsetType offsetType, GeometryBuffer geometryBuffer) {
        stringParams.setColor(r, g, b, a);
        float startX = x + offsetFunctions[offsetType.ordinal()].get(string, font, fontSize);
        stringParams.setX(startX);
        stringParams.setY(y);

        int offset = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == Font.NEW_LINE) {
                String substring = string.substring(offset, i);
                createString(stringGeometry, font, substring, fontSize, stringParams, geometryBuffer);
                stringParams.setX(startX);
                offset = i + 1;
            }
        }
        createString(stringGeometry, font, string.substring(offset), fontSize, stringParams, geometryBuffer);
    }

    private void createString(StringGeometry stringGeometry, Font font, String string, int fontSize,
                              StringParams stringParams, GeometryBuffer geometryBuffer) {
        GlyphsData glyphsData = font.getGlyphsData(string, fontSize);
        List<Glyph> glyphs = glyphsData.getGlyphs();
        int height = (int) (font.getLineHeight(fontSize));

        for (int glyphIndex = 0; glyphIndex < glyphs.size(); glyphIndex++) {
            Glyph glyph = glyphs.get(glyphIndex);
            if (glyph.isEmpty()) {
                stringParams.setX(stringParams.getX() + glyph.getAdvance());
                continue;
            }

            glyphKey.setFont(font);
            glyphKey.setFontSize(fontSize);
            glyphKey.setCodepoint(glyph.getCodepoint());
            Primitive primitive = glyphPrimitiveMap.get(glyphKey);
            if (primitive == null) {
                Primitive addGlyphPrimitive = new Primitive(glyph.getX1(), -glyph.getY1(), glyph.getU1(), glyph.getV1(),
                        glyph.getX1(), -glyph.getY2(), glyph.getU1(), glyph.getV2(), glyph.getX2(), -glyph.getY2(), glyph.getU2(),
                        glyph.getV2(), glyph.getX2(), -glyph.getY1(), glyph.getU2(), glyph.getV1());
                geometryBuffer.addPrimitive(addGlyphPrimitive);
                glyphPrimitiveMap.put(new GlyphKey(font, fontSize, glyph.getCodepoint()), addGlyphPrimitive);
                primitive = addGlyphPrimitive;
            }

            Vector4f color = stringParams.getColor();
            stringGeometry.addGlyphData(new GlyphData(primitive.getBaseVertex(), stringParams.getX(), stringParams.getY(), color.x, color.y,
                    color.z, color.w, glyph.getTextureHandle()));
            stringParams.setX(stringParams.getX() + glyph.getAdvance());
        }

        stringGeometry.setWidth(glyphsData.getWidth());
        stringParams.setY(stringParams.getY() - height);
    }
}