package ru.krogenit.bfsr.client.gui.menu;

import org.joml.Vector2f;

import ru.krogenit.bfsr.client.gui.Gui;
import ru.krogenit.bfsr.client.gui.GuiSettings;
import ru.krogenit.bfsr.client.gui.GuiTextureObject;
import ru.krogenit.bfsr.client.gui.button.Button;
import ru.krogenit.bfsr.client.gui.button.ButtonBase;
import ru.krogenit.bfsr.client.gui.multiplayer.GuiConnect;
import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.core.Main;
import ru.krogenit.bfsr.entity.TextureObject;

public class GuiMainMenu extends Gui {
	private final TextureObject bfsrText, bfsrLogo;

	public GuiMainMenu() {
		bfsrText = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiBfsrText2));
		bfsrLogo = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiLogoBFSR));
	}

	@Override
	public void init() {
		super.init();
		
		bfsrLogo.setPosition(center.x, center.y - 150);
		bfsrLogo.setScale(180f, 180f);
		
		bfsrText.setPosition(center.x, center.y - 150);
		bfsrText.setScale(1553f/2.25f, 158f/2f);

		buttons.add(new ButtonBase(0, new Vector2f(center.x, center.y - 45), new Vector2f(260, 40), "gui.mainmenu.singleplayer", new Vector2f(0.9f, 0.8f)));
		buttons.add(new ButtonBase(1, new Vector2f(center.x, center.y), new Vector2f(260, 40), "gui.mainmenu.multiplayer", new Vector2f(0.9f, 0.8f)));
		buttons.add(new ButtonBase(2, new Vector2f(center.x, center.y + 45), new Vector2f(260, 40), "gui.mainmenu.options", new Vector2f(0.9f, 0.8f)));
		buttons.add(new ButtonBase(3, new Vector2f(center.x, center.y + 90), new Vector2f(260, 40), "gui.mainmenu.quit", new Vector2f(0.9f, 0.8f)));
	}

	@Override
	protected void onButtonLeftClick(Button b) {
		switch (b.getId()) {
		case 0:
			Core.getCore().startSingleplayer();
			Core.getCore().setCurrentGui(null);
			return;
		case 1:
//			Core.getCore().connectToServer("localhost", 25565);
			Core.getCore().setCurrentGui(new GuiConnect(this));
			return;
		case 2:
			Core.getCore().setCurrentGui(new GuiSettings(this));
			return;
		case 3:
			Main.isRunning = false;
		}
	}

	@Override
	public void update(double delta) {
		super.update(delta);
	}

	@Override
	public void render(BaseShader shader) {
		bfsrLogo.render(shader);
		bfsrText.render(shader);
		super.render(shader);
	}
}
