package ru.krogenit.bfsr.client.font;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.client.loader.MeshLoader;
import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.render.OpenGLHelper;
import ru.krogenit.bfsr.client.render.Renderer;
import ru.krogenit.bfsr.client.shader.FontShader;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.math.EnumZoomFactor;
import ru.krogenit.bfsr.math.Transformation;
import ru.krogenit.bfsr.util.PathHelper;

public class FontRenderer {

	private final int vaoForTextRendering;
	private final int posVbo;
	private final int textVbo;

	private static final MeshLoader MESH_LOADER = new MeshLoader();
	private static final HashMap<EnumParticlePositionType, HashMap<FontType, List<GUIText>>> TEXTS = new HashMap<>();
	private final FontShader shader;
	private final Vector4f shadowColor = new Vector4f(0, 0, 0, 1);
	private final Vector2f shadowOffset = new Vector2f(1, 1);

	public static final FontType ARIAL = new FontType(TextureLoader.getTexture(TextureRegister.fontArialNew, false).getId(), new File(PathHelper.font, "arial_new.fnt"));
	public static final FontType BAHNSCHRIFT = new FontType(TextureLoader.getTexture(TextureRegister.fontBahnschrift, false).getId(), new File(PathHelper.font, "bahnschrift.fnt"));

	public static final FontType CONSOLA = new FontType(TextureLoader.getTexture(TextureRegister.fontConsola, false).getId(), new File(PathHelper.font, "consola.fnt"));
	public static final FontType NASALIZATION_RG = new FontType(TextureLoader.getTexture(TextureRegister.fontNasalization_rg, false).getId(), new File(PathHelper.font, "nasalization-rg.fnt"));

	public static final FontType CONTHRAX = new FontType(TextureLoader.getTexture(TextureRegister.fontConthrax, false).getId(), new File(PathHelper.font, "conthrax-sb.fnt"));
	public static final FontType XOLONIUM = new FontType(TextureLoader.getTexture(TextureRegister.fontXolonium, false).getId(), new File(PathHelper.font, "xolonium-regular.fnt"));

	public FontRenderer() {
		shader = new FontShader();
		vaoForTextRendering = glGenVertexArrays();
		glBindVertexArray(vaoForTextRendering);
		posVbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, posVbo);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		textVbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, textVbo);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	public static void loadText(GUIText text) {
		text.setRemoved(false);
		FontType font = text.getFont();
		TextMeshData data = font.loadText(text);
		int vao = MESH_LOADER.loadToVAO(text, data.getVertexPositions(), data.getTextureCoords());
		text.setMeshInfo(vao, data.getVertexCount());

		HashMap<FontType, List<GUIText>> mapByFontType = TEXTS.computeIfAbsent(text.getPositionType(), k -> new HashMap<>());
		List<GUIText> textBatch = mapByFontType.computeIfAbsent(font, k -> new ArrayList<>());

		textBatch.add(text);
	}

	public static void updateText(GUIText text) {
		int vao = text.getTextMeshVao();
		FontType font = text.getFont();
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
			HashMap<FontType, List<GUIText>> mapByFontType = TEXTS.get(text.getPositionType());
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

	public void updateOrthoMatrix(Matrix4f orthoMatrix) {
		shader.enable();
		shader.setOrthoMatrix(orthoMatrix);
	}

	public void render(EnumParticlePositionType positionType) {
//		prepare();
		shader.enable();

		HashMap<FontType, List<GUIText>> mapByFontType = TEXTS.get(positionType);
		if (mapByFontType != null) {
			for (FontType font : mapByFontType.keySet()) {
				OpenGLHelper.activateTexture0();
				OpenGLHelper.bindTexture(font.getTextureAtlas());
				for (GUIText text : mapByFontType.get(font)) {
					renderText(text);
				}
			}
		}
//		endRendering();
	}

	public void clear() {
		shader.clear();
	}

	private void prepare() {
		shader.enable();
	}

	private void renderText(GUIText text) {
		glBindVertexArray(text.getMesh());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		Core core = Core.getCore();
		Renderer renderer = core.getRenderer();
		if (text.isShadow()) {
			shader.setColor(new Vector4f(shadowColor.x, shadowColor.y, shadowColor.z, text.getColor().w));
			shader.setModelViewMatrix(Transformation.getModelViewMatrix(text, shadowOffset));
			glDrawArrays(GL_TRIANGLES, 0, text.getVertexCount());
			renderer.setDrawCalls(renderer.getDrawCalls() + 1);
		}
		shader.setColor(text.getColor());
		shader.setModelViewMatrix(Transformation.getModelViewMatrix(text));
		glDrawArrays(GL_TRIANGLES, 0, text.getVertexCount());
		renderer.setDrawCalls(renderer.getDrawCalls() + 1);
//		glDisableVertexAttribArray(0);
//		glDisableVertexAttribArray(1);
//		glBindVertexArray(0);
	}

	public void renderString(String text, Vector2f pos, Vector2f fontSize, FontType font, Vector4f color, boolean isCentered, EnumZoomFactor factor, boolean shadow) {
		shader.enable();
		TextMeshData data = font.loadText(text, fontSize, 1.0f, isCentered, 1f);
		glBindVertexArray(vaoForTextRendering);
		glBindBuffer(GL_ARRAY_BUFFER, posVbo);
		glBufferData(GL_ARRAY_BUFFER, data.getVertexPositions(), GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, textVbo);
		glBufferData(GL_ARRAY_BUFFER, data.getTextureCoords(), GL_STATIC_DRAW);
//		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		OpenGLHelper.bindTexture(font.getTextureAtlas());
		if (shadow) {
			shader.setColor(new Vector4f(shadowColor.x, shadowColor.y, shadowColor.z, color.w));
			shader.setModelViewMatrix(Transformation.getModelViewMatrixForTextRendering(pos, factor, shadowOffset));
			glDrawArrays(GL_TRIANGLES, 0, data.getVertexCount());
		}
		shader.setColor(color);
		shader.setModelViewMatrix(Transformation.getModelViewMatrixForTextRendering(pos, factor));
		glDrawArrays(GL_TRIANGLES, 0, data.getVertexCount());
//		glDisableVertexAttribArray(0);
//		glDisableVertexAttribArray(1);
//		glBindVertexArray(0);
	}

	private void endRendering() {
		shader.disable();
	}
}
