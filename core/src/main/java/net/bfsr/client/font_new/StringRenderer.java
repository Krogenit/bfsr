package net.bfsr.client.font_new;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import net.bfsr.client.render.VAO;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL44C;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class StringRenderer {
    private static final int INFORMATION_SIZE = 4;

    private final String newLineString = "\n";
    private final char newLineChar = '\n';
    private int bufferSize = 256;
    private FloatBuffer vertexUVBuffer = BufferUtils.createFloatBuffer(bufferSize);
    private FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(bufferSize);
    private final IStringXOffsetSupplier[] offsetFunctions = new IStringXOffsetSupplier[3];
    private final StringParams stringParams = new StringParams();

    private final ISortedStringCreated defaultSortedStringCompleteFunction = (glString1, r, g, b, a) -> {};
    private final IStringCreationMethod dynamicStringRenderingMethod = (glString1, vaoListTexture) -> updateAttributes(vaoListTexture);
    private final BiFunction<GLString, Integer, VAOListTexture> dynamicVaoListTextureFunction = (glString, index1) -> {
        List<VAOListTexture> vaoList = glString.getVaoList();
        if (vaoList.size() > index1) {
            return vaoList.get(index1);
        } else {
            VAO vao = VAO.create(2);
            vao.createAttribute(0, INFORMATION_SIZE, 0);
            vao.createAttribute(1, INFORMATION_SIZE, 1);
            vao.bindAttribs();
            VAOListTexture vaoListTexture = new VAOListTexture(vao, 0);
            glString.add(vaoListTexture);
            return vaoListTexture;
        }
    };
    private final ISortedStringCreated widthRestrictedSortedStringCreationFunction = (glString1, r, g, b, a) -> stringParams.getColor().set(r, g, b, a);
    private final float defaultIndent = 1.25f;

    public StringRenderer() {
        offsetFunctions[StringOffsetType.DEFAULT.ordinal()] = (string, stringCache) -> 0.0f;
        offsetFunctions[StringOffsetType.CENTERED.ordinal()] = (string, stringCache) -> -stringCache.getStringWidth(string) / 2.0f;
        offsetFunctions[StringOffsetType.RIGHT.ordinal()] = (string, stringCache) -> -stringCache.getStringWidth(string);
    }

    public void updateDynamicGLString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a) {
        updateDynamicGLString(glString, stringCache, string, x, y, fontSize, r, g, b, a, StringOffsetType.DEFAULT);
    }

    public void updateDynamicGLString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a, StringOffsetType offsetType) {
        SortedString sortedString = new SortedString();
        createSortedString(glString, stringCache, string, x + offsetFunctions[offsetType.ordinal()].get(string, stringCache), y, fontSize, r, g, b, a, sortedString);
        createGLString(sortedString, glString, dynamicStringRenderingMethod, dynamicVaoListTextureFunction);
    }

    public void compileGLString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a) {
        SortedString sortedString = new SortedString();
        createSortedString(glString, stringCache, string, x, y, fontSize, r, g, b, a, sortedString);
        createGLString(sortedString, glString, (glString1, vaoListTexture) -> compileAndCreateNewVAOListTexture(vaoListTexture), (glString1, integer) -> {
            VAOListTexture vaoListTexture = new VAOListTexture(VAO.create(2), GL11.glGenLists(1));
            glString1.add(vaoListTexture);
            return vaoListTexture;
        });
    }

    public void updateDynamicGLString(GLString glString, StringCache stringCache, String text, float x, float y, int fontSize, float r, float g, float b, float a,
                                      IStringCreationMethod renderingMethod, BiFunction<GLString, Integer, VAOListTexture> vaoListTextureSupplier, int maxWidth, StringOffsetType offsetType) {
        createSortedString(glString, stringCache, text, x, y, fontSize, r, g, b, a, renderingMethod, vaoListTextureSupplier, maxWidth, offsetType, defaultIndent);
    }

    public void updateDynamicGLString(GLString glString, StringCache stringCache, String text, float x, float y, int fontSize, float r, float g, float b, float a,
                                      int maxWidth, StringOffsetType offsetType) {
        createSortedString(glString, stringCache, text, x, y, fontSize, r, g, b, a, dynamicStringRenderingMethod, dynamicVaoListTextureFunction, maxWidth, offsetType, defaultIndent);
    }

    public void updateDynamicGLString(GLString glString, StringCache stringCache, String text, float x, float y, int fontSize, float r, float g, float b, float a,
                                      int maxWidth, StringOffsetType offsetType, float indent) {
        createSortedString(glString, stringCache, text, x, y, fontSize, r, g, b, a, dynamicStringRenderingMethod, dynamicVaoListTextureFunction, maxWidth, offsetType, indent);
    }

    private void createSortedString(GLString glString, StringCache stringCache, String text, float x, float y, int fontSize, float r, float g, float b, float a,
                                    IStringCreationMethod renderingMethod, BiFunction<GLString, Integer, VAOListTexture> vaoListTextureSupplier, int maxWidth,
                                    StringOffsetType offsetType, float indent) {
        stringParams.getColor().set(r, g, b, a);
        stringParams.setX(x);
        stringParams.setY(y);
        stringParams.setHeight(0);
        int offset = 0;
        char[] chars = text.toCharArray();
        SortedString sortedString = new SortedString();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == newLineChar) {
                String string = text.substring(offset, i);
                if (string.isEmpty()) {
                    float height = stringCache.getHeight(newLineString) * indent;
                    stringParams.setY(stringParams.getY() + height);
                    stringParams.addHeight((int) height);
                } else {
                    trimAndCreateString(glString, stringCache, string, stringParams, widthRestrictedSortedStringCreationFunction, maxWidth, offsetType, indent, fontSize, sortedString);
                }
                offset = i + 1;
            }
        }
        String string = text.substring(offset).trim();
        trimAndCreateString(glString, stringCache, string, stringParams, widthRestrictedSortedStringCreationFunction, maxWidth, offsetType, indent, fontSize, sortedString);
        glString.setHeight(stringParams.getHeight());
        createGLString(sortedString, glString, renderingMethod, vaoListTextureSupplier);
    }

    private void trimAndCreateString(GLString glString, StringCache stringCache, String string, StringParams stringParams, ISortedStringCreated completeFunction, int maxWidth,
                                     StringOffsetType offsetType, float indent, int fontSize, SortedString sortedString) {
        do {
            String temp = stringCache.trimStringToWidthSaveWords(string, maxWidth);
            Vector4f color1 = stringParams.getColor();
            createSortedString(glString, stringCache, temp, stringParams.getX() + offsetFunctions[offsetType.ordinal()].get(temp, stringCache), stringParams.getY(), fontSize,
                    color1.x, color1.y, color1.z, color1.w, completeFunction, sortedString);
            string = string.replace(temp, "").trim();
            float height = stringCache.getHeight(temp) * indent * stringParams.getScale();
            stringParams.addHeight((int) height);
            stringParams.setY(stringParams.getY() + height);
        } while (!string.isEmpty());
    }

    private void createSortedString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a, SortedString sortedString) {
        createSortedString(glString, stringCache, string, x, y, fontSize, r, g, b, a, defaultSortedStringCompleteFunction, sortedString);
    }

    private void createSortedString(GLString glString, StringCache stringCache, String string, float x, float y, int fontSize, float r, float g, float b, float a,
                                    ISortedStringCreated completeFunction, SortedString sortedString) {
        stringCache.setFontSize(fontSize);
        Entry entry = stringCache.cacheString(string);

        for (int glyphIndex = 0, colorIndex = 0; glyphIndex < entry.glyphs.length; glyphIndex++) {
            Glyph glyph = entry.glyphs[glyphIndex];

            while (colorIndex < entry.colors.length && glyph.stringIndex >= entry.colors[colorIndex++].stringIndex) {
                Vector3f color = stringCache.getColor(entry.colors[colorIndex].colorCode);
                r = color.x;
                g = color.y;
                b = color.z;
            }

            addGlyph(sortedString, glyph, x, y, r, g, b, a);
        }

        glString.setWidth(entry.advance / 2);
        completeFunction.complete(glString, r, g, b, a);
    }

    private void createGLString(SortedString sortedString, GLString glString, IStringCreationMethod renderingMethod, BiFunction<GLString, Integer, VAOListTexture> vaoListTextureSupplier) {
        TIntObjectMap<List<GlyphAndColor>> glyphsByTexture = sortedString.getGlyphAndColorList();
        TIntObjectIterator<List<GlyphAndColor>> iterator = glyphsByTexture.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            iterator.advance();
            int texture = iterator.key();
            List<GlyphAndColor> glyphs = iterator.value();
            VAOListTexture vaoListTexture = vaoListTextureSupplier.apply(glString, index++);
            vaoListTexture.setTexture(texture);

            for (int i = 0; i < glyphs.size(); i++) {
                addGlyph(glyphs.get(i));
            }

            renderingMethod.render(glString, vaoListTexture);
        }

        glString.setVAOCount(index);
    }

    private void updateAttributes(VAOListTexture vaoListTexture) {
        vertexUVBuffer.flip();
        colorBuffer.flip();
        vaoListTexture.getVao().updateAttribute(0, vertexUVBuffer, GL44C.GL_DYNAMIC_STORAGE_BIT);
        vaoListTexture.getVao().updateAttribute(1, colorBuffer, GL44C.GL_DYNAMIC_STORAGE_BIT);
        vaoListTexture.setVertexCount(vertexUVBuffer.remaining() / INFORMATION_SIZE);
        vertexUVBuffer.clear();
        colorBuffer.clear();
    }

    private void compileAndCreateNewVAOListTexture(VAOListTexture vaoListTexture) {
        vertexUVBuffer.flip();
        colorBuffer.flip();
        vaoListTexture.getVao().createAttribute(0, vertexUVBuffer, INFORMATION_SIZE);
        vaoListTexture.getVao().createAttribute(1, colorBuffer, INFORMATION_SIZE);
        vaoListTexture.getVao().bindAttribs();
        GL11.glNewList(vaoListTexture.getCallList(), GL11.GL_COMPILE);
        vaoListTexture.getVao().bind();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, vaoListTexture.getTexture());
        GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexUVBuffer.remaining() / INFORMATION_SIZE);
        GL11.glEndList();
        vaoListTexture.getVao().unbind();
        vertexUVBuffer.clear();
        colorBuffer.clear();
    }

    private void addGlyph(SortedString sortedString, Glyph glyph, float startX, float startY, float r, float g, float b, float a) {
        float x1 = startX + glyph.x / 2.0f;
        float x2 = startX + (glyph.x + glyph.texture.width) / 2.0f;
        float y1 = startY + glyph.y / 2.0f;
        float y2 = startY + (glyph.y + glyph.texture.height) / 2.0f;
        TIntObjectMap<List<GlyphAndColor>> glyphAndColorList = sortedString.getGlyphAndColorList();
        List<GlyphAndColor> glyphs = glyphAndColorList.get(glyph.texture.textureName);
        if (glyphs == null) {
            glyphs = new ArrayList<>(32);
            glyphAndColorList.put(glyph.texture.textureName, glyphs);
        }
        glyphs.add(new GlyphAndColor(x1, y1, x2, y2, glyph.texture.u1, glyph.texture.v1, glyph.texture.u2, glyph.texture.v2, r, g, b, a));
    }

    private void addGlyph(GlyphAndColor glyphAndColor) {
        if (vertexUVBuffer.position() == bufferSize) {
            resizeBuffer();
        }
        addVertexWithUV(glyphAndColor.getX1(), glyphAndColor.getY1(), glyphAndColor.getU1(), glyphAndColor.getV1(), glyphAndColor.getR(), glyphAndColor.getG(), glyphAndColor.getB(), glyphAndColor.getA());
        addVertexWithUV(glyphAndColor.getX1(), glyphAndColor.getY2(), glyphAndColor.getU1(), glyphAndColor.getV2(), glyphAndColor.getR(), glyphAndColor.getG(), glyphAndColor.getB(), glyphAndColor.getA());
        addVertexWithUV(glyphAndColor.getX2(), glyphAndColor.getY2(), glyphAndColor.getU2(), glyphAndColor.getV2(), glyphAndColor.getR(), glyphAndColor.getG(), glyphAndColor.getB(), glyphAndColor.getA());
        addVertexWithUV(glyphAndColor.getX2(), glyphAndColor.getY1(), glyphAndColor.getU2(), glyphAndColor.getV1(), glyphAndColor.getR(), glyphAndColor.getG(), glyphAndColor.getB(), glyphAndColor.getA());
    }

    private void addVertexWithUV(float x, float y, float u, float v, float r, float g, float b, float a) {
        vertexUVBuffer.put(x);
        vertexUVBuffer.put(y);
        vertexUVBuffer.put(u);
        vertexUVBuffer.put(v);
        colorBuffer.put(r);
        colorBuffer.put(g);
        colorBuffer.put(b);
        colorBuffer.put(a);
    }

    private void resizeBuffer() {
        bufferSize <<= 1;
        FloatBuffer newVertexAndUVBuffer = BufferUtils.createFloatBuffer(bufferSize);
        vertexUVBuffer.flip();
        newVertexAndUVBuffer.put(vertexUVBuffer);
        vertexUVBuffer = newVertexAndUVBuffer;
        FloatBuffer newColorBuffer = BufferUtils.createFloatBuffer(bufferSize);
        colorBuffer.flip();
        newColorBuffer.put(colorBuffer);
        colorBuffer = newColorBuffer;
    }

    public void render(GLString string) {
        List<VAOListTexture> vaoList = string.getVaoList();
        for (int i = 0; i < string.getVAOCount(); i++) {
            VAOListTexture vaoListTexture = vaoList.get(i);
            vaoListTexture.getVao().bind();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, vaoListTexture.getTexture());
            GL11.glDrawArrays(GL11.GL_QUADS, 0, vaoListTexture.getVertexCount());
        }
    }
}