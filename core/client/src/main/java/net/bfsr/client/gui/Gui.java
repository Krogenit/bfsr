package net.bfsr.client.gui;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public abstract class Gui implements GuiObjectsHandler {
    protected Gui parentGui;
    @Getter
    protected int width, height;
    protected final Vector2i center = new Vector2i();
    @Getter
    protected final List<GuiObject> guiObjects = new ArrayList<>();
    private final List<GuiObject> contextMenu = new ArrayList<>();
    @Getter
    private GuiObject hoveredGuiObject;

    protected Gui(Gui parentGui) {
        this.parentGui = parentGui;
    }

    public void init() {
        width = Core.get().getScreenWidth();
        height = Core.get().getScreenHeight();
        updateCenter();
        initElements();
    }

    private void updateCenter() {
        center.x = width / 2;
        center.y = height / 2;
    }

    protected abstract void initElements();

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
        if (isContextMenuOpened()) {
            GuiUpdateUtils.setGuiObjectsHover(guiObjects, false);
            hoveredGuiObject = GuiUpdateUtils.updateGuiObjectsHover(contextMenu);
        } else {
            hoveredGuiObject = GuiUpdateUtils.updateGuiObjectsHover(guiObjects);
        }

        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).update();
        }
    }

    @Override
    public void render() {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            GuiObject guiObject = guiObjects.get(i);
            if (guiObject.isVisible()) {
                guiObject.render();
            }
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
    public void onMouseLeftRelease() {
        if (contextMenu.size() > 0) {
            for (int i = 0; i < contextMenu.size(); i++) {
                if (contextMenu.get(i).onMouseLeftClick()) {
                    break;
                }
            }

            closeContextMenu();
        } else {
            for (int i = 0; i < guiObjects.size(); i++) {
                guiObjects.get(i).onMouseLeftRelease();
            }
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
    public void onMouseRightRelease() {

    }

    @Override
    public void onMouseScroll(float y) {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).onMouseScroll(y);
        }
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
        updateCenter();
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).onScreenResize(width, height);
        }
    }

    @Override
    public void input(int key) {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).input(key);
        }
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