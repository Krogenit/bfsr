package net.bfsr.client.gui.scroll;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.gui.SimpleGuiObject;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.render.BufferType;
import net.bfsr.client.render.InstancedRenderer;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.core.Core;

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
    private boolean collided;
    @Getter
    private boolean movingByMouse;
    private int scrollHeight;
    private int scrollY;
    private final List<ScrollableGuiObject> scrollableElements = new ArrayList<>();
    private BiFunction<Integer, Integer, Integer> viewHeightResizeFunction = (width, height) -> viewHeight;
    @Setter
    private float scrollModifier = 20.0f;

    public Scroll() {
        super(0, 0, 0, 0);
    }

    public void registerGuiObject(Scrollable scrollable) {
        scrollableElements.add(new ScrollableGuiObject(scrollable));
    }

    @Override
    public void scroll(float y) {
        updatePositionAndSize((int) (scroll - y * scrollModifier));
    }

    public void scrollBottom() {
        updatePositionAndSize(Integer.MAX_VALUE);
    }

    @Override
    public void update() {
        if (movingByMouse) {
            updatePositionAndSize((int) (clickStartScroll + (Mouse.getPosition().y - mouseStartClickY) / (scrollHeight / (float) totalHeight)));
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

    private void updatePositionAndSize(int newValue) {
        int heightDiff = totalHeight - viewHeight;
        if (heightDiff < 0) heightDiff = 0;

        scroll = newValue;

        if (scroll < 0) scroll = 0;
        else if (scroll > heightDiff) scroll = heightDiff;

        for (int i = 0; i < scrollableElements.size(); i++) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(i);
            Scrollable scrollable = scrollableGuiObject.getScrollable();
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
    public void resize(int width, int height) {
        for (int i = 0; i < scrollableElements.size(); i++) {
            ScrollableGuiObject scrollableGuiObject = scrollableElements.get(i);
            scrollableGuiObject.updateY();
        }

        super.resize(width, height);
        viewHeight = viewHeightResizeFunction.apply(width, height);
        updatePositionAndSize(scroll);
    }

    @Override
    public void onMouseLeftClick() {
        if (isIntersects()) {
            movingByMouse = true;
            mouseStartClickY = (int) Mouse.getPosition().y;
            clickStartScroll = scroll;
            Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
        }
    }

    @Override
    public void onMouseLeftRelease() {
        movingByMouse = false;
    }

    @Override
    public void render() {
        InstancedRenderer.INSTANCE.addGUIElementToRenderPipeLine(x, y, width, height, color.x, color.y, color.z, color.w, 0, BufferType.GUI);
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
