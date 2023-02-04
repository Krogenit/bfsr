package net.bfsr.client.render.font;

import net.bfsr.client.render.font.string.DynamicGLString;
import net.bfsr.client.render.font.string.GLString;
import net.bfsr.client.shader.font.FontShader;
import net.bfsr.core.Core;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;

public class StringRenderer {
    public static final int VERTEX_DATA_SIZE = 8 << 2;

    private final String newLineString = "\n";
    private final char newLineChar = '\n';
    private final IStringXOffsetSupplier[] offsetFunctions = new IStringXOffsetSupplier[3];
    private final StringParams stringParams = new StringParams();

    private final int defaultIndent = 0;

    private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(256);
    private LongBuffer textureBuffer = BufferUtils.createLongBuffer(128);
    private final FontShader fontShader = new FontShader();
    private final DynamicGLString glString = new DynamicGLString();

    public StringRenderer() {
        offsetFunctions[StringOffsetType.DEFAULT.ordinal()] = (string, stringCache) -> 0.0f;
        offsetFunctions[StringOffsetType.CENTERED.ordinal()] = (string, stringCache) -> -stringCache.getStringWidth(string) / 2.0f;
        offsetFunctions[StringOffsetType.RIGHT.ordinal()] = (string, stringCache) -> -stringCache.getStringWidth(string);
    }

    public void init() {
        fontShader.load();
        fontShader.init();

        glString.init();
    }

    private void begin() {
        vertexBuffer.clear();
        textureBuffer.clear();
    }

    private void end(GLString glString) {
        glString.fillBuffer(vertexBuffer.flip(), textureBuffer.flip());
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
        begin();
        trimAndCreateString(glString, stringCache, text, x, stringParams, maxWidth, offsetType, indent);
        end(glString);
        glString.setHeight(stringParams.getHeight());
    }

    private void trimAndCreateString(GLString glString, StringCache stringCache, String string, float startX, StringParams stringParams, int maxWidth,
                                     StringOffsetType offsetType, int indent) {
        do {
            String temp = stringCache.trimStringToWidthSaveWords(string, maxWidth);
            stringParams.setX(startX + offsetFunctions[offsetType.ordinal()].get(temp, stringCache));
            createString(glString, stringCache, temp, stringParams, indent);
            string = string.replace(temp, "").trim();
            float height = stringCache.getHeight(temp) + indent;
            stringParams.setY(stringParams.getY() + height);
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
        begin();
        createString(glString, stringCache, string, stringParams, indent);
        end(glString);
        glString.setHeight(stringParams.getHeight());
    }

    private void createString(GLString glString, StringCache stringCache, String string, StringParams stringParams, int indent) {
        Entry entry = stringCache.cacheString(string);
        float height = stringCache.getHeight(newLineString) + indent;
        int stringAdvance = 0;
        float offsetX = 0;

        for (int glyphIndex = 0, colorIndex = 0; glyphIndex < entry.glyphs.length; glyphIndex++) {
            Glyph glyph = entry.glyphs[glyphIndex];

            if (string.charAt(glyph.stringIndex) == newLineChar) {
                stringParams.setY(stringParams.getY() + height);
                stringParams.addHeight((int) height);
                offsetX = -stringAdvance / 2.0f;
            }

            while (colorIndex < entry.colors.length && glyph.stringIndex >= entry.colors[colorIndex++].stringIndex) {
                Vector3f color = stringCache.getColor(entry.colors[colorIndex].colorCode);
                stringParams.setColor(color.x, color.y, color.z);
            }

            addGlyph(glyph, stringParams.getX() + offsetX, stringParams.getY(), stringParams.getColor().x, stringParams.getColor().y, stringParams.getColor().z, stringParams.getColor().w);
            stringAdvance += glyph.advance;
        }

        glString.setWidth(entry.advance / 2);
        stringParams.addHeight((int) height);
    }

    public void render(GLString string) {
        fontShader.enable();
        fontShader.setModelMatrix(string.getMatrixBuffer());
        string.bind();
        GL11.glDrawArrays(GL11.GL_QUADS, 0, string.getVertexCount());
        Core.getCore().getRenderer().increaseDrawCalls();
    }

    public void render(String string, StringCache stringCache, int fontSize, float x, float y) {
        render(string, stringCache, fontSize, x, y, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void render(String string, StringCache stringCache, int fontSize, float x, float y, float r, float g, float b, float a) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a);
        render(glString);
    }

    public int render(String string, StringCache stringCache, int fontSize, float x, float y, float r, float g, float b, float a, int maxWidth) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, maxWidth);
        render(glString);
        return glString.getHeight();
    }

    public int render(String string, StringCache stringCache, int fontSize, float x, float y, float r, float g, float b, float a, int maxWidth, int indent) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, maxWidth, indent);
        render(glString);
        return glString.getHeight();
    }

    private void addGlyph(Glyph glyph, float startX, float startY, float r, float g, float b, float a) {
        float x1 = startX + glyph.x / 2.0f;
        float x2 = startX + (glyph.x + glyph.texture.width) / 2.0f;
        float y1 = startY + glyph.y / 2.0f;
        float y2 = startY + (glyph.y + glyph.texture.height) / 2.0f;
        if (vertexBuffer.position() == vertexBuffer.capacity()) {
            FloatBuffer newBuffer = BufferUtils.createFloatBuffer(vertexBuffer.capacity() << 1);
            vertexBuffer.flip();
            newBuffer.put(vertexBuffer);
            vertexBuffer = newBuffer;
        }
        if (textureBuffer.position() == textureBuffer.capacity()) {
            LongBuffer newBuffer = BufferUtils.createLongBuffer(textureBuffer.capacity() << 1);
            textureBuffer.flip();
            newBuffer.put(textureBuffer);
            textureBuffer = newBuffer;
        }
        addVertex(x1, y1, glyph.texture.u1, glyph.texture.v1, r, g, b, a);
        addVertex(x1, y2, glyph.texture.u1, glyph.texture.v2, r, g, b, a);
        addVertex(x2, y2, glyph.texture.u2, glyph.texture.v2, r, g, b, a);
        addVertex(x2, y1, glyph.texture.u2, glyph.texture.v1, r, g, b, a);
        textureBuffer.put(glyph.texture.textureHandle);
    }

    private void addVertex(float x, float y, float u, float v, float r, float g, float b, float a) {
        vertexBuffer.put(x);
        vertexBuffer.put(y);
        vertexBuffer.put(u);
        vertexBuffer.put(v);
        vertexBuffer.put(r);
        vertexBuffer.put(g);
        vertexBuffer.put(b);
        vertexBuffer.put(a);
    }
}