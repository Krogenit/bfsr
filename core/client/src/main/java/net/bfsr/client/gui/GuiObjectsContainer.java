package net.bfsr.client.gui;

import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.scroll.Scroll;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import org.lwjgl.opengl.GL11C;

import java.util.ArrayList;
import java.util.List;

public class GuiObjectsContainer extends GuiObjectWithSubObjects implements GuiObjectsHandler {
    @Getter
    private final List<GuiObject> guiObjects = new ArrayList<>();
    private final Scroll scroll = new Scroll();

    public GuiObjectsContainer(int width, int scrollWidth) {
        super(width, 0);
        scroll.setWidth(scrollWidth);
        scroll.setColor(77 / 255f, 78 / 255f, 81 / 255f, 1.0f);
        scroll.setHoverColor(92 / 255f, 93 / 255f, 94 / 255f, 1.0f);
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
            GuiUpdateUtils.setGuiObjectsHover(guiObjects, false);
            scroll.setMouseHover(false);
        } else {
            GuiUpdateUtils.updateGuiObjectsHover(guiObjects);
            scroll.updateMouseHover();
        }
    }

    @Override
    public boolean onMouseLeftClick() {
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
    public void onMouseLeftRelease() {
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).onMouseLeftRelease();
        }
        scroll.onMouseLeftRelease();
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
    public void onMouseRightRelease() {

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
    public void onMouseScroll(float y) {
        if (isMouseHover()) {
            scroll.onMouseScroll(y);
            for (int i = 0; i < guiObjects.size(); i++) {
                guiObjects.get(i).onMouseScroll(y);
            }
        }
    }

    @Override
    public void input(int key) {
        super.input(key);
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).input(key);
        }
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
        GUIRenderer.get().render();
        GL11C.glEnable(GL11C.GL_SCISSOR_TEST);
        GL11C.glScissor(x, Core.get().getScreenHeight() - (y + height), width, height);

        for (int i = 0; i < guiObjects.size(); i++) {
            GuiObject guiObject = guiObjects.get(i);
            if (isIntersects(guiObject.getX(), guiObject.getY()) || isIntersects(guiObject.getX() + guiObject.getWidth() - 1, guiObject.getY() + guiObject.getHeight() - 1)) {
                guiObject.render();
            }
        }

        GUIRenderer.get().render();
        GL11C.glDisable(GL11C.GL_SCISSOR_TEST);

        scroll.render();
    }

    @Override
    public void addSubObject(AbstractGuiObject object) {
        subObjects.add(object);
        registerGuiObject(object);
    }

    @Override
    public void removeSubObject(AbstractGuiObject object) {
        subObjects.remove(object);
        unregisterGuiObject(object);
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
        scroll.updateScroll();
    }

    @Override
    public void onUnregistered(GuiObjectsHandler gui) {
        super.onUnregistered(gui);
        while (guiObjects.size() > 0) {
            unregisterGuiObject(guiObjects.get(0));
        }
    }

    @Override
    protected void registerSubElements(GuiObjectsHandler gui) {

    }

    @Override
    protected void unregisterSubElements(GuiObjectsHandler gui) {

    }

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
        Core.get().getCurrentGui().openContextMenu(objects);
    }

    @Override
    public boolean isContextMenuOpened() {
        return Core.get().getCurrentGui().isContextMenuOpened();
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
}