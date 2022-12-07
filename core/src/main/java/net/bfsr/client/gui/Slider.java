package net.bfsr.client.gui;

import net.bfsr.client.font.GUIText;
import net.bfsr.client.gui.button.Button;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.loader.TextureLoader;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.texture.TextureRegister;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.EnumZoomFactor;
import net.bfsr.math.Transformation;
import net.bfsr.settings.EnumOption;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.text.DecimalFormat;

public class Slider extends TextureObject {

    private EnumOption option;
    private float value;
    private boolean isMoving;
    private final Button button;
    private String text;
    private AxisAlignedBoundingBox aabb;
    private boolean collided;
    private final float sideOut;
    private final DecimalFormat formatter = new DecimalFormat("0.00");

    public Slider(Vector2f pos, Vector2f scale, Vector4f color, EnumOption option) {
        super(TextureLoader.getTexture(TextureRegister.guiSlider), Transformation.getOffsetByScale(pos), new Vector2f(29 * Transformation.guiScale.x, 50 * Transformation.guiScale.y));
        Object optionValue = Core.getCore().getSettings().getOptionValue(option);
        float baseValue;
        if (option.getType() == int.class) baseValue = (int) optionValue;
        else baseValue = (float) optionValue;
        this.value = (baseValue - option.getMinValue()) / (option.getMaxValue() - option.getMinValue());
        this.option = option;
        this.text = "settings." + option.toString();
        this.button = new Button(0, TextureRegister.guiButtonBase, Transformation.getOffsetByScale(pos), new Vector2f(scale.x, scale.y), Lang.getString(text) + ": " + formatter.format(baseValue));
        this.sideOut = scale.x / 15.0f * Transformation.guiScale.x;

        float maxXpos = button.getPosition().x + button.getScale().x / 2.0f - this.getScale().x / 2.0f - sideOut;
        float minXpos = button.getPosition().x - button.getScale().x / 2.0f + this.getScale().x / 2.0f + sideOut;

        this.position.x = value * (maxXpos - minXpos) + minXpos;

        this.aabb = new AxisAlignedBoundingBox(new Vector2f(position.x + origin.x, position.y + origin.y), new Vector2f(position.x - origin.x, position.y - origin.y));
        setColor(color);
        setZoomFactor(EnumZoomFactor.Gui);
    }

    public void setOption(EnumOption option) {
        this.option = option;
    }

    @Override
    public void update() {
        super.update();

        if (isMoving) {
            this.position.x = Mouse.getPosition().x;

            float maxXpos = button.getPosition().x + button.getScale().x / 2.0f - this.getScale().x / 2.0f - sideOut;
            float minXpos = button.getPosition().x - button.getScale().x / 2.0f + this.getScale().x / 2.0f + sideOut;

            if (this.position.x > maxXpos) this.position.x = maxXpos;
            else if (this.position.x < minXpos) this.position.x = minXpos;

            this.value = (this.position.x - minXpos) / (maxXpos - minXpos);

            if (option != null) {
                float value;
                if (option.getType() == int.class) {
                    Core.getCore().getSettings().setOptionValue(option, this.value);
                    value = (int) Core.getCore().getSettings().getOptionValue(option);
                } else {
                    Core.getCore().getSettings().setOptionValue(option, this.value);
                    value = (float) Core.getCore().getSettings().getOptionValue(option);
                }
                button.setText(new GUIText(Lang.getString("settings." + option.toString()) + ": " + formatter.format(value), new Vector2f(0.8f * Transformation.guiScale.x, 1f * Transformation.guiScale.y),
                        button.getPosition(), new Vector4f(1, 1, 1, 1), true, EnumParticlePositionType.Gui));
            }

            this.aabb = new AxisAlignedBoundingBox(new Vector2f(position.x + origin.x, position.y + origin.y), new Vector2f(position.x - origin.x, position.y - origin.y));
        }

        if (button.isIntersects()) {
            if (!collided) {
                collided = true;
                Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonCollide));
            }
        } else {
            collided = false;
        }
    }

    @Override
    public void render(BaseShader shader) {
        button.render(shader);
        super.render(shader);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        button.setPosition(x, y);
        aabb.setMinX(x + origin.x);
        aabb.setMaxX(x - origin.x);
        aabb.setMinY(y + origin.y);
        aabb.setMaxY(y - origin.y);

        float maxXpos = button.getPosition().x + button.getScale().x / 2.0f - this.getScale().x / 2.0f - sideOut;
        float minXpos = button.getPosition().x - button.getScale().x / 2.0f + this.getScale().x / 2.0f + sideOut;

        this.position.x = value * (maxXpos - minXpos) + minXpos;
    }

    @Override
    public void setPosition(Vector2f position) {
        this.setPosition(position.x, position.y);
    }

    public void clear() {
        button.clear();
    }

    public boolean isIntersects() {
        return aabb.isIntersects(Mouse.getPosition());
    }

    public void setMoving(boolean moving) {
        this.isMoving = moving;

        if (moving) {
            Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
        }
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setTextColor(int r, int g, int b, int a) {
        button.setTextColor(r, g, b, a);
    }

    public Vector2f getPositionOnScreen() {
        return position;
    }

    public void onMouseLeftClicked() {
        if (isIntersects()) {
            setMoving(true);
        }
    }

    public void onMouseLeftRelease() {
        setMoving(false);
    }
}
