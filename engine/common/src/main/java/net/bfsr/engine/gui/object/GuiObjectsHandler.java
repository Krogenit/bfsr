package net.bfsr.engine.gui.object;

import java.util.List;

public interface GuiObjectsHandler {
    void registerGuiObject(GuiObject guiObject);

    void unregisterGuiObject(GuiObject guiObject);

    int getWidth();

    int getHeight();

    void update();

    void render();

    boolean onMouseLeftClick();

    boolean onMouseLeftRelease();

    boolean onMouseRightClick();

    boolean onMouseRightRelease();

    boolean onMouseScroll(float y);

    void onScreenResize(int width, int height);

    boolean input(int key);

    void textInput(int key);

    void openContextMenu(GuiObject... objects);

    boolean isContextMenuOpened();

    List<GuiObject> getGuiObjects();

    void clear();
}