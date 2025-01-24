package net.bfsr.client.input;

import lombok.Getter;
import net.bfsr.client.Client;
import net.bfsr.engine.input.AbstractInputHandler;

public class InputHandler extends AbstractInputHandler {
    private final GuiInputController guiInputController;
    @Getter
    private final PlayerInputController playerInputController;
    private final CameraInputController cameraInputController;
    private final DebugInputController debugInputController;

    public InputHandler(Client client) {
        guiInputController = new GuiInputController();
        playerInputController = new PlayerInputController(client);
        cameraInputController = new CameraInputController(client, playerInputController);
        debugInputController = new DebugInputController(client);
    }

    public void update() {
        playerInputController.update();
        cameraInputController.update();
    }

    @Override
    public void input(int key) {
        if (!guiInputController.input(key)) {
            if (!playerInputController.input(key)) {
                debugInputController.input(key);
            }
        }
    }

    @Override
    public void textInput(int key) {
        guiInputController.textInput(key);
    }

    @Override
    public void release(int key) {
        playerInputController.release(key);
    }

    @Override
    protected void mouseLeftClick() {
        if (!guiInputController.mouseLeftClick()) {
            playerInputController.mouseLeftClick();
        }
    }

    @Override
    protected void mouseLeftRelease() {
        if (!guiInputController.mouseLeftRelease()) {
            playerInputController.mouseLeftRelease();
        }
    }

    @Override
    protected void mouseRightClick() {
        if (!guiInputController.mouseRightClick()) {
            playerInputController.mouseRightClick();
        }
    }

    @Override
    protected void mouseRightRelease() {
        guiInputController.mouseRightRelease();
    }

    @Override
    public void mouseMove(float dx, float dy) {
        if (!guiInputController.mouseMove(dx, dy)) {
            cameraInputController.mouseMove(dx, dy);
        }
    }

    @Override
    public void scroll(float y) {
        if (!guiInputController.scroll(y)) {
            cameraInputController.scroll(y);
        }
    }
}