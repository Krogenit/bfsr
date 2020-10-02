package ru.krogenit.bfsr.client.render;

import org.joml.Vector4f;
import ru.krogenit.bfsr.client.camera.Camera;
import ru.krogenit.bfsr.client.font.FontRenderer;
import ru.krogenit.bfsr.client.gui.Gui;
import ru.krogenit.bfsr.client.gui.ingame.GuiInGame;
import ru.krogenit.bfsr.client.model.TexturedQuad;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.core.Main;
import ru.krogenit.bfsr.math.Transformation;
import ru.krogenit.bfsr.world.WorldClient;

public class Renderer {
	
	public static TexturedQuad quad = new TexturedQuad();
	
	private final Core core;
	private final Camera camera;
	private final FontRenderer fontRenderer;
	private final BaseShader shader;
	private final GuiInGame guiInGame;
	private int drawCalls;

	public Renderer(Core core) {
		this.core = core;
		this.camera = new Camera(core.getWidth(), core.getHeight());
		this.fontRenderer = new FontRenderer();
		this.shader = new BaseShader();
		this.shader.initialize();
		this.guiInGame = new GuiInGame();
		this.guiInGame.init();
	}
	
	public void update(double delta) {
		camera.update(delta);
		guiInGame.update(delta);
	}
	
	public void input() {
		if(core.getWorld() != null) guiInGame.input();
	}

	public void render() {
		fontRenderer.updateOrthographicMatrix(camera.getOrthographicMatrix());
		shader.enable();
		shader.enableTexture();
		shader.setOrthoMatrix(camera.getOrthographicMatrix());
		shader.setColor(new Vector4f(1,1,1,1));
		Main.checkGlError("init shaders");
		Transformation.updateViewMatrix(camera);
		OpenGLHelper.alphaGreater(0.5f);

		WorldClient world = core.getWorld();
		if (world != null) {
			world.renderAmbient(shader);
			world.renderBackParticles();
			OpenGLHelper.alphaGreater(0.75f);
			world.renderEntities(shader);
//			shader.setColor(new Vector4f(1,1,1,1));
//			OpenGLHelper.bindTexture(TextureLoader.getTexture(Textures.particleWreckSaimonSmall0Wreck1).getId());
//			shader.setModelViewMatrix(Transformation.getModelViewMatrix(0, 0, 0, 100, 100, EnumZoomFactor.Default));
//			quad.render();
			Main.checkGlError("entitys");
			fontRenderer.render(EnumParticlePositionType.Default);
			world.renderParticles();
			Main.checkGlError("particles");
			if(core.getSettings().isDebug()) {
				shader.disable();
				camera.setupOldOpenGLMatrixForDebugRendering();
				world.renderDebug();
				Main.checkGlError("debug");
			}
			shader.enable();
			guiInGame.render(shader);
			Main.checkGlError("gui in game");
			fontRenderer.render(EnumParticlePositionType.GuiInGame);
			shader.enable();
		}
		
		Gui gui = core.getCurrentGui();
		if (gui != null) {
			OpenGLHelper.alphaGreater(0.01f);
			gui.render(shader);
			fontRenderer.render(EnumParticlePositionType.Gui);
		}
		
		OpenGLHelper.alphaGreater(0.01f);
		fontRenderer.render(EnumParticlePositionType.Last);
	}

	public Camera getCamera() {
		return camera;
	}

	public BaseShader getShader() {
		return shader;
	}

	public void resize(int width, int height) {
		camera.resize(width, height);
		guiInGame.resize(width, height);
	}

	public void clear() {
		guiInGame.clearByExit();
		camera.clear();
	}
	
	public GuiInGame getGuiInGame() {
		return guiInGame;
	}
	
	public void setDrawCalls(int drawCalls) {
		this.drawCalls = drawCalls;
	}
	
	public int getDrawCalls() {
		return drawCalls;
	}

	public void increaseDrawCalls() {
		this.drawCalls++;
	}
}
