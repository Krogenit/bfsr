package net.bfsr.client.gui;

import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public abstract class Gui {
    protected Gui parentGui;
    protected int width, height;
    protected final Vector2i center = new Vector2i();
    protected final List<IGuiObject> guiObjects = new ArrayList<>();

    protected Gui(Gui parentGui) {
        this.parentGui = parentGui;
    }

    public void init() {
        width = Core.get().getScreenWidth();
        height = Core.get().getScreenHeight();
        updateCenter();
        initElements();
        resize(width, height);
    }

    private void updateCenter() {
        center.x = width / 2;
        center.y = height / 2;
    }

    protected abstract void initElements();

    public void registerGuiObject(IGuiObject guiObject) {
        guiObjects.add(guiObject);
    }

    public void unregisterGuiObject(IGuiObject guiObject) {
        guiObjects.remove(guiObject);
    }

    public void update() {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).update();
        }
    }

    public void render(float interpolation) {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).render();
        }
    }

    public void onMouseLeftClicked() {
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).onMouseLeftClick();
        }
    }

    public void onMouseLeftRelease() {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).onMouseLeftRelease();
        }
    }

    public void onMouseRightClicked() {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).onMouseRightClick();
        }
    }

    public void onMouseRightRelease() {

    }

    public void onMouseScroll(float y) {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).scroll(y);
        }
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        updateCenter();
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).resize(width, height);
        }
    }

    public void input(int key) {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).input(key);
        }
    }

    public void textInput(int key) {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).textInput(key);
        }
    }

    public void clear() {
        int size = guiObjects.size();
        for (int i = 0; i < size; i++) {
            guiObjects.get(i).clear();
        }
        guiObjects.clear();
    }
}
