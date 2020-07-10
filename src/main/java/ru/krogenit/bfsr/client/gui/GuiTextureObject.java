package ru.krogenit.bfsr.client.gui;

import org.joml.Vector2f;

import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.texture.Texture;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.entity.TextureObject;
import ru.krogenit.bfsr.math.EnumZoomFactor;
import ru.krogenit.bfsr.math.Transformation;

public class GuiTextureObject extends TextureObject {
	
	public GuiTextureObject(TextureRegister tex) {
		super(TextureLoader.getTexture(tex), new Vector2f());
		setZoomFactor(EnumZoomFactor.Gui);
	}

	public GuiTextureObject(Texture tex) {
		super(tex, new Vector2f(), new Vector2f(tex.getWidth(), tex.getHeight()));
		setZoomFactor(EnumZoomFactor.Gui);
	}

	public GuiTextureObject(Texture tex, Vector2f pos) {
		super(tex, pos, new Vector2f(tex.getWidth(), tex.getHeight()));
		setZoomFactor(EnumZoomFactor.Gui);
	}
	
	@Override
	public void setPosition(float x, float y) {
		this.setPosition(new Vector2f(x, y));
	}
	
	@Override
	public void setPosition(Vector2f position) {
		this.position = Transformation.getOffsetByScale(position);
	}
	
	@Override
	public void setScale(float x, float y) {
		this.scale.x = Transformation.guiScale.x * x;
		this.scale.y = Transformation.guiScale.y * y;
	}
	
	@Override
	public void setScale(Vector2f scale) {
		this.scale = new Vector2f(Transformation.guiScale.x * scale.x, Transformation.guiScale.y * scale.y);
	}
}
