package net.bfsr.engine.gui;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.gui.component.GuiObject;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class ContextMenu extends GuiObject {
    private final Consumer<ContextMenu> closeListener;

    void open(GuiObject[] objects) {
        GuiObject guiObject = objects[0];
        int minX = guiObject.getX();
        int maxX = guiObject.getX() + guiObject.getWidth();

        for (int i = 1, size = objects.length; i < size; i++) {
            guiObject = objects[i];
            minX = Math.min(minX, guiObject.getX());
            maxX = Math.max(maxX, guiObject.getX() + guiObject.getWidth());
        }

        int width = maxX - minX;

        for (int i = 0, size = objects.length; i < size; i++) {
            guiObject = objects[i];
            guiObject.setWidth(width);
            add(guiObject);
        }
    }

    void close() {
        clear();
        closeListener.accept(this);
    }

    @Override
    public GuiObject mouseLeftClick() {
        GuiObject guiObject = super.mouseLeftClick();

        if (guiObject == null) {
            close();
            return this;
        }

        return guiObject;
    }

    @Override
    public GuiObject mouseLeftRelease() {
        GuiObject guiObject = super.mouseLeftRelease();
        close();
        return guiObject != null ? guiObject : this;
    }

    @Override
    public boolean mouseMove(float x, float y) {
        super.mouseMove(x, y);
        return true;
    }

    @Override
    public boolean mouseScroll(float y) {
        super.mouseScroll(y);
        return true;
    }
}
