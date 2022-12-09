package net.bfsr.client.font;

import lombok.Getter;
import net.bfsr.client.font_new.FontType;
import net.bfsr.client.font_new.GLString;
import net.bfsr.client.font_new.StringCache;
import net.bfsr.client.font_new.StringRenderer;
import net.bfsr.client.loader.MeshLoader;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.FontShader;
import net.bfsr.client.shader.FontShaderTextured;
import net.bfsr.core.Core;
import net.bfsr.math.Transformation;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL15C.glBufferData;

/**
 * TODO: Удалить старые методы отрисовки текста. Перенести необходимое в StringRenderer, либо сюда
 */
@Deprecated
public class FontRenderer {
    @Getter
    private static FontRenderer instance;

    private int vaoForTextRendering;
    private int posVbo;
    private int textVbo;

    private static final MeshLoader MESH_LOADER = new MeshLoader();
    private static final HashMap<EnumParticlePositionType, HashMap<net.bfsr.client.font.FontType, List<GUIText>>> TEXTS = new HashMap<>();
    private final FontShader fontShader = new FontShader();
    private final FontShaderTextured texturedFontShader = new FontShaderTextured();
    private final Vector4f shadowColor = new Vector4f(0, 0, 0, 1);
    private final Vector2f shadowOffset = new Vector2f(1, 1);

    @Getter
    private final StringRenderer stringRenderer = new StringRenderer();

    public FontRenderer() {
        instance = this;
    }

    public void init() {
        fontShader.load();
        fontShader.init();
        texturedFontShader.load();
        texturedFontShader.init();
        initVao();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        stringRenderer.init();
    }

    private void initVao() {
        vaoForTextRendering = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoForTextRendering);
        posVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posVbo);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
        textVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textVbo);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
    }

    public static void loadText(GUIText text) {
        text.setRemoved(false);
        net.bfsr.client.font.FontType font = text.getFont();
        TextMeshData data = font.loadText(text);
        int vao = MESH_LOADER.loadToVAO(text, data.getVertexPositions(), data.getTextureCoords());
        text.setMeshInfo(vao, data.getVertexCount());

        HashMap<net.bfsr.client.font.FontType, List<GUIText>> mapByFontType = TEXTS.computeIfAbsent(text.getPositionType(), k -> new HashMap<>());
        List<GUIText> textBatch = mapByFontType.computeIfAbsent(font, k -> new ArrayList<>());

        textBatch.add(text);
    }

    public static void updateText(GUIText text) {
        int vao = text.getTextMeshVao();
        net.bfsr.client.font.FontType font = text.getFont();
        TextMeshData data = font.loadText(text);
        text.setVertexCount(data.getVertexCount());
        int[] vbos = MESH_LOADER.getVBOs(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos[0]);
        glBufferData(GL15.GL_ARRAY_BUFFER, data.getVertexPositions(), GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos[1]);
        glBufferData(GL15.GL_ARRAY_BUFFER, data.getTextureCoords(), GL15.GL_STATIC_DRAW);
    }

    public static void removeText(GUIText text) {
        if (!text.isRemoved()) {
            text.setRemoved(true);
            MESH_LOADER.removeVao(text.getTextMeshVao());
            HashMap<net.bfsr.client.font.FontType, List<GUIText>> mapByFontType = TEXTS.get(text.getPositionType());
            if (mapByFontType != null) {
                List<GUIText> textBatch = mapByFontType.get(text.getFont());
                textBatch.remove(text);
                if (textBatch.isEmpty()) {
                    mapByFontType.remove(text.getFont());

                    if (mapByFontType.isEmpty()) TEXTS.remove(text.getPositionType());
                }
            }
        }
    }

    public void updateOrthographicMatrix(Matrix4f orthographicMatrix) {
        fontShader.enable();
        fontShader.setOrthoMatrix(orthographicMatrix);
        texturedFontShader.enable();
        texturedFontShader.setOrthographicMatrix(orthographicMatrix);
    }

    @Deprecated
    public void render(EnumParticlePositionType positionType) {
        fontShader.enable();

        HashMap<net.bfsr.client.font.FontType, List<GUIText>> mapByFontType = TEXTS.get(positionType);
        if (mapByFontType != null) {
            for (net.bfsr.client.font.FontType font : mapByFontType.keySet()) {
                OpenGLHelper.activateTexture0();
                OpenGLHelper.bindTexture(font.getTextureAtlas());
                for (GUIText text : mapByFontType.get(font)) {
                    renderText(text);
                }
            }
        }
    }

    public void clear() {
        fontShader.delete();
        GL15.glDeleteBuffers(posVbo);
        GL15.glDeleteBuffers(textVbo);
        GL30.glDeleteVertexArrays(vaoForTextRendering);
    }

    @Deprecated
    private void renderText(GUIText text) {
        GL30.glBindVertexArray(text.getMesh());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        Core core = Core.getCore();
        Renderer renderer = core.getRenderer();
        if (text.isShadow()) {
            fontShader.setColor(new Vector4f(shadowColor.x, shadowColor.y, shadowColor.z, text.getColor().w));
            fontShader.setModelViewMatrix(Transformation.getModelViewMatrix(text, new Vector2f(shadowOffset.x + text.getShadowOffset().x, shadowOffset.y + text.getShadowOffset().y)));
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());
            renderer.setDrawCalls(renderer.getDrawCalls() + 1);
        }
        fontShader.setColor(text.getColor());
        fontShader.setModelViewMatrix(Transformation.getModelViewMatrix(text));
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());
        renderer.setDrawCalls(renderer.getDrawCalls() + 1);
    }

    public GLString createString(FontType font, String string, int x, int y, int fontSize, float r, float g, float b, float a) {
        GLString glString = new GLString();
        glString.init();
        stringRenderer.createString(glString, font.getStringCache(), string, x, y, fontSize, r, g, b, a);
        return glString;
    }

    public void render(List<GLString> strings) {
        fontShader.enable();
        for (int i = 0; i < strings.size(); i++) {
            stringRenderer.render(strings.get(i));
        }
    }

    @Deprecated
    public void renderString(FontType font, String text, int x, int y, float fontSizeY, float r, float g, float b, float a, boolean shadow, float maxLineWidth) {
        Renderer renderer = Core.getCore().getRenderer();
        StringCache stringCache = font.getStringCache();

        if (shadow) {
            stringRenderer.render(text, stringCache, (int) fontSizeY, (int) (x + shadowOffset.x), (int) (y + shadowOffset.y), shadowColor.x, shadowColor.y, shadowColor.z, a);
            renderer.increaseDrawCalls();
        }

        stringRenderer.render(text, stringCache, (int) fontSizeY, x, y, r, g, b, a);
        renderer.increaseDrawCalls();
    }
}
