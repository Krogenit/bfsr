package net.bfsr.client.input;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.GuiManager;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.input.AbstractMouse;
import org.joml.Vector2i;

import java.util.List;

public class GuiInputController extends InputController {
    private final GuiManager guiManager = Engine.getGuiManager();
    private final AbstractMouse mouse = Engine.getMouse();

    @Override
    public boolean input(int key) {
        return guiManager.getLast().input(key);
    }

    @Override
    public boolean textInput(int key) {
        return guiManager.getLast().textInput(key);
    }

    @Override
    public boolean scroll(float scrollY) {
        Vector2i mousePosition = mouse.getGuiPosition();
        return guiManager.getLast().mouseScroll(mousePosition.x, mousePosition.y, scrollY);
    }

    @Override
    public boolean mouseMove(float x, float y) {
        return guiManager.getLast().mouseMove(x, y);
    }

    @Override
    public boolean mouseLeftClick() {
        Vector2i mousePosition = mouse.getGuiPosition();
        GuiObject hoveredGuiObject = guiManager.findHoveredGuiObject(mousePosition.x, mousePosition.y);

        boolean contextMenuWasOpen = guiManager.isContextMenuOpen();
        guiManager.forEach(guiObject -> guiObject.mouseLeftClick(mousePosition.x, mousePosition.y));
        if (hoveredGuiObject != null) {
            hoveredGuiObject.getLeftClickConsumer().accept(mousePosition.x, mousePosition.y);
            return true;
        }

        return contextMenuWasOpen;
    }

    @Override
    public boolean mouseLeftRelease() {
        Vector2i mousePosition = mouse.getGuiPosition();
        GuiObject hoveredGuiObject = guiManager.findHoveredGuiObject(mousePosition.x, mousePosition.y);
        List<GuiObject> guiStack = guiManager.getGuiStack();

        GuiObject guiObject = null;
        for (int i = 0; i < guiStack.size(); i++) {
            guiObject = guiStack.get(i).mouseLeftRelease(mousePosition.x, mousePosition.y);
        }

        if (hoveredGuiObject == null || guiObject != hoveredGuiObject) {
            return false;
        }

        hoveredGuiObject.getLeftReleaseConsumer().accept(mousePosition.x, mousePosition.y);
        return true;
    }

    @Override
    public boolean mouseRightClick() {
        Vector2i mousePosition = mouse.getGuiPosition();
        GuiObject hoveredGuiObject = guiManager.findHoveredGuiObject(mousePosition.x, mousePosition.y);
        guiManager.forEach(guiObject -> guiObject.mouseRightClick(mousePosition.x, mousePosition.y));
        if (hoveredGuiObject == null) {
            return false;
        }

        hoveredGuiObject.getRightClickConsumer().accept(mousePosition.x, mousePosition.y);
        return true;
    }

    @Override
    public boolean mouseRightRelease() {
        Vector2i mousePosition = mouse.getGuiPosition();
        GuiObject hoveredGuiObject = guiManager.findHoveredGuiObject(mousePosition.x, mousePosition.y);
        guiManager.forEach(guiObject -> guiObject.mouseRightRelease(mousePosition.x, mousePosition.y));
        if (hoveredGuiObject == null) {
            return false;
        }

        hoveredGuiObject.getRightReleaseConsumer().accept(mousePosition.x, mousePosition.y);
        return true;
    }
}