package net.bfsr.client.gui.scroll;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.GuiObject;
import net.bfsr.client.gui.SimpleGuiObject;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;

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
    @Setter
    private SoundRegistry collideSound;
    private int minObjectY = Integer.MAX_VALUE, maxObjectY = Integer.MIN_VALUE;

    public void registerGuiObject(GuiObject guiObject) {
        ScrollableGuiObject scrollableGuiObject = new ScrollableGuiObject(guiObject);
        scrollableElements.add(scrollableGuiObject);
        guiObject.setY(scrollableGuiObject.getY() - scroll);
        minObjectY = Math.min(guiObject.getY(), minObjectY);
        maxObjectY = Math.max(guiObject.getY() + guiObject.getHeight(), maxObjectY);
        totalHeight = maxObjectY - minObjectY;
    }

    public void unregisterGuiObject(GuiObject guiObject) {
        if (scrollableElements.remove(new ScrollableGuiObject(guiObject))) {
            updateTotalHeight();
        }
    }

    private void updateTotalHeight() {
        if (scrollableElements.size() > 0) {
            ScrollableGuiObject guiObject = scrollableElements.get(0);
            minObjectY = guiObject.getGuiObject().getY();
            maxObjectY = guiObject.getGuiObject().getY() + guiObject.getGuiObject().getHeight();
            for (int i = 0; i < scrollableElements.size(); i++) {
                guiObject = scrollableElements.get(i);
                minObjectY = Math.min(guiObject.getGuiObject().getY(), minObjectY);
                maxObjectY = Math.max(guiObject.getGuiObject().getY() + guiObject.getGuiObject().getHeight(), maxObjectY);
            }
            totalHeight = maxObjectY - minObjectY;
        } else {
            totalHeight = 0;
        }
    }

    @Override
    public void onMouseScroll(float y) {
        accumulator -= y * scrollAmount;
    }

    public void scrollBottom() {
        updatePositionAndSize(Integer.MAX_VALUE);
    }

    @Override
    public void onMouseHover() {
        if (collideSound != null) {
            Core.get().getSoundManager().play(new GuiSoundSource(collideSound));
        }
    }

    @Override
    public void update() {
        super.update();

        if (accumulator != 0) {
            updatePositionAndSize(scroll + accumulator);
            accumulator = 0;
        }

        if (movingByMouse) {
            updatePositionAndSize((int) (clickStartScroll + (Mouse.getPosition().y - mouseStartClickY) / (scrollHeight / (float) totalHeight)));
        }
    }

    public void updateScroll() {
        updatePositionAndSize(scroll);
    }

    private void updatePositionAndSize(int newValue) {
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
        updatePositionAndSize(scroll);
    }

    @Override
    public boolean onMouseLeftClick() {
        if (!isMouseHover()) return false;

        movingByMouse = true;
        mouseStartClickY = (int) Mouse.getPosition().y;
        clickStartScroll = scroll;
        Core.get().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
        return true;
    }

    @Override
    public void onMouseLeftRelease() {
        movingByMouse = false;
    }

    @Override
    public void render() {
        if (isMouseHover()) {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        } else {
            GUIRenderer.get().add(lastX, lastY, x, y, width, height, color.x, color.y, color.z, color.w);
        }
    }

    public void updateScrollableObjectsY() {
        for (int i = 0; i < scrollableElements.size(); i++) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(i);
            scrollableGuiObject.updateY();
        }
    }

    public void resetScroll() {
        scroll = 0;
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

    public boolean isScrollNeeded() {
        return totalHeight > viewHeight;
    }

    public void removeAllRegisteredObjects() {
        totalHeight = 0;
        scrollableElements.clear();
    }
}