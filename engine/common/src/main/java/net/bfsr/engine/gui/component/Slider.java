package net.bfsr.engine.gui.component;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.font.string.StringOffsetType;
import org.joml.Vector4f;

public class Slider extends Rectangle {
    private final AbstractMouse mouse = Engine.getMouse();

    protected float value;
    private boolean movingByMouse;
    private final int indent;
    protected final Label label;
    @Getter
    private final Rectangle movingValue;

    public Slider(int width, int height, int fontSize, float value, String string) {
        super(width, height);
        this.movingValue = new Rectangle(29, 50);
        this.value = value;
        this.indent = 28;

        add(movingValue.atBottomLeft(this::calculateSliderXPos, () -> 0));
        setHoverColor(0.5f, 1.0f, 1.0f, 1.0f);

        this.label = new Label(Engine.getFontManager().getDefaultFont(), string, fontSize, StringOffsetType.CENTERED);
        add(label.atBottomLeft(width / 2, label.getCenteredOffsetY(height)));
    }

    @Override
    public void update(int mouseX, int mouseY) {
        super.update(mouseX, mouseY);

        if (movingByMouse) {
            int sliderX = (int) mouse.getScreenPosition().x - getSceneX() - movingValue.getWidth() / 2;

            int maxXPos = getMaxX();
            int minXPos = getMinX();

            if (sliderX > maxXPos) sliderX = maxXPos;
            else if (sliderX < minXPos) sliderX = minXPos;

            movingValue.setX(sliderX);

            float prevValue = value;
            value = (sliderX - minXPos) / (float) (maxXPos - minXPos);

            if (prevValue != value) {
                onValueChanged();
            }
        }
    }

    protected void onValueChanged() {}

    private int calculateSliderXPos() {
        int maxXPos = getMaxX();
        int minXPos = getMinX();
        return (int) (value * (maxXPos - minXPos) + minXPos);
    }

    private int getMaxX() {
        return width - indent - movingValue.getWidth();
    }

    private int getMinX() {
        return indent;
    }

    @Override
    public boolean isMouseHover() {
        return super.isMouseHover() || movingValue.isMouseHover();
    }

    @Override
    public GuiObject mouseLeftClick(int mouseX, int mouseY) {
        GuiObject guiObject = super.mouseLeftClick(mouseX, mouseY);

        if (isMouseHover()) {
            movingByMouse = true;
            Engine.getGuiManager().setActiveGuiObject(movingValue);
        }

        return guiObject;
    }

    @Override
    public GuiObject mouseLeftRelease(int mouseX, int mouseY) {
        boolean wasMovingByMouse = movingByMouse;

        movingByMouse = false;

        if (wasMovingByMouse) {
            Engine.getGuiManager().setActiveGuiObject(null);
        }

        GuiObject childGuiObject = super.mouseLeftRelease(mouseX, mouseY);
        if (childGuiObject != null) {
            return childGuiObject;
        }

        return wasMovingByMouse ? movingValue : null;
    }

    @Override
    public Slider setHoverColor(float r, float g, float b, float a) {
        movingValue.setHoverColor(r, g, b, a);
        super.setHoverColor(r, g, b, a);
        return this;
    }

    @Override
    public Slider setHoverColor(Vector4f color) {
        movingValue.setHoverColor(color.x, color.y, color.z, color.w);
        super.setHoverColor(color);
        return this;
    }

    @Override
    public void remove() {
        super.remove();

        if (movingByMouse) {
            Engine.getGuiManager().setActiveGuiObject(null);
        }
    }
}