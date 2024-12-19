package net.bfsr.client.input;

import net.bfsr.client.Client;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.GuiObject;

import java.util.List;

class GuiInputController extends InputController {
    private GuiManager guiManager;

    @Override
    public void init() {
        guiManager = Client.get().getGuiManager();
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
        List<GuiObject> guiStack = guiManager.getGuiStack();

        GuiObject guiObject = null;
        for (int i = 0; i < guiStack.size(); i++) {
            guiObject = guiStack.get(i).mouseLeftRelease();
        }

        if (hoveredGuiObject == null || guiObject != hoveredGuiObject) {
            return false;
        }

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