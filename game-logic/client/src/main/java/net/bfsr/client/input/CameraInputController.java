package net.bfsr.client.input;

import net.bfsr.client.Core;
import net.bfsr.client.gui.Gui;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.settings.Option;
import net.bfsr.engine.Engine;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.camera.AbstractCamera;

public class CameraInputController extends InputController {
    private final AbstractCamera camera = Engine.renderer.camera;
    private GuiManager guiManager;
    private final AbstractMouse mouse = Engine.mouse;

    @Override
    public void init() {
        guiManager = Core.get().getGuiManager();
    }

    @Override
    public boolean scroll(float y) {
        Gui gui = guiManager.getCurrentGui();
        if (gui == null || gui.isAllowCameraZoom()) {
            camera.zoom(y * Option.CAMERA_ZOOM_SPEED.getFloat());
            return true;
        }

        return false;
    }

    @Override
    public void mouseMove(float x, float y) {
        if (mouse.isRightDown()) {
            camera.moveByMouse(x, y);
        }
    }
}