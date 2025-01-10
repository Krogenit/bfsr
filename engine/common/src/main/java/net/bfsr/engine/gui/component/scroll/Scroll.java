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
    @Getter
    private int scroll;
    private int clickStartScroll;
    private int mouseStartClickY;
    @Getter
    private int totalHeight;
    private int viewHeight;
    private int firstElementGapHeight;
    @Getter
    private boolean movingByMouse;
    private int scrollHeight;
    private int scrollY;
    private final List<ScrollableGuiObject> scrollableElements = new ArrayList<>();
    private BiFunction<Integer, Integer, Integer> viewHeightResizeFunction = (width, height) -> viewHeight;
    @Setter
    private int scrollAmount = 40;
    private int accumulator;
    private int minObjectY = Integer.MAX_VALUE, maxObjectY = Integer.MIN_VALUE;
    private final AbstractMouse mouse = Engine.mouse;

    public Scroll(int width, int height) {
        super(width, height);
    }

    public void addScrollable(GuiObject guiObject) {
        scrollableElements.add(new ScrollableGuiObject(guiObject));
        minObjectY = Math.min(guiObject.getY(), minObjectY);
        maxObjectY = Math.max(guiObject.getY() + guiObject.getHeight(), maxObjectY);
        totalHeight = maxObjectY - minObjectY;
        updateFirstElementGapHeight(minObjectY);
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
            updateScroll((int) (clickStartScroll - (mouse.getGuiPosition().y - mouseStartClickY) /
                    (scrollHeight / (float) (totalHeight + firstElementGapHeight))));
        }
    }

    private void updateScroll(int newValue) {
        int heightDiff = totalHeight - viewHeight + firstElementGapHeight;
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
            guiObject.setY(scrollableGuiObject.getY() + scroll);
        }
    }

    public void updateScrollable(GuiObject guiObject) {
        for (int i = 0; i < scrollableElements.size(); i++) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(i);
            GuiObject guiObject1 = scrollableGuiObject.getGuiObject();
            if (guiObject == guiObject1) {
                guiObject.applyPositionFunctions((x, y) -> scrollableGuiObject.setY(y));
                updateTotalHeight();
                updateScroll(scroll);
                return;
            }
        }
    }

    private void updateScrollPositionAndSize() {
        float scrollValue = viewHeight / (float) (totalHeight + firstElementGapHeight);
        float scrollYValue = scrollHeight / (float) (totalHeight + firstElementGapHeight);
        if (scrollValue > 1) scrollValue = 1.0f;
        if (scrollYValue > 1) scrollYValue = 1.0f;
        int height = (int) (scrollHeight * scrollValue);

        super.setPosition(x, (int) (viewHeight - height + scrollY - (scroll * scrollYValue)));
        super.setHeight(height);
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        viewHeight = viewHeightResizeFunction.apply(width, height);
    }

    private void updateTotalHeight() {
        if (scrollableElements.size() > 0) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(0);
            GuiObject guiObject = scrollableGuiObject.getGuiObject();
            minObjectY = scrollableGuiObject.getY();
            maxObjectY = scrollableGuiObject.getY() + guiObject.getHeight();
            for (int i = 0; i < scrollableElements.size(); i++) {
                scrollableGuiObject = scrollableElements.get(i);
                guiObject = scrollableGuiObject.getGuiObject();
                minObjectY = Math.min(scrollableGuiObject.getY(), minObjectY);
                maxObjectY = Math.max(scrollableGuiObject.getY() + guiObject.getHeight(), maxObjectY);
            }

            totalHeight = maxObjectY - minObjectY;
            updateFirstElementGapHeight(minObjectY);
        } else {
            totalHeight = 0;
        }
    }

    private void updateFirstElementGapHeight(int minObjectY) {
        // Fixes not enough scroll ability to last objects
        if (minObjectY < 0) {
            firstElementGapHeight = Math.max(0, viewHeight - (totalHeight + minObjectY));
        }
    }

    private void updateScrollableLastValues() {
        updateLastValues();
        for (int i = 0; i < scrollableElements.size(); i++) {
            scrollableElements.get(i).getGuiObject().updateLastValues();
        }
    }

    @Override
    public GuiObject mouseLeftClick() {
        if (!isMouseHover()) return null;

        movingByMouse = true;
        mouseStartClickY = (int) (mouse.getGuiPosition().y);
        clickStartScroll = scroll;

        return this;
    }

    @Override
    public GuiObject mouseLeftRelease() {
        GuiObject guiObject = super.mouseLeftRelease();

        if (movingByMouse) {
            movingByMouse = false;

            if (guiObject == null) {
                return this;
            }
        }

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
        updateScrollableLastValues();
    }

    @Override
    public Scroll setPosition(int x, int y) {
        setX(x);
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
        minObjectY = Integer.MAX_VALUE;
        maxObjectY = Integer.MIN_VALUE;
    }
}