package net.bfsr.client.gui;

import net.bfsr.client.core.Core;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.renderer.font.FontType;
import net.bfsr.client.renderer.font.StringCache;
import net.bfsr.client.renderer.font.StringOffsetType;
import net.bfsr.client.renderer.font.string.StringObject;
import net.bfsr.client.settings.Option;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.texture.TextureRegister;
import net.bfsr.util.DecimalUtils;

public class Slider extends TexturedGuiObject {
    private final Option option;
    private float value;
    private boolean movingByMouse;
    private final int indent;
    private final StringObject stringObject;
    private final SimpleGuiObject slider;

    Slider(int x, int y, int width, int height, Option option) {
        this(x, y, width, height, option, 20);
    }

    private Slider(int x, int y, int width, int height, Option option, int fontSize) {
        super(TextureRegister.guiButtonBase, x, y, width, height);
        float baseValue = option.getFloat();
        this.value = (baseValue - option.getMinValue()) / (option.getMaxValue() - option.getMinValue());
        this.option = option;
        this.indent = 28;

        String string = Lang.getString("settings." + option.getOptionName()) + ": " + DecimalUtils.strictFormatWithToDigits(baseValue);
        FontType font = FontType.XOLONIUM;
        StringCache stringCache = font.getStringCache();
        stringObject = new StringObject(font, string, 0, 0, fontSize, StringOffsetType.CENTERED);
        stringObject.setPosition(x + width / 2, (int) (y + (height - stringCache.getHeight(string, fontSize)) / 2.0f + stringCache.getAscent(string, fontSize)));
        stringObject.compile();

        slider = new TexturedGuiObject(TextureRegister.guiSlider, x, y, 29, 50);
        slider.setX(calculateSliderXPos());
    }

    @Override
    public void onMouseHover() {
        Core.get().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonCollide));
    }

    @Override
    public void update() {
        super.update();
        slider.update();
        stringObject.update();

        if (movingByMouse) {
            int sliderX = (int) Mouse.getPosition().x - slider.width / 2;

            int maxXPos = getMaxX();
            int minXPos = getMinX();

            if (sliderX > maxXPos) sliderX = maxXPos;
            else if (sliderX < minXPos) sliderX = minXPos;

            slider.setX(sliderX);

            value = (sliderX - minXPos) / (float) (maxXPos - minXPos);

            option.changeValue(value);

            stringObject.setString(Lang.getString("settings." + option.getOptionName()) + ": " + DecimalUtils.strictFormatWithToDigits(option.getFloat()));
        }
    }

    @Override
    public void render() {
        super.render();
        slider.render();
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
    public boolean onMouseLeftClick() {
        if (!isMouseHover()) return false;
        movingByMouse = true;
        Core.get().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
        return true;
    }

    @Override
    public void onMouseLeftRelease() {
        movingByMouse = false;
    }
}