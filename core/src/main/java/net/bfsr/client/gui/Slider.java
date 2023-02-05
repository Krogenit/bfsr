package net.bfsr.client.gui;

import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.StringCache;
import net.bfsr.client.render.font.StringOffsetType;
import net.bfsr.client.render.font.string.DynamicString;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.core.Core;
import net.bfsr.settings.EnumOption;
import net.bfsr.util.DecimalUtils;

public class Slider extends TexturedGuiObject {
    private final EnumOption option;
    private float value;
    private boolean movingByMouse;
    private boolean collided;
    private final int indent;
    private final StringObject stringObject;
    private final SimpleGuiObject slider;

    Slider(int x, int y, int width, int height, EnumOption option) {
        this(x, y, width, height, option, 20);
    }

    private Slider(int x, int y, int width, int height, EnumOption option, int fontSize) {
        super(TextureRegister.guiButtonBase, x, y, width, height);
        float baseValue = option.getFloat();
        this.value = (baseValue - option.getMinValue()) / (option.getMaxValue() - option.getMinValue());
        this.option = option;
        this.indent = 28;

        String string = Lang.getString("settings." + option.getOptionName()) + ": " + DecimalUtils.formatWithToDigits(baseValue);
        FontType font = FontType.XOLONIUM;
        StringCache stringCache = font.getStringCache();
        stringObject = new DynamicString(font, string, 0, 0, fontSize, StringOffsetType.CENTERED);
        stringObject.setPosition(x + width / 2, (int) (y + (height - stringCache.getHeight(string, fontSize)) / 2.0f + stringCache.getAscent(string, fontSize)));
        stringObject.compile();

        slider = new TexturedGuiObject(TextureRegister.guiSlider, x, y, 29, 50);
        slider.setX(calculateSliderXPos());
    }

    @Override
    public void update() {
        if (movingByMouse) {
            int sliderX = (int) Mouse.getPosition().x - slider.width / 2;

            int maxXPos = getMaxX();
            int minXPos = getMinX();

            if (sliderX > maxXPos) sliderX = maxXPos;
            else if (sliderX < minXPos) sliderX = minXPos;

            slider.setX(sliderX);

            value = (sliderX - minXPos) / (float) (maxXPos - minXPos);

            option.changeValue(value);

            stringObject.update(Lang.getString("settings." + option.getOptionName()) + ": " + DecimalUtils.formatWithToDigits(option.getFloat()));
        }

        if (isIntersects()) {
            if (!collided) {
                collided = true;
                Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonCollide));
            }
        } else {
            collided = false;
        }
    }

    @Override
    public void render() {
        super.render();
        slider.render();
        InstancedRenderer.INSTANCE.render();
        stringObject.render();
    }

    @Override
    public Slider setPosition(int x, int y) {
        super.setPosition(x, y);
        slider.setPosition(calculateSliderXPos(), y);
        StringCache stringCache = stringObject.getStringCache();
        stringObject.setPosition(x + width / 2, (int) (y + (height - stringCache.getHeight(stringObject.getString(), stringObject.getFontSize())) / 2.0f +
                stringCache.getAscent(stringObject.getString(), stringObject.getFontSize())));
        return this;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        slider.setX(calculateSliderXPos());
        stringObject.setX(x + width / 2);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        slider.setY(y);
        StringCache stringCache = stringObject.getStringCache();
        stringObject.setY((int) (y + (height - stringCache.getHeight(stringObject.getString(), stringObject.getFontSize())) / 2.0f +
                stringCache.getAscent(stringObject.getString(), stringObject.getFontSize())));
    }

    private int calculateSliderXPos() {
        int maxXPos = getMaxX();
        int minXPos = getMinX();
        return (int) (value * (maxXPos - minXPos) + minXPos);
    }

    private int getMaxX() {
        return x + width - indent - slider.width;
    }

    private int getMinX() {
        return x + indent;
    }

    @Override
    public void clear() {
        stringObject.clear();
    }

    @Override
    public void onMouseLeftClick() {
        if (isIntersects()) {
            movingByMouse = true;
            Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
        }
    }

    @Override
    public void onMouseLeftRelease() {
        movingByMouse = false;
    }
}
