package net.bfsr.engine.gui.component;

import net.bfsr.engine.gui.component.scroll.Scroll;
import net.bfsr.engine.gui.renderer.ScrollPaneRenderer;
import org.joml.Vector4f;

import java.util.List;

public class ScrollPane extends GuiObject {
    private final Scroll scroll;
    private final GuiObject pane = new GuiObject();

    public ScrollPane(int width, int height, int scrollWidth) {
        super(width, height);
        this.scroll = new Scroll(scrollWidth, height);

        super.add(pane.setCanBeHovered(false).setFillParent());
        super.add(scroll.setViewHeightResizeFunction((width1, height1) -> this.height).atTopRight(() -> -scroll.getWidth(), () -> 0)
                .setWidthFunction((width1, height1) -> scrollWidth).setHeightFunction((width1, height1) -> this.height));
        setRenderer(new ScrollPaneRenderer(this, scroll));
    }

    @Override
    public void add(GuiObject guiObject) {
        pane.add(guiObject);
        scroll.addScrollable(guiObject);
        guiObject.setParent(this);
        guiObject.onAdded();
    }

    @Override
    public void addAt(int index, GuiObject guiObject) {
        pane.addAt(index, guiObject);
        scroll.addScrollable(guiObject);
        guiObject.setParent(this);
        guiObject.onAdded();
    }

    @Override
    public int addBefore(GuiObject guiObject, GuiObject beforeObject) {
        int index = pane.addBefore(guiObject, beforeObject);
        scroll.addScrollable(guiObject);
        guiObject.setParent(this);
        guiObject.onAdded();
        return index;
    }

    @Override
    public void remove(GuiObject guiObject) {
        pane.remove(guiObject);
        scroll.removeScrollable(guiObject);
    }

    public void scrollBottom() {
        scroll.scrollBottom();
    }

    public void setScrollColor(Vector4f color) {
        setScrollColor(color.x, color.y, color.z, color.w);
    }

    public ScrollPane setScrollColor(float r, float g, float b, float a) {
        scroll.setColor(r, g, b, a);
        return this;
    }

    public ScrollPane setScrollHoverColor(Vector4f color) {
        setScrollHoverColor(color.x, color.y, color.z, color.w);
        return this;
    }

    public ScrollPane setScrollHoverColor(float r, float g, float b, float a) {
        scroll.setHoverColor(r, g, b, a);
        return this;
    }

    public int getScrollWidth() {
        return scroll.getWidth();
    }

    public int getTotalHeight() {
        return scroll.getTotalHeight();
    }

    @Override
    public GuiObject getHovered(GuiObject hoveredObject) {
        if (isIntersectsWithMouse()) {
            hoveredObject = this;

            for (int i = 0, size = guiObjects.size(); i < size; i++) {
                hoveredObject = guiObjects.get(i).getHovered(hoveredObject);
            }
        }

        return hoveredObject;
    }

    public boolean isMovingByMouse() {
        return scroll.isMovingByMouse();
    }

    @Override
    public List<GuiObject> getGuiObjects() {
        return pane.getGuiObjects();
    }

    @Override
    public void clear() {
        pane.clear();
        scroll.clear();
    }
}