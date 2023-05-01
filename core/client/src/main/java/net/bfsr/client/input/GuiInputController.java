package net.bfsr.client.input;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiManager;

public class GuiInputController extends InputController {
    private GuiManager guiManager;

    public void init() {
        guiManager = Core.get().getGuiManager();
    }

    @Override
    public boolean input(int key) {
        Gui currentGui = guiManager.getCurrentGui();
        if (currentGui != null) {
            currentGui.input(key);
            return true;
        } else {
            guiManager.getGuiInGame().input(key);
        }

        return false;
    }

    @Override
    public void textInput(int key) {
        Gui gui = guiManager.getCurrentGui();
        if (gui != null) {
            gui.textInput(key);
        } else {
            guiManager.getGuiInGame().textInput(key);
        }
    }

    @Override
    public boolean scroll(float y) {
        Gui gui = guiManager.getCurrentGui();
        if (gui != null) {
            gui.onMouseScroll(y);
            return true;
        } else {
            guiManager.getGuiInGame().onMouseScroll(y);
        }

        return false;
    }

    @Override
    public boolean onMouseLeftClick() {
        Gui gui = guiManager.getCurrentGui();
        if (gui != null) {
            return gui.onMouseLeftClick();
        } else {
            return guiManager.getGuiInGame().onMouseLeftClick();
        }
    }

    @Override
    public boolean onMouseLeftRelease() {
        Gui gui = guiManager.getCurrentGui();
        if (gui != null) {
            gui.onMouseLeftRelease();
        } else {
            guiManager.getGuiInGame().onMouseLeftRelease();
        }

        return false;
    }

    @Override
    public boolean onMouseRightClick() {
        Gui gui = guiManager.getCurrentGui();
        if (gui != null) {
            return gui.onMouseRightClick();
        } else {
            return guiManager.getGuiInGame().onMouseRightClick();
        }
    }

    @Override
    public boolean onMouseRightRelease() {
        Gui gui = guiManager.getCurrentGui();
        if (gui != null) {
            gui.onMouseRightRelease();
        } else {
            guiManager.getGuiInGame().onMouseRightRelease();
        }

        return false;
    }
}