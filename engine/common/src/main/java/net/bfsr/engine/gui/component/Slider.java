package net.bfsr.engine.gui.component;

import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.StringOffsetType;
import net.bfsr.engine.renderer.texture.TextureRegister;
import org.joml.Vector4f;

public class Slider extends TexturedRectangle {
    protected float value;
    private boolean movingByMouse;
    private final int indent;
    protected final Label label;
    private final TexturedRectangle slider = new TexturedRectangle(TextureRegister.guiSlider, 0, 0, 29, 50);

    public Slider(int x, int y, int width, int height, int fontSize, float value, String string) {
        super(TextureRegister.guiButtonBase, x, y, width, height);
        this.value = value;
        this.indent = 28;

        add(slider);
        slider.setX(calculateSliderXPos());
        setHoverColor(0.5f, 1.0f, 1.0f, 1.0f);

        Font font = Font.XOLONIUM;
        this.label = new Label(font, string, fontSize, StringOffsetType.CENTERED);
        add(label.atTopLeft(width / 2, label.getCenteredOffsetY(height)));
    }

    @Override
    public void update() {
        super.update();

        if (movingByMouse) {
            int sliderX = (int) Engine.mouse.getPosition().x - getSceneX() - slider.getWidth() / 2;

            int maxXPos = getMaxX();
            int minXPos = getMinX();

            if (sliderX > maxXPos) sliderX = maxXPos;
            else if (sliderX < minXPos) sliderX = minXPos;

            slider.setX(sliderX);

            value = (sliderX - minXPos) / (float) (maxXPos - minXPos);

            onValueChanged();
        }
    }

    protected void onValueChanged() {}

    private int calculateSliderXPos() {
        int maxXPos = getMaxX();
        int minXPos = getMinX();
        return (int) (value * (maxXPos - minXPos) + minXPos);
    }

    private int getMaxX() {
        return width - indent - slider.getWidth();
    }

    private int getMinX() {
        return indent;
    }

    @Override
    public boolean isMouseHover() {
        return super.isMouseHover() || slider.isMouseHover();
    }

    @Override
    public GuiObject mouseLeftClick() {
        GuiObject guiObject = super.mouseLeftClick();

        if (isMouseHover()) {
            movingByMouse = true;
        }

        return guiObject;
    }

    @Override
    public GuiObject mouseLeftRelease() {
        movingByMouse = false;
        return super.mouseLeftRelease();
    }

    @Override
    public Slider setHoverColor(float r, float g, float b, float a) {
        slider.setHoverColor(r, g, b, a);
        super.setHoverColor(r, g, b, a);
        return this;
    }

    @Override
    public Slider setHoverColor(Vector4f color) {
        slider.setHoverColor(color.x, color.y, color.z, color.w);
        super.setHoverColor(color);
        return this;
    }
}