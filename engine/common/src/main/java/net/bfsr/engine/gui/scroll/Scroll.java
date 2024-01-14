package net.bfsr.engine.gui.scroll;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.object.GuiObject;
import net.bfsr.engine.gui.object.SimpleGuiObject;
import net.bfsr.engine.input.AbstractMouse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Scroll extends SimpleGuiObject {
    @Getter
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

    public void registerGuiObject(GuiObject guiObject) {
        ScrollableGuiObject scrollableGuiObject = new ScrollableGuiObject(guiObject);
        scrollableElements.add(scrollableGuiObject);
        guiObject.setY(scrollableGuiObject.getY() - scroll);
        minObjectY = Math.min(guiObject.getY(), minObjectY);
        maxObjectY = Math.max(guiObject.getY() + guiObject.getHeight(), maxObjectY);
        totalHeight = maxObjectY - minObjectY;
        updateScrollPositionAndSize();
    }

    public void unregisterGuiObject(GuiObject guiObject) {
        if (scrollableElements.remove(new ScrollableGuiObject(guiObject))) {
            updateTotalHeight();
            updateScrollPositionAndSize();
        }
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

    @Override
    public boolean onMouseScroll(float y) {
        accumulator -= y * scrollAmount;
        return false;
    }

    public void scrollBottom() {
        updateScroll(Integer.MAX_VALUE);
    }

    @Override
    public void update() {
        super.update();

        if (accumulator != 0) {
            updateScroll(scroll + accumulator);
            accumulator = 0;
        }

        if (movingByMouse) {
            updateScroll((int) (clickStartScroll + (mouse.getPosition().y - mouseStartClickY) /
                    (scrollHeight / (float) totalHeight)));
        }
    }

    private void updateScroll(int newValue) {
        int heightDiff = totalHeight - viewHeight;
        if (heightDiff < 0) heightDiff = 0;

        scroll = newValue;

        if (scroll < 0) scroll = 0;
        else if (scroll > heightDiff) scroll = heightDiff;

        for (int i = 0; i < scrollableElements.size(); i++) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(i);
            Scrollable scrollable = scrollableGuiObject.getGuiObject();
            scrollable.setY(scrollableGuiObject.getY() - scroll);
        }

        updateScrollPositionAndSize();
    }

    private void updateScrollPositionAndSize() {
        float scrollValue = viewHeight / (float) totalHeight;
        float scrollYValue = scrollHeight / (float) totalHeight;
        if (scrollValue > 1) scrollValue = 1.0f;
        if (scrollYValue > 1) scrollYValue = 1.0f;
        int height = (int) (scrollHeight * scrollValue);

        super.setPosition(x, (int) (scrollY + scroll * scrollYValue));
        setSize(width, height);
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        updateScrollableObjectsY();
        super.updatePositionAndSize(width, height);
        viewHeight = viewHeightResizeFunction.apply(width, height);
        updateTotalHeight();
        updateScrollPositionAndSize();
        updateLastPosition();
    }

    @Override
    public boolean onMouseLeftClick() {
        if (!isMouseHover()) return false;

        movingByMouse = true;
        mouseStartClickY = (int) mouse.getPosition().y;
        clickStartScroll = scroll;
        onLeftClickSupplier.get();
        return true;
    }

    @Override
    public boolean onMouseLeftRelease() {
        movingByMouse = false;
        return false;
    }

    @Override
    public void render() {
        if (isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        } else {
            guiRenderer.add(lastX, lastY, x, y, width, height, color.x, color.y, color.z, color.w);
        }
    }

    public void updateScrollableObjectsY() {
        for (int i = 0; i < scrollableElements.size(); i++) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(i);
            scrollableGuiObject.updateY();
        }
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

    public Scroll setTotalHeight(int totalHeight) {
        this.totalHeight = totalHeight;
        return this;
    }
}