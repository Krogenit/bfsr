package ru.krogenit.bfsr.client.font_new;

import org.joml.Matrix4f;
import ru.krogenit.bfsr.client.render.OpenGLHelper;
import ru.krogenit.bfsr.client.shader.FontShaderSpecial;
import ru.krogenit.bfsr.client.shader.FontShaderTextured;
import ru.krogenit.bfsr.math.EnumZoomFactor;
import ru.krogenit.bfsr.math.Transformation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class StringRenderer {

    private static final int UNDERLINE_OFFSET = 1;
    private static final int UNDERLINE_THICKNESS = 2;
    private static final int STRIKETHROUGH_OFFSET = -6;
    private static final int STRIKETHROUGH_THICKNESS = 2;

    private static final int VERTEX_INFORMATION_SIZE = 4;
    private static final int COLOR_INFORMATION_SIZE = 4;

    private int vboVertex;
    private int vboColor;
    private int vao;
    private final FontShaderTextured fontShaderDynamic;
    private final FontShaderSpecial fontShaderSpecial;
    private static final List<Float> vertices = new ArrayList<>();
    private static final List<Float> colors = new ArrayList<>();

    public StringRenderer() {
        initDynamicVao();
        this.fontShaderDynamic = new FontShaderTextured();
        this.fontShaderDynamic.initialize();
        this.fontShaderSpecial = new FontShaderSpecial();
        this.fontShaderSpecial.initialize();
    }

    private void initDynamicVao() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vboVertex = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertex);
        glVertexAttribPointer(0, VERTEX_INFORMATION_SIZE, GL_FLOAT, false, 0, 0);
        vboColor = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboColor);
        glVertexAttribPointer(1, COLOR_INFORMATION_SIZE, GL_FLOAT, false, 0, 0);
    }

    public int renderString(StringCache stringCache, String str, int startX, int startY, float r, float g, float b, float a, EnumZoomFactor factor) {
        if (str == null || str.isEmpty()) {
            return startX;
        }

        Entry entry = stringCache.cacheString(str);
        Matrix4f orthographicViewMatrixForFontRendering = Transformation.getOrthographicViewMatrixForFontRendering(startX, startY, factor);

        fontShaderDynamic.enable();
        fontShaderDynamic.setModelViewMatrix(orthographicViewMatrixForFontRendering);
        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        vertices.clear();
        colors.clear();

        int fontStyle = Font.PLAIN;
        int boundTextureName = 0;

        for (int glyphIndex = 0, colorIndex = 0; glyphIndex < entry.glyphs.length; glyphIndex++) {
            while (colorIndex < entry.colors.length && entry.glyphs[glyphIndex].stringIndex >= entry.colors[colorIndex].stringIndex) {
                fontStyle = entry.colors[colorIndex].fontStyle;
                colorIndex++;
            }

            Glyph glyph = entry.glyphs[glyphIndex];
            GlyphCache.Entry texture = glyph.texture;
            int glyphX = glyph.x;

            char c = str.charAt(glyph.stringIndex);
            if (c >= '0' && c <= '9') {
                int oldWidth = texture.width;
                texture = stringCache.digitGlyphs[fontStyle][c - '0'].texture;
                int newWidth = texture.width;
                glyphX += (oldWidth - newWidth) >> 1;
            }

            if (boundTextureName != texture.textureName) {//TODO: sort by texture
                if (boundTextureName != 0) {
                    glBindBuffer(GL_ARRAY_BUFFER, vboVertex);
                    glBufferData(GL_ARRAY_BUFFER, listToArray(vertices), GL_DYNAMIC_DRAW);
                    glBindBuffer(GL_ARRAY_BUFFER, vboColor);
                    glBufferData(GL_ARRAY_BUFFER, listToArray(colors), GL_DYNAMIC_DRAW);
                    glDrawArrays(GL_QUADS, 0, vertices.size() / VERTEX_INFORMATION_SIZE);
                    vertices.clear();
                    colors.clear();
                }

                OpenGLHelper.bindTexture(texture.textureName);
                boundTextureName = texture.textureName;
            }

            float x1 = (glyphX) / 2.0F;
            float x2 = (glyphX + texture.width) / 2.0F;
            float y1 = (glyph.y) / 2.0F;
            float y2 = (glyph.y + texture.height) / 2.0F;
            addVertexWithUV(x1, y1, texture.u1, texture.v1, r, g, b, a);
            addVertexWithUV(x1, y2, texture.u1, texture.v2, r, g, b, a);
            addVertexWithUV(x2, y2, texture.u2, texture.v2, r, g, b, a);
            addVertexWithUV(x2, y1, texture.u2, texture.v1, r, g, b, a);
        }

        glBindBuffer(GL_ARRAY_BUFFER, vboVertex);
        glBufferData(GL_ARRAY_BUFFER, listToArray(vertices), GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, vboColor);
        glBufferData(GL_ARRAY_BUFFER, listToArray(colors), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_QUADS, 0, vertices.size() / VERTEX_INFORMATION_SIZE);

        if (entry.specialRender) {
            fontShaderSpecial.enable();
            fontShaderSpecial.setModelViewMatrix(orthographicViewMatrixForFontRendering);
            int renderStyle = 0;

            for (int glyphIndex = 0, colorIndex = 0; glyphIndex < entry.glyphs.length; glyphIndex++) {
                while (colorIndex < entry.colors.length && entry.glyphs[glyphIndex].stringIndex >= entry.colors[colorIndex].stringIndex) {
                    renderStyle = entry.colors[colorIndex].renderStyle;
                    colorIndex++;
                }

                Glyph glyph = entry.glyphs[glyphIndex];

                int glyphSpace = glyph.advance - glyph.texture.width;

                if ((renderStyle & ColorCode.UNDERLINE) != 0) {
                    float x1 = (glyph.x - glyphSpace) / 2.0F;
                    float x2 = (glyph.x + glyph.advance) / 2.0F;
                    float y1 = (UNDERLINE_OFFSET) / 2.0F;
                    float y2 = (UNDERLINE_OFFSET + UNDERLINE_THICKNESS) / 2.0F;

                    addVertex(x1, y1, r, g, b, a);
                    addVertex(x1, y2, r, g, b, a);
                    addVertex(x2, y2, r, g, b, a);
                    addVertex(x2, y1, r, g, b, a);
                }

                if ((renderStyle & ColorCode.STRIKETHROUGH) != 0) {
                    float x1 = (glyph.x - glyphSpace) / 2.0F;
                    float x2 = (glyph.x + glyph.advance) / 2.0F;
                    float y1 = (STRIKETHROUGH_OFFSET) / 2.0F;
                    float y2 = (STRIKETHROUGH_OFFSET + STRIKETHROUGH_THICKNESS) / 2.0F;

                    addVertex(x1, y1, r, g, b, a);
                    addVertex(x1, y2, r, g, b, a);
                    addVertex(x2, y2, r, g, b, a);
                    addVertex(x2, y1, r, g, b, a);
                }
            }

            glBindBuffer(GL_ARRAY_BUFFER, vboVertex);
            glBufferData(GL_ARRAY_BUFFER, listToArray(vertices), GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, vboColor);
            glBufferData(GL_ARRAY_BUFFER, listToArray(colors), GL_DYNAMIC_DRAW);
            glDrawArrays(GL_QUADS, 0, vertices.size() / VERTEX_INFORMATION_SIZE);
        }

        return entry.advance / 2;
    }

    private void addVertex(float x, float y, float r, float g, float b, float a) {
        addVertexWithUV(x, y, 0, 0, r, g, b, a);
    }

    private void addVertexWithUV(float x, float y, float u, float v, float r, float g, float b, float a) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(u);
        vertices.add(v);
        colors.add(r);
        colors.add(g);
        colors.add(b);
        colors.add(a);
    }

    public void updateMatrix(Matrix4f orthographicMatrix) {
        fontShaderDynamic.enable();
        fontShaderDynamic.setOrthographicMatrix(orthographicMatrix);
        fontShaderSpecial.enable();
        fontShaderSpecial.setOrthographicMatrix(orthographicMatrix);
    }

    private static float[] listToArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public void clear() {
        glDeleteBuffers(vboVertex);
        glDeleteVertexArrays(vao);
        fontShaderDynamic.clear();
    }
}