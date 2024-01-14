package net.bfsr.engine.gui;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.object.GuiObject;
import net.bfsr.engine.gui.object.GuiObjectsHandler;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public abstract class Gui implements GuiObjectsHandler {
    protected final AbstractRenderer renderer = Engine.renderer;
    protected final AbstractGUIRenderer guiRenderer = renderer.guiRenderer;
    protected Gui parentGui;
    @Getter
    protected int width, height;
    @Getter
    protected final List<GuiObject> guiObjects = new ArrayList<>();
    private final List<GuiObject> contextMenu = new ArrayList<>();
    @Getter
    private GuiObject hoveredGuiObject;

    protected Gui(Gui parentGui) {
        this.parentGui = parentGui;
    }

    public void init() {
        width = renderer.getScreenWidth();
        height = renderer.getScreenHeight();
        initElements();
    }

    protected abstract void initElements();

    @Override
    public void registerGuiObjectBefore(GuiObject guiObject, GuiObject beforeObject) {
        int index = guiObjects.indexOf(beforeObject);
        if (index >= 0) {
            guiObjects.add(index, guiObject);
            guiObject.onRegistered(this);
        }
    }

    @Override
    public void registerGuiObject(GuiObject guiObject) {
        guiObjects.add(guiObject);
        guiObject.onRegistered(this);
    }

    @Override
    public void unregisterGuiObject(GuiObject guiObject) {
        guiObjects.remove(guiObject);
        guiObject.onUnregistered(this);
    }

    @Override
    public void update() {
        GuiObject hoveredObject;
        if (isContextMenuOpened()) {
            hoveredObject = updateGuiObjectsHover(hoveredGuiObject, contextMenu);
        } else {
            hoveredObject = updateGuiObjectsHover(hoveredGuiObject, guiObjects);
        }

        if (hoveredObject != hoveredGuiObject) {
            if (hoveredGuiObject != null) {
                hoveredGuiObject.onMouseStopHover();
            }

            hoveredGuiObject = hoveredObject;
            hoveredGuiObject.onMouseHover();
        }

        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).update();
        }
    }

    private GuiObject updateGuiObjectsHover(GuiObject hoveredObject, List<GuiObject> guiObjects) {
        int size = guiObjects.size();

        for (int i = 0; i < size; i++) {
            GuiObject guiObject = guiObjects.get(i);

            guiObject.updateMouseHover();
            if (guiObject.isMouseHover()) {
                if (hoveredObject != null && hoveredObject != guiObject) {
                    hoveredObject.setMouseHover(false);
                }
                hoveredObject = guiObject;
            }

            if (guiObject instanceof GuiObjectsHandler guiObjectsHandler) {
                hoveredObject = updateGuiObjectsHover(hoveredObject, guiObjectsHandler.getGuiObjects());
            }
        }

        return hoveredObject;
    }

    @Override
    public void render() {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            GuiObject guiObject = guiObjects.get(i);
            guiObject.render();
        }
    }

    @Override
    public boolean onMouseLeftClick() {
        if (contextMenu.size() > 0) {
            boolean contextMenuHovered = false;
            for (int i = 0; i < contextMenu.size(); i++) {
                if (contextMenu.get(i).isMouseHover()) {
                    contextMenuHovered = true;
                    break;
                }
            }

            if (!contextMenuHovered) {
                closeContextMenu();
            }

            return contextMenuHovered;
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
        if (contextMenu.size() > 0) {
            for (int i = 0; i < contextMenu.size(); i++) {
                if (contextMenu.get(i).onMouseLeftRelease()) {
                    break;
                }
            }

            closeContextMenu();
            return true;
        } else {
            boolean mouseLeftRelease = false;
            for (int i = 0; i < guiObjects.size(); i++) {
                if (guiObjects.get(i).onMouseLeftRelease()) {
                    mouseLeftRelease = true;
                }
            }

            return mouseLeftRelease;
        }
    }

    @Override
    public boolean onMouseRightClick() {
        if (contextMenu.size() > 0) {
            boolean contextMenuClicked = false;
            for (int i = 0; i < contextMenu.size(); i++) {
                if (contextMenu.get(i).onMouseRightClick()) {
                    contextMenuClicked = true;
                    break;
                }
            }

            closeContextMenu();

            if (contextMenuClicked) {
                return true;
            }
        }

        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            GuiObject guiObject = guiObjects.get(i);
            if (guiObject.onMouseRightClick()) {
                onGuiObjectMouseRightClick(guiObject);
                return true;
            }
        }

        return false;
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

    @Override
    public boolean onMouseScroll(float y) {
        boolean scroll = false;
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            if (guiObjects.get(i).onMouseScroll(y)) {
                scroll = true;
            }
        }

        return scroll;
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
    public void onScreenResize(int width, int height) {
        this.width = width;
        this.height = height;
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).onScreenResize(width, height);
        }
    }

    @Override
    public boolean input(int key) {
        boolean input = false;
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            if (guiObjects.get(i).input(key)) {
                input = true;
            }
        }

        return input;
    }

    @Override
    public void textInput(int key) {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).textInput(key);
        }
    }

    @Override
    public void openContextMenu(GuiObject... objects) {
        if (isContextMenuOpened()) {
            closeContextMenu();
        }

        GuiObject guiObject = objects[0];
        int minX = guiObject.getX();
        int maxX = guiObject.getX() + guiObject.getWidth();

        for (int i = 1; i < objects.length; i++) {
            guiObject = objects[i];
            minX = Math.min(minX, guiObject.getX());
            maxX = Math.max(maxX, guiObject.getX() + guiObject.getWidth());
        }

        int width = maxX - minX;

        for (int i = 0; i < objects.length; i++) {
            guiObject = objects[i];
            guiObject.setWidth(width);
            contextMenu.add(guiObject);
            registerGuiObject(guiObject);
        }
    }

    protected void closeContextMenu() {
        for (int i = 0; i < contextMenu.size(); i++) {
            unregisterGuiObject(contextMenu.get(i));
        }

        contextMenu.clear();

        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).onContextMenuClosed();
        }
    }

    @Override
    public boolean isContextMenuOpened() {
        return contextMenu.size() > 0;
    }

    @Override
    public boolean isObjectRegistered(GuiObject object) {
        return guiObjects.contains(object);
    }

    @Override
    public void clear() {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).clear();
        }
        guiObjects.clear();
    }

    public boolean isAllowCameraZoom() {
        return false;
    }
}