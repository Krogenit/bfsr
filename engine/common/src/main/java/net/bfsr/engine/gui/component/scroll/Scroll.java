package net.bfsr.engine.gui.component.scroll;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.input.AbstractMouse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Scroll extends Rectangle {
    private int scroll;
    private int clickStartScroll;
    private int mouseStartClickY;
    @Getter
    private int totalHeight;
    private int viewHeight;
    @Getter
    private boolean movingByMouse;
    private int scrollHeight;
    private int scrollY;
    private final List<ScrollableGuiObject> scrollableElements = new ArrayList<>();
    private BiFunction<Integer, Integer, Integer> viewHeightResizeFunction = (width, height) -> viewHeight;
    @Setter
    private int scrollAmount = 40;
    private int accumulator;
    private int minObjectY, maxObjectY = Integer.MIN_VALUE;
    private final AbstractMouse mouse = Engine.mouse;

    public Scroll(int width, int height) {
        super(width, height);
    }

    public void addScrollable(GuiObject guiObject) {
        scrollableElements.add(new ScrollableGuiObject(guiObject));
        minObjectY = Math.min(guiObject.getY(), minObjectY);
        maxObjectY = Math.max(guiObject.getY() + guiObject.getHeight(), maxObjectY);
        totalHeight = maxObjectY - minObjectY;
        updateScrollPositionAndSize();
    }

    public void removeScrollable(GuiObject guiObject) {
        if (scrollableElements.remove(new ScrollableGuiObject(guiObject))) {
            updatePositionAndSize();
        }
    }

    @Override
    public void update() {
        super.update();

        if (accumulator != 0) {
            updateScroll(scroll + accumulator);
            accumulator = 0;
        }

        if (movingByMouse) {
            updateScroll((int) (clickStartScroll + (mouse.getPosition().y - mouseStartClickY) / (scrollHeight / (float) totalHeight)));
        }
    }

    private void updateScroll(int newValue) {
        int heightDiff = totalHeight - viewHeight;
        if (heightDiff < 0) heightDiff = 0;

        scroll = newValue;

        if (scroll < 0) scroll = 0;
        else if (scroll > heightDiff) scroll = heightDiff;

        updateScrollableElementsPosition();
        updateScrollPositionAndSize();
    }

    private void updateScrollableElementsPosition() {
        for (int i = 0; i < scrollableElements.size(); i++) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(i);
            GuiObject guiObject = scrollableGuiObject.getGuiObject();
            guiObject.setY(scrollableGuiObject.getY() - scroll);
        }
    }

    private void updateScrollPositionAndSize() {
        float scrollValue = viewHeight / (float) totalHeight;
        float scrollYValue = scrollHeight / (float) totalHeight;
        if (scrollValue > 1) scrollValue = 1.0f;
        if (scrollYValue > 1) scrollYValue = 1.0f;
        int height = (int) (scrollHeight * scrollValue);

        super.setPosition(x, (int) (scrollY + scroll * scrollYValue));
        super.setHeight(height);
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        viewHeight = viewHeightResizeFunction.apply(width, height);
        updateTotalHeight();
        updateScrollableObjectsY();
        updateScroll(scroll);
        updateScrollableLastValues();
    }

    private void updateTotalHeight() {
        if (scrollableElements.size() > 0) {
            GuiObject guiObject = scrollableElements.get(0).getGuiObject();
            minObjectY = guiObject.getY();
            maxObjectY = guiObject.getY() + guiObject.getHeight();
            for (int i = 0; i < scrollableElements.size(); i++) {
                guiObject = scrollableElements.get(i).getGuiObject();
                minObjectY = Math.min(guiObject.getY(), minObjectY);
                maxObjectY = Math.max(guiObject.getY() + guiObject.getHeight(), maxObjectY);
            }
            totalHeight = maxObjectY - minObjectY;
        } else {
            totalHeight = 0;
        }
    }

    private void updateScrollableObjectsY() {
        for (int i = 0; i < scrollableElements.size(); i++) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(i);
            GuiObject guiObject = scrollableGuiObject.getGuiObject();
            guiObject.updatePositionAndSize();
            scrollableGuiObject.updateY();
        }
    }

    private void updateScrollableLastValues() {
        updateLastValues();
        for (int i = 0; i < scrollableElements.size(); i++) {
            scrollableElements.get(i).getGuiObject().update();
        }
    }

    @Override
    public GuiObject mouseLeftClick() {
        if (!isMouseHover()) return null;

        movingByMouse = true;
        mouseStartClickY = (int) mouse.getPosition().y;
        clickStartScroll = scroll;

        return this;
    }

    @Override
    public GuiObject mouseLeftRelease() {
        GuiObject guiObject = super.mouseLeftRelease();

        movingByMouse = false;

        return guiObject;
    }

    @Override
    public boolean mouseScroll(float y) {
        if (!parent.isIntersectsWithMouse()) return false;
        accumulator -= (int) (y * scrollAmount);
        return true;
    }

    public void scrollBottom() {
        updateScroll(Integer.MAX_VALUE);
        updatePositionAndSize();
    }

    @Override
    public Scroll setPosition(int x, int y) {
        this.x = x;
        this.scrollY = y;
        return this;
    }

    @Override
    public Scroll setHeight(int height) {
        this.scrollHeight = height;
        return this;
    }

    public Scroll setViewHeightResizeFunction(BiFunction<Integer, Integer, Integer> viewHeightResizeFunction) {
        this.viewHeightResizeFunction = viewHeightResizeFunction;
        return this;
    }

    @Override
    public void clear() {
        super.clear();
        scrollableElements.clear();
    }
}