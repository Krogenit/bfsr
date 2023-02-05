package net.bfsr.client.gui;

import net.bfsr.client.gui.scroll.Scrollable;

public interface IGuiObject extends Scrollable {
    void setX(int x);
    void setY(int y);
    int getY();
    void update();
    void render();
    void onMouseLeftClick();
    void onMouseLeftRelease();
    void onMouseRightClick();
    void scroll(float y);
    void resize(int width, int height);
    void input(int key);
    void textInput(int key);
    void clear();
}
