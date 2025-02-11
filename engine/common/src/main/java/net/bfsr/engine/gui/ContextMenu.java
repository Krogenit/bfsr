package net.bfsr.engine.gui;

import net.bfsr.engine.gui.component.GuiObject;
import org.jetbrains.annotations.Nullable;

public class ContextMenu extends GuiObject {
    void add(GuiObject[] objects) {
        for (int i = 0, size = guiObjects.size(); i < size; i++) {
            guiObjects.get(i).clear();
        }

        while (guiObjects.size() > 0) {
            remove(guiObjects.get(0));
        }

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

    private void close() {
        clear();
        removeFromParent();
    }

    @Override
    public GuiObject mouseLeftClick(int mouseX, int mouseY) {
        GuiObject guiObject = super.mouseLeftClick(mouseX, mouseY);

        if (guiObject == null) {
            clear();
            return this;
        }

        return guiObject;
    }

    @Override
    public GuiObject mouseLeftRelease(int mouseX, int mouseY) {
        GuiObject guiObject = super.mouseLeftRelease(mouseX, mouseY);
        close();
        return guiObject != null ? guiObject : this;
    }

    @Override
    public @Nullable GuiObject mouseRightClick(int mouseX, int mouseY) {
        GuiObject guiObject = super.mouseRightClick(mouseX, mouseY);

        if (guiObject == null) {
            clear();
            return this;
        }

        return guiObject;
    }

    @Override
    public GuiObject mouseRightRelease(int mouseX, int mouseY) {
        GuiObject guiObject = super.mouseRightRelease(mouseX, mouseY);
        close();
        return guiObject != null ? guiObject : this;
    }

    @Override
    public boolean mouseMove(float x, float y) {
        super.mouseMove(x, y);
        return true;
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, float scrollY) {
        super.mouseScroll(mouseX, mouseY, scrollY);
        return true;
    }

    boolean isOpen() {
        return guiObjects.size() > 0;
    }
}
