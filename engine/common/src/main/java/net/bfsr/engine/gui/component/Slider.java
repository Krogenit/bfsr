package net.bfsr.engine.gui.component;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.object.SimpleGuiObject;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringCache;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class Slider extends TexturedGuiObject {
    protected float value;
    private boolean movingByMouse;
    private final int indent;
    protected final StringObject stringObject;
    private final SimpleGuiObject slider;

    public Slider(int x, int y, int width, int height, float value, String string) {
        this(x, y, width, height, 20, value, string);
    }

    public Slider(int x, int y, int width, int height, int fontSize, float value, String string) {
        super(TextureRegister.guiButtonBase, x, y, width, height);
        this.value = value;
        this.indent = 28;

        FontType font = FontType.XOLONIUM;
        StringCache stringCache = font.getStringCache();
        stringObject = new StringObject(font, string, 0, 0, fontSize, StringOffsetType.CENTERED);
        stringObject.setPosition(x + width / 2,
                (int) (y + (height - stringCache.getHeight(string, fontSize)) / 2.0f + stringCache.getAscent(string, fontSize)));
        stringObject.compile();

        slider = new TexturedGuiObject(TextureRegister.guiSlider, x, y, 29, 50);
        slider.setX(calculateSliderXPos());
        setHoverColor(0.5f, 1.0f, 1.0f, 1.0f);
        slider.setHoverColor(0.5f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void update() {
        super.update();
        slider.update();
        stringObject.update();

        if (movingByMouse) {
            int sliderX = (int) Engine.mouse.getPosition().x - slider.getWidth() / 2;

            int maxXPos = getMaxX();
            int minXPos = getMinX();

            if (sliderX > maxXPos) sliderX = maxXPos;
            else if (sliderX < minXPos) sliderX = minXPos;

            slider.setX(sliderX);

            value = (sliderX - minXPos) / (float) (maxXPos - minXPos);

            onValueChanged();
        }
    }

    @Override
    public void updateMouseHover() {
        super.updateMouseHover();
        slider.updateMouseHover();
    }

    protected void onValueChanged() {}

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
        stringObject.setPosition(x + width / 2, (int) (y + (height - stringCache.getHeight(stringObject.getString(),
                stringObject.getFontSize())) / 2.0f + stringCache.getAscent(stringObject.getString(),
                stringObject.getFontSize())));
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
        stringObject.setY((int) (y + (height - stringCache.getHeight(stringObject.getString(), stringObject.getFontSize())) / 2.0f
                + stringCache.getAscent(stringObject.getString(), stringObject.getFontSize())));
    }

    private int calculateSliderXPos() {
        int maxXPos = getMaxX();
        int minXPos = getMinX();
        return (int) (value * (maxXPos - minXPos) + minXPos);
    }

    private int getMaxX() {
        return x + width - indent - slider.getWidth();
    }

    private int getMinX() {
        return x + indent;
    }

    @Override
    public boolean onMouseLeftClick() {
        if (!isMouseHover()) return false;
        movingByMouse = true;
        onLeftClickSupplier.get();
        return true;
    }

    @Override
    public boolean onMouseLeftRelease() {
        movingByMouse = false;
        return false;
    }
}