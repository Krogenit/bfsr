package net.bfsr.client.input;

import net.bfsr.client.Core;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.GuiObject;

class GuiInputController extends InputController {
    private GuiManager guiManager;

    @Override
    public void init() {
        guiManager = Core.get().getGuiManager();
    }

    @Override
    public boolean input(int key) {
        return guiManager.getLast().input(key);
    }

    @Override
    public boolean textInput(int key) {
        return guiManager.getLast().textInput(key);
    }

    @Override
    public boolean scroll(float y) {
        return guiManager.getLast().mouseScroll(y);
    }

    @Override
    public boolean mouseMove(float x, float y) {
        return guiManager.getLast().mouseMove(x, y);
    }

    @Override
    public boolean mouseLeftClick() {
        GuiObject hoveredGuiObject = guiManager.findHoveredGuiObject();
        guiManager.forEach(GuiObject::mouseLeftClick);
        if (hoveredGuiObject == null) return false;
        hoveredGuiObject.getLeftClickRunnable().run();
        return true;
    }

    @Override
    public boolean mouseLeftRelease() {
        GuiObject hoveredGuiObject = guiManager.findHoveredGuiObject();
        guiManager.forEach(GuiObject::mouseLeftRelease);
        if (hoveredGuiObject == null) return false;
        hoveredGuiObject.getLeftReleaseRunnable().run();
        return true;
    }

    @Override
    public boolean mouseRightClick() {
        GuiObject hoveredGuiObject = guiManager.findHoveredGuiObject();
        guiManager.forEach(GuiObject::mouseRightClick);
        if (hoveredGuiObject == null) return false;
        hoveredGuiObject.getRightClickRunnable().run();
        return true;
    }

    @Override
    public boolean mouseRightRelease() {
        GuiObject hoveredGuiObject = guiManager.findHoveredGuiObject();
        guiManager.forEach(GuiObject::mouseRightRelease);
        if (hoveredGuiObject == null) return false;
        hoveredGuiObject.getRightReleaseRunnable().run();
        return true;
    }
}