package ru.krogenit.bfsr.client.font;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import ru.krogenit.bfsr.client.font_new.FontRegistry;
import ru.krogenit.bfsr.client.font_new.StringCache;
import ru.krogenit.bfsr.client.font_new.StringRenderer;
import ru.krogenit.bfsr.client.loader.MeshLoader;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.render.OpenGLHelper;
import ru.krogenit.bfsr.client.render.Renderer;
import ru.krogenit.bfsr.client.shader.FontShader;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.math.EnumZoomFactor;
import ru.krogenit.bfsr.math.Transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

/**
 * TODO: Удалить старые методы отрисовки текста. Перенести необходимое в StringRenderer, либо сюда
 */
@Deprecated
public class FontRenderer {

	@Getter private static FontRenderer instance;

	private int vaoForTextRendering;
	private int posVbo;
	private int textVbo;

	private static final MeshLoader MESH_LOADER = new MeshLoader();
	private static final HashMap<EnumParticlePositionType, HashMap<ru.krogenit.bfsr.client.font.FontType, List<GUIText>>> TEXTS = new HashMap<>();
	private final FontShader fontShader = new FontShader();
	private final Vector4f shadowColor = new Vector4f(0, 0, 0, 1);
	private final Vector2f shadowOffset = new Vector2f(1, 1);

	private final StringRenderer stringRenderer = new StringRenderer();

	public FontRenderer() {
		this.fontShader.initialize();
		initVao();
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		instance = this;
	}

	private void initVao() {
		vaoForTextRendering = glGenVertexArrays();
		glBindVertexArray(vaoForTextRendering);
		posVbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, posVbo);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		textVbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, textVbo);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
	}

	public static void loadText(GUIText text) {
		text.setRemoved(false);
		ru.krogenit.bfsr.client.font.FontType font = text.getFont();
		TextMeshData data = font.loadText(text);
		int vao = MESH_LOADER.loadToVAO(text, data.getVertexPositions(), data.getTextureCoords());
		text.setMeshInfo(vao, data.getVertexCount());

		HashMap<ru.krogenit.bfsr.client.font.FontType, List<GUIText>> mapByFontType = TEXTS.computeIfAbsent(text.getPositionType(), k -> new HashMap<>());
		List<GUIText> textBatch = mapByFontType.computeIfAbsent(font, k -> new ArrayList<>());

		textBatch.add(text);
	}

	public static void updateText(GUIText text) {
		int vao = text.getTextMeshVao();
		ru.krogenit.bfsr.client.font.FontType font = text.getFont();
		TextMeshData data = font.loadText(text);
		text.setVertexCount(data.getVertexCount());
		int[] vbos = MESH_LOADER.getVBOs(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbos[0]);
		glBufferData(GL_ARRAY_BUFFER, data.getVertexPositions(), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, vbos[1]);
		glBufferData(GL_ARRAY_BUFFER, data.getTextureCoords(), GL_STATIC_DRAW);
	}

	public static void removeText(GUIText text) {
		if (!text.isRemoved()) {
			text.setRemoved(true);
			MESH_LOADER.removeVao(text.getTextMeshVao());
			HashMap<ru.krogenit.bfsr.client.font.FontType, List<GUIText>> mapByFontType = TEXTS.get(text.getPositionType());
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
		stringRenderer.updateMatrix(orthographicMatrix);
	}

	public void render(EnumParticlePositionType positionType) {
		fontShader.enable();

		HashMap<ru.krogenit.bfsr.client.font.FontType, List<GUIText>> mapByFontType = TEXTS.get(positionType);
		if (mapByFontType != null) {
			for (ru.krogenit.bfsr.client.font.FontType font : mapByFontType.keySet()) {
				OpenGLHelper.activateTexture0();
				OpenGLHelper.bindTexture(font.getTextureAtlas());
				for (GUIText text : mapByFontType.get(font)) {
					renderText(text);
				}
			}
		}
	}

	public void clear() {
		fontShader.clear();
		glDeleteBuffers(posVbo);
		glDeleteBuffers(textVbo);
		glDeleteVertexArrays(vaoForTextRendering);
		stringRenderer.clear();
	}

	private void prepare() {
		fontShader.enable();
	}

	private void renderText(GUIText text) {
		glBindVertexArray(text.getMesh());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		Core core = Core.getCore();
		Renderer renderer = core.getRenderer();
		if (text.isShadow()) {
			fontShader.setColor(new Vector4f(shadowColor.x, shadowColor.y, shadowColor.z, text.getColor().w));
			fontShader.setModelViewMatrix(Transformation.getModelViewMatrix(text, shadowOffset));
			glDrawArrays(GL_TRIANGLES, 0, text.getVertexCount());
			renderer.setDrawCalls(renderer.getDrawCalls() + 1);
		}
		fontShader.setColor(text.getColor());
		fontShader.setModelViewMatrix(Transformation.getModelViewMatrix(text));
		glDrawArrays(GL_TRIANGLES, 0, text.getVertexCount());
		renderer.setDrawCalls(renderer.getDrawCalls() + 1);
	}

	public void renderString(FontRegistry font, String text, int x, int y, int fontSize, float r, float g, float b, float a, EnumZoomFactor factor) {
		StringCache stringCache = font.getStringCache();
		stringCache.setFontSize(fontSize);
		stringRenderer.renderString(stringCache, text, x, y, r, g, b, a, factor);
	}

	public void renderString(FontRegistry font, String text, int x, int y, float fontSizeX, float fontSizeY, float r, float g, float b, float a,
							 boolean isCentered, EnumZoomFactor factor, boolean shadow, float maxLineWidth) {
		Renderer renderer = Core.getCore().getRenderer();
		StringCache stringCache = font.getStringCache();

		if (shadow) {
			stringRenderer.renderString(stringCache, text, (int)(x + shadowOffset.x), (int)(y + shadowOffset.y), shadowColor.x, shadowColor.y, shadowColor.z, a, factor);
			renderer.increaseDrawCalls();
		}

		stringRenderer.renderString(stringCache, text, x, y, r, g, b, a, factor);
		renderer.increaseDrawCalls();
	}
}
