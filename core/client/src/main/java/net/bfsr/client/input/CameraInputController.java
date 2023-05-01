package net.bfsr.client.input;

import net.bfsr.client.camera.Camera;
import net.bfsr.client.core.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiManager;

public class CameraInputController extends InputController {
    private Camera camera;
    private GuiManager guiManager;

    @Override
    public void init() {
        camera = Core.get().getRenderer().getCamera();
        guiManager = Core.get().getGuiManager();
    }

    @Override
    public boolean scroll(float y) {
        Gui gui = guiManager.getCurrentGui();
        if (gui == null || gui.isAllowCameraZoom()) {
            camera.zoom(y);
            return true;
        }

        return false;
    }

    @Override
    public void mouseMove(float x, float y) {
        if (Mouse.isRightDown()) {
            camera.moveByMouse(x, y);
        }
    }
}