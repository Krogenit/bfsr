package net.bfsr.client.gui;

import java.util.List;

public interface GuiObjectsHandler {
    void registerGuiObject(GuiObject guiObject);
    void unregisterGuiObject(GuiObject guiObject);
    int getWidth();
    int getHeight();
    void update();
    void render();
    boolean onMouseLeftClick();
    void onMouseLeftRelease();
    boolean onMouseRightClick();
    void onMouseRightRelease();
    void onMouseScroll(float y);
    void onScreenResize(int width, int height);
    void input(int key);
    void textInput(int key);
    void openContextMenu(GuiObject... objects);
    boolean isContextMenuOpened();
    List<GuiObject> getGuiObjects();
    void clear();
}