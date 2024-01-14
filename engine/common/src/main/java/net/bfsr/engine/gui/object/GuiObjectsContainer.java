package net.bfsr.engine.gui.object;

import lombok.Getter;
import net.bfsr.engine.gui.scroll.Scroll;
import net.bfsr.engine.renderer.opengl.GL;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class GuiObjectsContainer extends GuiObjectWithSubObjects implements GuiObjectsHandler {
    @Getter
    private final List<GuiObject> guiObjects = new ArrayList<>();
    private final Scroll scroll = new Scroll();

    public GuiObjectsContainer(int scrollWidth) {
        this(0, scrollWidth);
    }

    public GuiObjectsContainer(int width, int scrollWidth) {
        super(width, 0);
        scroll.setWidth(scrollWidth);
        scroll.setViewHeightResizeFunction((width1, height1) -> this.height);
        scroll.setHeightResizeFunction((width1, height1) -> this.height);
        scroll.updatePositionAndSize();
    }

    @Override
    public void update() {
        super.update();

        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).update();
        }

        scroll.update();
    }

    @Override
    public void updateMouseHover() {
        super.updateMouseHover();

        if (isContextMenuOpened() || !isMouseHover()) {
            scroll.setMouseHover(false);
        } else {
            scroll.updateMouseHover();
        }
    }

    @Override
    public boolean onMouseLeftClick() {
        if (!isIntersectsWithMouse()) return false;

        if (scroll.isMouseHover()) {
            scroll.onMouseLeftClick();
            return true;
        } else {
            for (int i = 0; i < guiObjects.size(); i++) {
                GuiObject guiObject = guiObjects.get(i);
                if (guiObject.onMouseLeftClick()) {
                    onGuiObjectMouseLeftClick(guiObject);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onMouseLeftRelease() {
        if (!isIntersectsWithMouse()) return false;

        boolean leftRelease = false;
        for (int i = 0; i < guiObjects.size(); i++) {
            if (guiObjects.get(i).onMouseLeftRelease()) {
                leftRelease = true;
            }
        }

        if (scroll.onMouseLeftRelease()) {
            leftRelease = true;
        }

        return leftRelease;
    }

    @Override
    public boolean onMouseRightClick() {
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject = guiObjects.get(i);
            if (guiObject.isMouseHover() && guiObject.onMouseRightClick()) {
                onGuiObjectMouseRightClick(guiObject);
                return true;
            }
        }

        if (!isMouseHover()) return false;
        return onRightClickSupplier.get();
    }

    @Override
    public boolean onMouseRightRelease() {
        boolean onMouseRightRelease = false;
        for (int i = 0; i < guiObjects.size(); i++) {
            if (guiObjects.get(i).onMouseRightRelease()) {
                onMouseRightRelease = true;
            }
        }

        return onMouseRightRelease;
    }

    private void onGuiObjectMouseLeftClick(GuiObject guiObject) {
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject1 = guiObjects.get(i);
            if (guiObject1 != guiObject) {
                guiObject1.onOtherGuiObjectMouseLeftClick(guiObject);
            }
        }
    }

    private void onGuiObjectMouseRightClick(GuiObject guiObject) {
        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject1 = guiObjects.get(i);
            if (guiObject1 != guiObject) {
                guiObject1.onOtherGuiObjectMouseRightClick(guiObject);
            }
        }
    }

    @Override
    public void onOtherGuiObjectMouseLeftClick(GuiObject guiObject) {
        super.onOtherGuiObjectMouseLeftClick(guiObject);
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).onOtherGuiObjectMouseLeftClick(guiObject);
        }
    }

    @Override
    public void onOtherGuiObjectMouseRightClick(GuiObject guiObject) {
        super.onOtherGuiObjectMouseRightClick(guiObject);
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).onOtherGuiObjectMouseRightClick(guiObject);
        }
    }

    @Override
    public boolean onMouseScroll(float y) {
        if (!isIntersectsWithMouse()) return false;

        scroll.onMouseScroll(y);
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).onMouseScroll(y);
        }

        return true;
    }

    @Override
    public boolean input(int key) {
        super.input(key);
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).input(key);
        }
        return false;
    }

    @Override
    public void textInput(int key) {
        super.textInput(key);
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).textInput(key);
        }
    }

    @Override
    public void render() {
        guiRenderer.render();
        renderer.glEnable(GL.GL_SCISSOR_TEST);
        renderer.glScissor(x, renderer.getScreenHeight() - (y + height), width, height);

        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject = guiObjects.get(i);
            if (isIntersects(guiObject.getX(), guiObject.getY()) ||
                    isIntersects(guiObject.getX() + guiObject.getWidth() - 1, guiObject.getY() + guiObject.getHeight() - 1)) {
                guiObject.render();
            }
        }

        guiRenderer.render();
        renderer.glDisable(GL.GL_SCISSOR_TEST);

        scroll.render();
    }

    @Override
    public void addSubObject(AbstractGuiObject object) {
        subObjects.add(object);
        registerGuiObject(object);
    }

    @Override
    public void addSubObject(int index, AbstractGuiObject object) {
        subObjects.add(index, object);
        registerGuiObject(object);
    }

    @Override
    public void removeSubObject(AbstractGuiObject object) {
        subObjects.remove(object);
        unregisterGuiObject(object);
    }

    @Override
    public void registerGuiObjectBefore(GuiObject guiObject, GuiObject beforeObject) {
        int index = guiObjects.indexOf(beforeObject);
        if (index >= 0) {
            guiObjects.add(index, guiObject);
            guiObject.onRegistered(this);
            scroll.registerGuiObject(guiObject);
        }
    }

    @Override
    public void registerGuiObject(GuiObject guiObject) {
        guiObjects.add(guiObject);
        guiObject.onRegistered(this);
        scroll.registerGuiObject(guiObject);
    }

    @Override
    public void unregisterGuiObject(GuiObject guiObject) {
        guiObjects.remove(guiObject);
        guiObject.onUnregistered(this);
        scroll.unregisterGuiObject(guiObject);
    }

    @Override
    public void onUnregistered(GuiObjectsHandler gui) {
        super.onUnregistered(gui);
        while (guiObjects.size() > 0) {
            unregisterGuiObject(guiObjects.get(0));
        }
    }

    @Override
    protected void registerSubElements(GuiObjectsHandler gui) {}

    @Override
    protected void unregisterSubElements(GuiObjectsHandler gui) {}

    public void updateScrollObjectsY() {
        scroll.updateScrollableObjectsY();
    }

    @Override
    protected void setRepositionConsumerForSubObjects() {
        subObjectsRepositionConsumer.setup(scroll, width - scroll.getWidth(), 0);
    }

    @Override
    public void updatePositionAndSize(int width, int height) {
        super.updatePositionAndSize(width, height);
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).updatePositionAndSize(width, height);
        }
        scroll.updatePositionAndSize(width, height);
    }

    @Override
    public void openContextMenu(GuiObject... objects) {
        gui.openContextMenu(objects);
    }

    @Override
    public boolean isContextMenuOpened() {
        return gui.isContextMenuOpened();
    }

    @Override
    public boolean isObjectRegistered(GuiObject object) {
        return guiObjects.contains(object);
    }

    public int getScrollWidth() {
        return scroll.getWidth();
    }

    @Override
    public GuiObjectsContainer setWidth(int width) {
        super.setWidth(width);
        setRepositionConsumerForSubObjects();
        scroll.updatePositionAndSize();
        return this;
    }

    public void setScrollColor(Vector4f color) {
        setScrollColor(color.x, color.y, color.z, color.w);
    }

    public void setScrollColor(float r, float g, float b, float a) {
        scroll.setColor(r, g, b, a);
    }

    public void setScrollHoverColor(Vector4f color) {
        setScrollHoverColor(color.x, color.y, color.z, color.w);
    }

    public void setScrollHoverColor(float r, float g, float b, float a) {
        scroll.setHoverColor(r, g, b, a);
    }
}