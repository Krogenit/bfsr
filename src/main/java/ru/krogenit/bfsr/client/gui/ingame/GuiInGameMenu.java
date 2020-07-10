package ru.krogenit.bfsr.client.gui.ingame;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import ru.krogenit.bfsr.client.gui.Gui;
import ru.krogenit.bfsr.client.gui.GuiSettings;
import ru.krogenit.bfsr.client.gui.GuiTextureObject;
import ru.krogenit.bfsr.client.gui.button.Button;
import ru.krogenit.bfsr.client.gui.button.ButtonBase;
import ru.krogenit.bfsr.client.input.Keyboard;
import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.render.Renderer;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.TextureObject;
import ru.krogenit.bfsr.math.Transformation;

public class GuiInGameMenu extends Gui {

	private final TextureObject logoBFSR;

	public GuiInGameMenu() {
		logoBFSR = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiLogoBFSR));
	}

	@Override
	public void init() {
		super.init();

		ButtonBase button = new ButtonBase(0, new Vector2f(center.x, center.y - 30), "gui.ingamemenu.backtogame");
		button.setTextColor(1, 1, 1, 1);
		buttons.add(button);
		button = new ButtonBase(1, new Vector2f(center.x, center.y + 30), "gui.ingamemenu.settings");
		button.setTextColor(1, 1, 1, 1);
		buttons.add(button);
		button = new ButtonBase(2, new Vector2f(center.x, center.y + 180), "gui.ingamemenu.tomainmenu");
		button.setTextColor(1, 1, 1, 1);
		buttons.add(button);
//		button = new ButtonBase(3, new Vector2f(center.x, center.y + 90), "gui.ingamemenu.stats");
//		button.setTextColor(1, 1, 1, 1);
//		buttons.add(button);
		logoBFSR.setPosition(center.x, center.y - 200);
		logoBFSR.setScale(256, 256);
	}

	@Override
	public void input() {
		super.input();

		if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
			Core.getCore().setCurrentGui(null);
		}
	}

	@Override
	protected void onButtonLeftClick(Button b) {
		switch (b.getId()) {
		case 0:
			Core.getCore().setCurrentGui(null);
			break;
		case 1:
			Core.getCore().setCurrentGui(new GuiSettings(this));
			break;
		case 2:
			Core.getCore().quitToMainMenu();
			break;
		case 3:
			break;
		}
	}

	@Override
	public void render(BaseShader shader) {
		shader.setColor(new Vector4f(0, 0, 0, 0.5f));
		shader.setModelViewMatrix(Transformation.getModelViewMatrixGui(width / 2.0F, height / 2.0f, 0, width, height));
		shader.disableTexture();
		Renderer.quad.render();

		logoBFSR.render(shader);
		super.render(shader);
	}
}
