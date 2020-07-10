package ru.krogenit.bfsr.client.gui.button;

import org.joml.Vector2f;
import org.joml.Vector4f;

import ru.krogenit.bfsr.client.font.GUIText;
import ru.krogenit.bfsr.client.gui.GuiSettings;
import ru.krogenit.bfsr.client.input.Mouse;
import ru.krogenit.bfsr.client.language.Lang;
import ru.krogenit.bfsr.client.loader.TextureLoader;
import ru.krogenit.bfsr.client.particle.EnumParticlePositionType;
import ru.krogenit.bfsr.client.shader.BaseShader;
import ru.krogenit.bfsr.client.sound.GuiSoundSource;
import ru.krogenit.bfsr.client.sound.SoundRegistry;
import ru.krogenit.bfsr.client.texture.TextureRegister;
import ru.krogenit.bfsr.collision.AxisAlignedBoundingBox;
import ru.krogenit.bfsr.core.Core;
import ru.krogenit.bfsr.entity.TextureObject;
import ru.krogenit.bfsr.math.EnumZoomFactor;
import ru.krogenit.bfsr.math.Transformation;
import ru.krogenit.bfsr.settings.ClientSettings;
import ru.krogenit.bfsr.settings.EnumOption;

public class Button extends TextureObject {
	private final AxisAlignedBoundingBox boundingBox;
	private GUIText text;
	private final int id;
	private SoundRegistry collideSound, clickSound;
	private boolean collided;
	private EnumOption option;
	
	public Button(int id, TextureRegister texture, Vector2f pos, Vector2f scale, String text, Vector2f fontSize) {
		super(TextureLoader.getTexture(texture), pos, new Vector2f(scale.x * Transformation.guiScale.x, scale.y * Transformation.guiScale.y));
		this.boundingBox = new AxisAlignedBoundingBox(new Vector2f(pos.x + origin.x, pos.y + origin.y), new Vector2f(pos.x - origin.x, pos.y - origin.y));
		this.id = id;
		if(text != null && text.length() > 0) this.text = new GUIText(Lang.getString(text), new Vector2f(fontSize.x*Transformation.guiScale.x,fontSize.y*Transformation.guiScale.y), new Vector2f(pos.x, pos.y), new Vector4f(1,1,1,1), true, EnumParticlePositionType.Gui);
		setZoomFactor(EnumZoomFactor.Gui);
		setClickSound(SoundRegistry.buttonClick);
		setCollideSound(SoundRegistry.buttonCollide);
	}
	
	public Button(int id, TextureRegister texture, Vector2f pos, Vector2f scale, String text) {
		this(id, texture, pos, scale, text, new Vector2f(0.8f, 1f));
	}
	
	public Button(int id, TextureRegister texture, Vector2f pos, Vector2f scale) {
		this(id, texture, pos, scale, "");
	}
	
	public Button(int id, TextureRegister texture, Vector2f pos, Vector2f scale, EnumOption option) {
		super(TextureLoader.getTexture(texture), pos, new Vector2f(scale.x * Transformation.guiScale.x, scale.y * Transformation.guiScale.y));
		this.text = new GUIText(Lang.getString("settings." + option.toString()) + ": " + Core.getCore().getSettings().getOptionValue(option), new Vector2f(0.8f*Transformation.guiScale.x,1f*Transformation.guiScale.y), new Vector2f(pos.x, pos.y), new Vector4f(1,1,1,1), true, EnumParticlePositionType.Gui);
		this.boundingBox = new AxisAlignedBoundingBox(new Vector2f(pos.x + origin.x, pos.y + origin.y), new Vector2f(pos.x - origin.x, pos.y - origin.y));
		this.id = id;
		this.option = option;
		setZoomFactor(EnumZoomFactor.Gui);
		setClickSound(SoundRegistry.buttonClick);
		setCollideSound(SoundRegistry.buttonCollide);
	}

	@Override
	public void update(double delta) {
		super.update(delta);
		
		if(collideSound != null) {
			if(boundingBox.isIntersects(Mouse.getPosition())) {
				if(!collided) {
					collided = true;
					Core.getCore().getSoundManager().play(new GuiSoundSource(collideSound));
				}
			} else {
				collided = false;
			}
		}
	}

	public void leftClick() {
		if(clickSound != null) {
			Core.getCore().getSoundManager().play(new GuiSoundSource(clickSound));
		}
		
		if(option != null) {
			ClientSettings settings = Core.getCore().getSettings();
			if(option.getType() == boolean.class) {
				boolean prevValue = (boolean) settings.getOptionValue(option);
				boolean newValue = !prevValue;
				settings.setOptionValue(option, newValue);
				text.updateText(Lang.getString("settings." + option.toString()) + ": " + settings.getOptionValue(option));
			} else {
				settings.setOptionValue(option, null);//change lang
				if(Core.getCore().getCurrentGui()  != null && Core.getCore().getCurrentGui() instanceof GuiSettings) {
					Core.getCore().getCurrentGui().clear();
					Core.getCore().getCurrentGui().init();
				}
			}

		}
	}

	public void rightClick() {
		if(clickSound != null) {
			Core.getCore().getSoundManager().play(new GuiSoundSource(clickSound));
		}
	}

	@Override
	public void render(BaseShader shader) {
		super.render(shader);
	}

	public boolean isIntersects() {
		return boundingBox.isIntersects(Mouse.getPosition());
	}

	public int getId() {
		return id;
	}

	public void clear() {
		if(text != null) text.clear();
	}
	
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		if(text != null) text.setPosition(x, y);
		boundingBox.setMinX(x + origin.x);
		boundingBox.setMaxX(x - origin.x);
		boundingBox.setMinY(y + origin.y);
		boundingBox.setMaxY(y - origin.y);
	}
	
	@Override
	public void setPosition(Vector2f position) {
		this.setPosition(position.x, position.y);
	}
	
	public Button setCollideSound(SoundRegistry collideSound) {
		this.collideSound = collideSound;
		return this;
	}
	
	public Button setClickSound(SoundRegistry clickSound) {
		this.clickSound = clickSound;
		return this;
	}
	
	public void setTextColor(float r, float g, float b, float a) {
		this.text.setColor(r, g, b, a);
	}
	
	public void setText(GUIText text) {
		if(this.text != null) this.text.clear();
		this.text = text;
	}
}
