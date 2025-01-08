package net.bfsr.engine.renderer.font;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.bfsr.engine.renderer.font.glyph.Glyph;
import net.bfsr.engine.renderer.font.glyph.GlyphData;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.glyph.GlyphsData;
import net.bfsr.engine.renderer.font.string.StringGeometry;
import net.bfsr.engine.renderer.primitive.GeometryBuffer;
import net.bfsr.engine.renderer.primitive.Primitive;
import org.joml.Vector4f;

import java.util.List;

public class StringGeometryBuilder {
    private static final char NEW_LINE_CHAR = '\n';
    private static final int DEFAULT_INDENT = 0;

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

    public void createString(StringGeometry stringGeometry, GlyphsBuilder glyphsBuilder, String string, float x, float y, int fontSize,
                             float r, float g, float b, float a, int maxWidth, int indent, GeometryBuffer geometryBuffer) {
        createString(stringGeometry, glyphsBuilder, string, x, y, fontSize, r, g, b, a, maxWidth, StringOffsetType.DEFAULT, indent,
                geometryBuffer);
    }

    private void createString(StringGeometry stringGeometry, GlyphsBuilder glyphsBuilder, String string, float x, float y, int fontSize,
                              float r, float g, float b, float a, int maxWidth, StringOffsetType offsetType, int indent,
                              GeometryBuffer geometryBuffer) {
        createString(stringGeometry, glyphsBuilder, string, x, y, fontSize, r, g, b, a, maxWidth, offsetType, indent, false, 0, 0,
                geometryBuffer);
    }

    public void createString(StringGeometry stringGeometry, GlyphsBuilder glyphsBuilder, String string, float x, float y, int fontSize,
                             float r, float g, float b, float a, int maxWidth, StringOffsetType offsetType, int indent, boolean shadow,
                             int shadowOffsetX, int shadowOffsetY, GeometryBuffer geometryBuffer) {
        stringParams.setColor(r, g, b, a);
        stringParams.setY(y);
        trimAndCreateString(stringGeometry, glyphsBuilder, string, x, fontSize, stringParams, maxWidth, r, g, b, a, offsetType, indent,
                shadow, shadowOffsetX, shadowOffsetY, geometryBuffer);
    }

    private void trimAndCreateString(StringGeometry stringGeometry, GlyphsBuilder glyphsBuilder, String string, float startX,
                                     int fontSize, StringParams stringParams, int maxWidth, float r, float g, float b, float a,
                                     StringOffsetType offsetType, int indent, boolean shadow, int shadowOffsetX, int shadowOffsetY,
                                     GeometryBuffer geometryBuffer) {
        do {
            int trimSize = glyphsBuilder.getWidth(string, fontSize, maxWidth, true);
            String subString = string.substring(0, trimSize);
            stringParams.setX(startX + offsetFunctions[offsetType.ordinal()].get(subString, glyphsBuilder, fontSize));

            if (shadow) {
                float prevX = stringParams.getX();
                float prevY = stringParams.getY();
                stringParams.setX(prevX + shadowOffsetX / 2.0f);
                stringParams.setY(prevY + shadowOffsetY / 2.0f);
                stringParams.getColor().set(0.0f, 0.0f, 0.0f, a);
                createString(stringGeometry, glyphsBuilder, subString, fontSize, stringParams, indent, geometryBuffer);
                stringParams.setX(prevX);
                stringParams.setY(prevY);
            }

            stringParams.getColor().set(r, g, b, a);
            createString(stringGeometry, glyphsBuilder, subString, fontSize, stringParams, indent, geometryBuffer);
            string = string.substring(trimSize);
        } while (!string.isEmpty());
    }

    public void createString(StringGeometry stringGeometry, GlyphsBuilder glyphsBuilder, String string, float x, float y, int fontSize,
                             float r, float g, float b, float a, StringOffsetType offsetType, boolean shadow, int shadowOffsetX,
                             int shadowOffsetY, GeometryBuffer geometryBuffer) {
        createString(stringGeometry, glyphsBuilder, string, x, y, fontSize, r, g, b, a, offsetType, DEFAULT_INDENT, shadow,
                shadowOffsetX, shadowOffsetY, geometryBuffer);
    }

    private void createString(StringGeometry stringGeometry, GlyphsBuilder glyphsBuilder, String string, float x, float y, int fontSize,
                              float r, float g, float b, float a, StringOffsetType offsetType, int indent, boolean shadow,
                              int shadowOffsetX, int shadowOffsetY, GeometryBuffer geometryBuffer) {
        if (shadow) {
            createString(stringGeometry, glyphsBuilder, string, x + shadowOffsetX / 2.0f, y + shadowOffsetY / 2.0f, fontSize, 0.0f, 0.0f,
                    0.0f, a, offsetType, indent, geometryBuffer);
        }

        createString(stringGeometry, glyphsBuilder, string, x, y, fontSize, r, g, b, a, offsetType, indent, geometryBuffer);
    }

    private void createString(StringGeometry stringGeometry, GlyphsBuilder glyphsBuilder, String string, float x, float y, int fontSize,
                              float r, float g, float b, float a, StringOffsetType offsetType, int indent, GeometryBuffer geometryBuffer) {
        stringParams.getColor().set(r, g, b, a);
        float startX = x + offsetFunctions[offsetType.ordinal()].get(string, glyphsBuilder, fontSize);
        stringParams.setX(startX);
        stringParams.setY(y);

        int offset = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == NEW_LINE_CHAR) {
                String substring = string.substring(offset, i);
                createString(stringGeometry, glyphsBuilder, substring, fontSize, stringParams, indent, geometryBuffer);
                stringParams.setX(startX);
                offset = i + 1;
            }
        }
        createString(stringGeometry, glyphsBuilder, string.substring(offset), fontSize, stringParams, indent, geometryBuffer);
    }

    private void createString(StringGeometry stringGeometry, GlyphsBuilder glyphsBuilder, String string, int fontSize,
                              StringParams stringParams, int indent, GeometryBuffer geometryBuffer) {
        GlyphsData glyphsData = glyphsBuilder.getGlyphsData(string, fontSize);
        List<Glyph> glyphs = glyphsData.getGlyphs();
        int height = (int) (glyphsBuilder.getLineHeight(fontSize)) + indent;

        for (int glyphIndex = 0; glyphIndex < glyphs.size(); glyphIndex++) {
            Glyph glyph = glyphs.get(glyphIndex);
            if (glyph.isEmpty()) {
                stringParams.setX(stringParams.getX() + glyph.getAdvance());
                continue;
            }

            glyphKey.setGlyphsBuilder(glyphsBuilder);
            glyphKey.setFontSize(fontSize);
            glyphKey.setCodepoint(glyph.getCodepoint());
            Primitive primitive = glyphPrimitiveMap.get(glyphKey);
            if (primitive == null) {
                Primitive addGlyphPrimitive = new Primitive(glyph.getX1(), -glyph.getY1(), glyph.getU1(), glyph.getV1(),
                        glyph.getX1(), -glyph.getY2(), glyph.getU1(), glyph.getV2(), glyph.getX2(), -glyph.getY2(), glyph.getU2(),
                        glyph.getV2(), glyph.getX2(), -glyph.getY1(), glyph.getU2(), glyph.getV1());
                geometryBuffer.addPrimitive(addGlyphPrimitive);
                glyphPrimitiveMap.put(new GlyphKey(glyphsBuilder, fontSize, glyph.getCodepoint()), addGlyphPrimitive);
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