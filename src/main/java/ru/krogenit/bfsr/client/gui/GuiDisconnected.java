package ru.krogenit.bfsr.client.gui;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.client.font.FontRenderer;
import ru.krogenit.bfsr.client.font.GUIText;
import ru.krogenit.bfsr.client.gui.button.Button;
import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.render.OpenGLHelper;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.TextureObject;
import ru.krogenit.bfsr.math.Transformation;

public class GuiDisconnected extends Gui {

	private final String errorMessage;
	private final String description;
	private final GUIText text;
	private final GUIText textDescription;
	private final Gui prevGui;
	private final TextureObject background;
	
	public GuiDisconnected(Gui prevGui, String errorMessage, String description) {
		this.errorMessage = errorMessage;
		this.prevGui = prevGui;
		this.description = description;
		this.background = new GuiTextureObject(TextureLoader.getTexture(TextureRegister.guiAdd));
		this.text = new GUIText(errorMessage, new Vector2f(1,1), FontRenderer.XOLONIUM,
				Transformation.getOffsetByScale(new Vector2f(center.x - 286, center.y - 128)), new Vector4f(1,1,1,1), false, EnumParticlePositionType.Gui);
		this.textDescription = new GUIText(description, new Vector3f(1,1,1.2f), FontRenderer.CONSOLA,
				Transformation.getOffsetByScale(new Vector2f(center.x - 286, center.y - 74)), new Vector4f(1,1,1,1), 0.415f, false, EnumParticlePositionType.Gui);
	}
	
	@Override
	public void init() {
		super.init();
		
		Vector2f scale = new Vector2f(0.6f, 0.6f);
		
		background.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x, center.y)));
		background.setScale(new Vector2f(600 * scale.x, 278 * scale.y));
		
		Button button = new Button(0, TextureRegister.guiButtonBase, new Vector2f(center.x, center.y+140 * scale.y),
				new Vector2f(300 * scale.x, 50 * scale.y), "gui.ok", new Vector2f(1f * scale.x, 0.9f * scale.y));
		buttons.add(button);
		
		text.setFontSize(Transformation.getScale(new Vector2f(1f * scale.x,0.9f * scale.y)));
		text.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x - 286*scale.x, center.y - 128*scale.y)));
		text.updateText(errorMessage);
		
		textDescription.setFontSize(Transformation.getScale(new Vector2f(1f * scale.x, 0.9f * scale.y)));
		textDescription.setPosition(Transformation.getOffsetByScale(new Vector2f(center.x - 286*scale.x, center.y - 74*scale.y)));
		textDescription.updateText(description);
	}
	
	@Override
	protected void onButtonLeftClick(Button b) {
		Core.getCore().setCurrentGui(prevGui);
	}
	
	@Override
	public void render(BaseShader shader) {
		OpenGLHelper.alphaGreater(0.01f);
		background.render(shader);
		super.render(shader);
	}
	
	@Override
	public void clear() {
		super.clear();
		text.clear();
		textDescription.clear();
	}
	
}
