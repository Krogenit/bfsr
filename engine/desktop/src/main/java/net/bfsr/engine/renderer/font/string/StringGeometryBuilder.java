package net.bfsr.engine.renderer.font.string;

import net.bfsr.engine.renderer.font.*;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class StringGeometryBuilder extends AbstractStringGeometryBuilder {
    private final String newLineString = "\n";
    private final char newLineChar = '\n';
    private final IStringXOffsetSupplier[] offsetFunctions = new IStringXOffsetSupplier[3];
    private final StringParams stringParams = new StringParams();

    private final int defaultIndent = 0;

    public StringGeometryBuilder() {
        offsetFunctions[StringOffsetType.DEFAULT.ordinal()] = (string, stringCache) -> 0;
        offsetFunctions[StringOffsetType.CENTERED.ordinal()] = (string, stringCache) -> -stringCache.getStringWidth(string) / 2;
        offsetFunctions[StringOffsetType.RIGHT.ordinal()] = (string, stringCache) -> -stringCache.getStringWidth(string);
    }

    private void begin(AbstractGLString glString) {
        glString.clearBuffers();
    }

    private void end(AbstractGLString glString) {
        glString.flipBuffers();
    }

    public void createString(AbstractGLString glString, StringCache stringCache, String string, int x, int y, int fontSize) {
        createString(glString, stringCache, string, x, y, fontSize, 1.0f, 1.0f, 1.0f, 1.0f, StringOffsetType.DEFAULT);
    }

    @Override
    public void createString(AbstractGLString glString, StringCache stringCache, String string, int x, int y, int fontSize, float r, float g, float b, float a) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    @Override
    public void createString(AbstractGLString glString, StringCache stringCache, String text, int x, int y, int fontSize, float r, float g, float b, float a, int maxWidth) {
        createString(glString, stringCache, text, x, y, fontSize, r, g, b, a, maxWidth, StringOffsetType.DEFAULT);
    }

    private void createString(AbstractGLString glString, StringCache stringCache, String text, int x, int y, int fontSize, float r, float g, float b, float a, int maxWidth,
                              StringOffsetType offsetType) {
        createString(glString, stringCache, text, x, y, fontSize, r, g, b, a, maxWidth, offsetType, defaultIndent);
    }

    @Override
    public void createString(AbstractGLString glString, StringCache stringCache, String text, int x, int y, int fontSize, float r, float g, float b, float a, int maxWidth, int indent) {
        createString(glString, stringCache, text, x, y, fontSize, r, g, b, a, maxWidth, StringOffsetType.DEFAULT, indent);
    }

    private void createString(AbstractGLString glString, StringCache stringCache, String text, int x, int y, int fontSize, float r, float g, float b, float a, int maxWidth,
                              StringOffsetType offsetType, int indent) {
        stringCache.setFontSize(fontSize);
        stringParams.getColor().set(r, g, b, a);
        stringParams.setY(y);
        stringParams.setHeight(0);
        begin(glString);
        trimAndCreateString(glString, stringCache, text, x, stringParams, maxWidth, offsetType, indent);
        end(glString);
        glString.setHeight(stringParams.getHeight());
    }

    private void trimAndCreateString(AbstractGLString glString, StringCache stringCache, String string, int startX, StringParams stringParams, int maxWidth,
                                     StringOffsetType offsetType, int indent) {
        do {
            int trimSize = stringCache.sizeString(string, maxWidth, true);
            String subString = string.substring(0, trimSize);
            stringParams.setX(startX + offsetFunctions[offsetType.ordinal()].get(subString, stringCache));
            createString(glString, stringCache, subString, stringParams, indent);
            string = string.substring(trimSize);
        } while (!string.isEmpty());
    }

    @Override
    public void createString(AbstractGLString glString, StringCache stringCache, String string, int x, int y, int fontSize, float r, float g, float b, float a, StringOffsetType offsetType) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, offsetType, defaultIndent);
    }

    private void createString(AbstractGLString glString, StringCache stringCache, String string, int x, int y, int fontSize, float r, float g, float b, float a, StringOffsetType offsetType, int indent) {
        stringCache.setFontSize(fontSize);
        stringParams.getColor().set(r, g, b, a);
        stringParams.setX(x + offsetFunctions[offsetType.ordinal()].get(string, stringCache));
        stringParams.setY(y);
        stringParams.setHeight(0);
        begin(glString);
        int offset = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == newLineChar) {
                String substring = string.substring(offset, i);
                createString(glString, stringCache, substring, stringParams, indent);
                offset = i + 1;
            }
        }
        createString(glString, stringCache, string.substring(offset), stringParams, indent);
        end(glString);
        glString.setHeight(stringParams.getHeight());
    }

    private void createString(AbstractGLString glString, StringCache stringCache, String string, StringParams stringParams, int indent) {
        Entry entry = stringCache.cacheString(string);
        int height = (int) stringCache.getHeight(newLineString) + indent;

        glString.checkBuffers(entry.glyphs.length);

        for (int glyphIndex = 0, colorIndex = 0; glyphIndex < entry.glyphs.length; glyphIndex++) {
            Glyph glyph = entry.glyphs[glyphIndex];

            while (colorIndex < entry.colors.length && glyph.stringIndex >= entry.colors[colorIndex++].stringIndex) {
                Vector3f color = stringCache.getColor(entry.colors[colorIndex].colorCode);
                stringParams.setColor(color.x, color.y, color.z);
            }

            addGlyph(glyph, stringParams.getX(), stringParams.getY(), stringParams.getColor().x, stringParams.getColor().y, stringParams.getColor().z, stringParams.getColor().w, glString);
        }

        glString.setWidth(entry.advance / 2);
        stringParams.addHeight(height);
        stringParams.setY(stringParams.getY() + height);
    }

    private void addGlyph(Glyph glyph, int startX, int startY, float r, float g, float b, float a, AbstractGLString glString) {
        int x1 = startX + glyph.x / 2;
        int x2 = startX + (glyph.x + glyph.texture.width) / 2;
        int y1 = startY + glyph.y / 2;
        int y2 = startY + (glyph.y + glyph.texture.height) / 2;
        addVertex(x1, y1, glyph.texture.u1, glyph.texture.v1, glString.getVertexBuffer());
        addVertex(x1, y2, glyph.texture.u1, glyph.texture.v2, glString.getVertexBuffer());
        addVertex(x2, y2, glyph.texture.u2, glyph.texture.v2, glString.getVertexBuffer());
        addVertex(x2, y1, glyph.texture.u2, glyph.texture.v1, glString.getVertexBuffer());
        addMaterial(r, g, b, a, glyph.texture.textureHandle, glString.getMaterialBuffer());
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
    }
}