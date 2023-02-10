package net.bfsr.client.renderer.font;

import net.bfsr.client.renderer.font.string.GLString;
import net.bfsr.client.renderer.instanced.BufferType;
import net.bfsr.client.renderer.instanced.InstancedRenderer;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class StringRenderer {
    private static final int INITIAL_QUADS_COUNT = 128;

    private final String newLineString = "\n";
    private final char newLineChar = '\n';
    private final IStringXOffsetSupplier[] offsetFunctions = new IStringXOffsetSupplier[3];
    private final StringParams stringParams = new StringParams();

    private final int defaultIndent = 0;

    private final GLString glString = new GLString();

    public StringRenderer() {
        offsetFunctions[StringOffsetType.DEFAULT.ordinal()] = (string, stringCache) -> 0.0f;
        offsetFunctions[StringOffsetType.CENTERED.ordinal()] = (string, stringCache) -> -stringCache.getStringWidth(string) / 2.0f;
        offsetFunctions[StringOffsetType.RIGHT.ordinal()] = (string, stringCache) -> -stringCache.getStringWidth(string);
    }

    public void init() {
        glString.init(INITIAL_QUADS_COUNT);
    }

    private void begin(GLString glString) {
        glString.clearBuffers();
    }

    private void end(GLString glString) {
        glString.flipBuffers();
    }

    public void createString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize) {
        createString(glString, stringCache, string, x, y, fontSize, 1.0f, 1.0f, 1.0f, 1.0f, StringOffsetType.DEFAULT);
    }

    public void createString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    private void createString(GLString glString, StringCache stringCache, String text, float x, float y, int fontSize, float r, float g, float b, float a, int maxWidth) {
        createString(glString, stringCache, text, x, y, fontSize, r, g, b, a, maxWidth, StringOffsetType.DEFAULT);
    }

    private void createString(GLString glString, StringCache stringCache, String text, float x, float y, int fontSize, float r, float g, float b, float a, int maxWidth,
                              StringOffsetType offsetType) {
        createString(glString, stringCache, text, x, y, fontSize, r, g, b, a, maxWidth, offsetType, defaultIndent);
    }

    private void createString(GLString glString, StringCache stringCache, String text, float x, float y, int fontSize, float r, float g, float b, float a, int maxWidth, int indent) {
        createString(glString, stringCache, text, x, y, fontSize, r, g, b, a, maxWidth, StringOffsetType.DEFAULT, indent);
    }

    private void createString(GLString glString, StringCache stringCache, String text, float x, float y, int fontSize, float r, float g, float b, float a, int maxWidth,
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

    private void trimAndCreateString(GLString glString, StringCache stringCache, String string, float startX, StringParams stringParams, int maxWidth,
                                     StringOffsetType offsetType, int indent) {
        do {
            int trimSize = stringCache.sizeString(string, maxWidth, true);
            String subString = string.substring(0, trimSize);
            stringParams.setX(startX + offsetFunctions[offsetType.ordinal()].get(subString, stringCache));
            createString(glString, stringCache, subString, stringParams, indent);
            string = string.substring(trimSize);
        } while (!string.isEmpty());
    }

    public void createString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a, StringOffsetType offsetType) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, offsetType, defaultIndent);
    }

    private void createString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a, StringOffsetType offsetType, int indent) {
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

    private void createString(GLString glString, StringCache stringCache, String string, StringParams stringParams, int indent) {
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

    public void render(GLString glString, BufferType bufferType) {
        InstancedRenderer.INSTANCE.addToRenderPipeLine(glString, bufferType);
    }

    public void render(String string, StringCache stringCache, int fontSize, float x, float y, BufferType bufferType) {
        render(string, stringCache, fontSize, x, y, 1.0f, 1.0f, 1.0f, 1.0f, bufferType);
    }

    public void render(String string, StringCache stringCache, int fontSize, float x, float y, float r, float g, float b, float a, BufferType bufferType) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a);
        render(glString, bufferType);
    }

    public int render(String string, StringCache stringCache, int fontSize, float x, float y, float r, float g, float b, float a, int maxWidth, BufferType bufferType) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, maxWidth);
        render(glString, bufferType);
        return glString.getHeight();
    }

    public int render(String string, StringCache stringCache, int fontSize, float x, float y, float r, float g, float b, float a, int maxWidth, int indent, BufferType bufferType) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, maxWidth, indent);
        render(glString, bufferType);
        return glString.getHeight();
    }

    private void addGlyph(Glyph glyph, float startX, float startY, float r, float g, float b, float a, GLString glString) {
        float x1 = startX + glyph.x / 2.0f;
        float x2 = startX + (glyph.x + glyph.texture.width) / 2.0f;
        float y1 = startY + glyph.y / 2.0f;
        float y2 = startY + (glyph.y + glyph.texture.height) / 2.0f;
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
        materialBuffer.putInt(0);//padding
    }
}