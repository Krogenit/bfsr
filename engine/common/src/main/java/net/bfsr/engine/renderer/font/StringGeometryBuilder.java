package net.bfsr.engine.renderer.font;

import net.bfsr.engine.renderer.font.glyph.Glyph;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.font.glyph.GlyphsData;
import net.bfsr.engine.renderer.font.string.AbstractGLString;
import net.bfsr.engine.renderer.font.string.AbstractStringGeometryBuilder;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public class StringGeometryBuilder extends AbstractStringGeometryBuilder {
    private static final String NEW_LINE_STRING = "\n";
    private static final char NEW_LINE_CHAR = '\n';
    private static final int DEFAULT_INDENT = 0;

    private final StringXOffsetSupplier[] offsetFunctions = new StringXOffsetSupplier[3];
    private final StringParams stringParams = new StringParams();

    public StringGeometryBuilder() {
        offsetFunctions[StringOffsetType.DEFAULT.ordinal()] = (string, glyphsBuilder, fontSize) -> 0;
        offsetFunctions[StringOffsetType.CENTERED.ordinal()] = (string, glyphsBuilder, fontSize) ->
                -glyphsBuilder.getWidth(string, fontSize) / 2;
        offsetFunctions[StringOffsetType.RIGHT.ordinal()] = (string, glyphsBuilder, fontSize) -> -glyphsBuilder.getWidth(string, fontSize);
    }

    private void begin(AbstractGLString glString) {
        glString.clearBuffers();
    }

    private void end(AbstractGLString glString) {
        glString.flipBuffers();
    }

    @Override
    public void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int x, int y, int fontSize, float r,
                             float g, float b, float a, int maxWidth, int indent) {
        createString(glString, glyphsBuilder, string, x, y, fontSize, r, g, b, a, maxWidth, StringOffsetType.DEFAULT, indent);
    }

    private void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int x, int y, int fontSize,
                              float r, float g, float b, float a, int maxWidth, StringOffsetType offsetType, int indent) {
        createString(glString, glyphsBuilder, string, x, y, fontSize, r, g, b, a, maxWidth, offsetType, indent, false, 0, 0);
    }

    @Override
    public void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int x, int y, int fontSize,
                             float r, float g, float b, float a, int maxWidth, StringOffsetType offsetType, int indent,
                             boolean shadow, int shadowOffsetX, int shadowOffsetY) {
        stringParams.getColor().set(r, g, b, a);
        stringParams.setY(y);
        stringParams.setHeight(0);
        begin(glString);
        trimAndCreateString(glString, glyphsBuilder, string, x, fontSize, stringParams, maxWidth, r, g, b, a, offsetType, indent,
                shadow, shadowOffsetX, shadowOffsetY);
        end(glString);
        glString.setHeight(stringParams.getHeight());
    }

    private void trimAndCreateString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int startX,
                                     int fontSize, StringParams stringParams, int maxWidth, float r, float g, float b, float a,
                                     StringOffsetType offsetType, int indent, boolean shadow, int shadowOffsetX, int shadowOffsetY) {
        do {
            int trimSize = glyphsBuilder.getWidth(string, fontSize, maxWidth, true);
            String subString = string.substring(0, trimSize);
            stringParams.setX(startX + offsetFunctions[offsetType.ordinal()].get(subString, glyphsBuilder, fontSize));

            if (shadow) {
                float prevX = stringParams.getX();
                float prevY = stringParams.getY();
                int prevHeight = stringParams.getHeight();
                stringParams.setX(prevX + shadowOffsetX / 2.0f);
                stringParams.setY(prevY + shadowOffsetY / 2.0f);
                stringParams.getColor().set(0.0f, 0.0f, 0.0f, a);
                createString(glString, glyphsBuilder, subString, fontSize, stringParams, indent);
                stringParams.setX(prevX);
                stringParams.setY(prevY);
                stringParams.setHeight(prevHeight);
            }

            stringParams.getColor().set(r, g, b, a);
            createString(glString, glyphsBuilder, subString, fontSize, stringParams, indent);
            string = string.substring(trimSize);
        } while (!string.isEmpty());
    }

    @Override
    public void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int x, int y, int fontSize,
                             float r, float g, float b, float a, StringOffsetType offsetType, boolean shadow,
                             int shadowOffsetX, int shadowOffsetY) {
        createString(glString, glyphsBuilder, string, x, y, fontSize, r, g, b, a, offsetType, DEFAULT_INDENT, shadow,
                shadowOffsetX, shadowOffsetY);
    }

    private void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int x, int y, int fontSize,
                              float r, float g, float b, float a, StringOffsetType offsetType, int indent, boolean shadow,
                              int shadowOffsetX, int shadowOffsetY) {
        begin(glString);

        if (shadow) {
            createString(glString, glyphsBuilder, string, x + shadowOffsetX / 2.0f, y + shadowOffsetY / 2.0f, fontSize, 0.0f, 0.0f, 0.0f, a,
                    offsetType, indent);
        }

        createString(glString, glyphsBuilder, string, x, y, fontSize, r, g, b, a, offsetType, indent);
        end(glString);
        glString.setHeight(stringParams.getHeight());
    }

    private void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, float x, float y, int fontSize,
                              float r, float g, float b, float a, StringOffsetType offsetType, int indent) {
        stringParams.getColor().set(r, g, b, a);
        stringParams.setX(x + offsetFunctions[offsetType.ordinal()].get(string, glyphsBuilder, fontSize));
        stringParams.setY(y);
        stringParams.setHeight(0);

        int offset = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == NEW_LINE_CHAR) {
                String substring = string.substring(offset, i);
                createString(glString, glyphsBuilder, substring, fontSize, stringParams, indent);
                offset = i + 1;
            }
        }
        createString(glString, glyphsBuilder, string.substring(offset), fontSize, stringParams, indent);
    }

    private void createString(AbstractGLString glString, GlyphsBuilder glyphsBuilder, String string, int fontSize,
                              StringParams stringParams, int indent) {
        GlyphsData glyphsData = glyphsBuilder.getGlyphsData(string, fontSize);
        List<Glyph> glyphs = glyphsData.getGlyphs();
        int height = (int) (glyphsBuilder.getHeight(NEW_LINE_STRING, fontSize)) + indent;

        glString.checkBuffers(glyphs.size());

        for (int glyphIndex = 0; glyphIndex < glyphs.size(); glyphIndex++) {
            Glyph glyph = glyphs.get(glyphIndex);
            addGlyph(glyph, stringParams.getX(), stringParams.getY(), stringParams.getColor().x, stringParams.getColor().y,
                    stringParams.getColor().z, stringParams.getColor().w, glString);
        }

        glString.setWidth(glyphsData.getWidth());
        stringParams.addHeight(height);
        stringParams.setY(stringParams.getY() + height);
    }

    private void addGlyph(Glyph glyph, float x, float y, float r, float g, float b, float a, AbstractGLString glString) {
        float x1 = x + glyph.getX1();
        float x2 = x + glyph.getX2();
        float y1 = y + glyph.getY1();
        float y2 = y + glyph.getY2();
        addVertex(x1, y1, glyph.getU1(), glyph.getV1(), glString.getVertexBuffer());
        addVertex(x1, y2, glyph.getU1(), glyph.getV2(), glString.getVertexBuffer());
        addVertex(x2, y2, glyph.getU2(), glyph.getV2(), glString.getVertexBuffer());
        addVertex(x2, y1, glyph.getU2(), glyph.getV1(), glString.getVertexBuffer());
        addMaterial(r, g, b, a, glyph.getTextureHandle(), glString.getMaterialBuffer());
    }

    private void addVertex(float x, float y, float u, float v, FloatBuffer vertexBuffer) {
        vertexBuffer.put(x);
        vertexBuffer.put(y);
        vertexBuffer.put(u);
        vertexBuffer.put(v);
    }

    private void addMaterial(float r, float g, float b, float a, long textureHandle, ByteBuffer materialBuffer) {
        materialBuffer.putFloat(r);
        materialBuffer.putFloat(g);
        materialBuffer.putFloat(b);
        materialBuffer.putFloat(a);
        materialBuffer.putLong(textureHandle);
        materialBuffer.putInt(textureHandle != 0 ? 1 : 0);
        materialBuffer.putInt(0);
        materialBuffer.putLong(0);
        materialBuffer.putFloat(0.0f);
        materialBuffer.putFloat(0.0f);
        materialBuffer.putInt(1);
        materialBuffer.putInt(0);
        materialBuffer.putInt(0);
        materialBuffer.putInt(0);
    }
}