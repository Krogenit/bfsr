package net.bfsr.client.font_new;

import net.bfsr.client.camera.Camera;
import net.bfsr.client.shader.font.FontShader;
import net.bfsr.core.Core;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;

public class StringRenderer {
    public static final int VERTEX_DATA_SIZE = 8 * 4;

    private final String newLineString = "\n";
    private final char newLineChar = '\n';
    private final IStringXOffsetSupplier[] offsetFunctions = new IStringXOffsetSupplier[3];
    private final StringParams stringParams = new StringParams();

    private final ISortedStringCreated defaultSortedStringCompleteFunction = (glString1, r, g, b, a) -> {};
    private final ISortedStringCreated widthRestrictedSortedStringCreationFunction = (glString1, r, g, b, a) -> stringParams.getColor().set(r, g, b, a);
    private final float defaultIndent = 1.25f;

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

    private void begin(StringCache stringCache, int fontSize) {
        vertexBuffer.clear();
        textureBuffer.clear();
        stringCache.setFontSize(fontSize);
    }

    private void end(GLString glString) {
        vertexBuffer.flip();
        textureBuffer.flip();
        glString.fillBuffer(vertexBuffer, textureBuffer);
    }

    public void createString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    private void createString(GLString glString, StringCache stringCache, String text, float x, float y, float r, float g, float b, float a, int maxWidth) {
        createString(glString, stringCache, text, x, y, r, g, b, a, maxWidth, StringOffsetType.DEFAULT);
    }

    private void createString(GLString glString, StringCache stringCache, String text, float x, float y, float r, float g, float b, float a, int maxWidth,
                              StringOffsetType offsetType) {
        createString(glString, stringCache, text, x, y, r, g, b, a, maxWidth, offsetType, defaultIndent);
    }

    private void createString(GLString glString, StringCache stringCache, String text, float x, float y, float r, float g, float b, float a, int maxWidth,
                              StringOffsetType offsetType, float indent) {
        stringParams.getColor().set(r, g, b, a);
        stringParams.setX(x);
        stringParams.setY(y);
        stringParams.setHeight(0);
        int offset = 0;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == newLineChar) {
                String string = text.substring(offset, i);
                if (string.isEmpty()) {
                    float height = stringCache.getHeight(newLineString) * indent;
                    stringParams.setY(stringParams.getY() + height);
                    stringParams.addHeight((int) height);
                } else {
                    trimAndCreateString(glString, stringCache, string, stringParams, widthRestrictedSortedStringCreationFunction, maxWidth, offsetType, indent);
                }
                offset = i + 1;
            }
        }
        String string = text.substring(offset).trim();
        trimAndCreateString(glString, stringCache, string, stringParams, widthRestrictedSortedStringCreationFunction, maxWidth, offsetType, indent);
        glString.setHeight(stringParams.getHeight());
    }

    private void trimAndCreateString(GLString glString, StringCache stringCache, String string, StringParams stringParams, ISortedStringCreated completeFunction, int maxWidth,
                                     StringOffsetType offsetType, float indent) {
        do {
            String temp = stringCache.trimStringToWidthSaveWords(string, maxWidth);
            Vector4f color1 = stringParams.getColor();
            createString(glString, stringCache, temp, stringParams.getX() + offsetFunctions[offsetType.ordinal()].get(temp, stringCache), stringParams.getY(),
                    color1.x, color1.y, color1.z, color1.w, completeFunction);
            string = string.replace(temp, "").trim();
            float height = stringCache.getHeight(temp) * indent * stringParams.getScale();
            stringParams.addHeight((int) height);
            stringParams.setY(stringParams.getY() + height);
        } while (!string.isEmpty());
    }

    private void createString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a, StringOffsetType offsetType) {
        begin(stringCache, fontSize);
        createString(glString, stringCache, string, x + offsetFunctions[offsetType.ordinal()].get(string, stringCache), y, r, g, b, a, defaultSortedStringCompleteFunction);
        end(glString);
    }

    private void createString(GLString glString, StringCache stringCache, String string, float x, float y, float r, float g, float b, float a,
                              ISortedStringCreated completeFunction) {
        Entry entry = stringCache.cacheString(string);

        for (int glyphIndex = 0, colorIndex = 0; glyphIndex < entry.glyphs.length; glyphIndex++) {
            Glyph glyph = entry.glyphs[glyphIndex];

            while (colorIndex < entry.colors.length && glyph.stringIndex >= entry.colors[colorIndex++].stringIndex) {
                Vector3f color = stringCache.getColor(entry.colors[colorIndex].colorCode);
                r = color.x;
                g = color.y;
                b = color.z;
            }

            addGlyph(glyph, x, y, r, g, b, a);
        }

        glString.setWidth(entry.advance / 2);
        completeFunction.complete(glString, r, g, b, a);
    }

    public void render(GLString string) {
        string.bind();
        GL11.glDrawArrays(GL11.GL_QUADS, 0, string.getVertexCount());
    }

    public void render(String string, StringCache stringCache, int fontSize, float x, float y) {
        render(string, stringCache, fontSize, x, y, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void render(String string, StringCache stringCache, int fontSize, float x, float y, float r, float g, float b, float a) {
        createString(glString, stringCache, string, x, y, fontSize, r, g, b, a);
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, Camera.VIEW_MATRIX_UBO, Core.getCore().getRenderer().getCamera().getGUIViewMatrixUBO());
        render(glString);
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