package net.bfsr.client.gui;

import net.bfsr.client.gui.scroll.Scrollable;

public interface GuiObject extends Scrollable {
    void setX(int x);
    int getX();
    int getY();
    GuiObject setWidth(int width);
    int getWidth();
    void update();
    void render();
    boolean onMouseLeftClick();
    void onMouseLeftRelease();
    boolean onMouseRightClick();
    void onMouseScroll(float y);
    void onScreenResize(int width, int height);
    void input(int key);
    void textInput(int key);
    void clear();
    void onRegistered(GuiObjectsHandler gui);
    void onUnregistered(GuiObjectsHandler gui);
    boolean isIntersectsWithMouse();
    void updatePositionAndSize(int width, int height);
    boolean isMouseHover();
    void setMouseHover(boolean value);
    void onMouseStopHover();
    void onMouseHover();
    void updateMouseHover();
    void onOtherGuiObjectMouseLeftClick(GuiObject guiObject);
    void onOtherGuiObjectMouseRightClick(GuiObject guiObject);
    boolean isVisible();
    GuiObject setColor(float r, float g, float b, float a);
    GuiObject setHoverColor(float r, float g, float b, float a);
    GuiObject setOutlineColor(float r, float g, float b, float a);
    GuiObject setOutlineHoverColor(float r, float g, float b, float a);
    GuiObject setTextColor(float r, float g, float b, float a);
    void onContextMenuClosed();
}