package net.bfsr.client.input;

import lombok.Getter;

public class InputHandler {
    private final Mouse mouse = new Mouse();
    private final Keyboard keyboard = new Keyboard();
    private final MouseConsumer[][] mouseConsumers = new MouseConsumer[2][2];
    private final GuiInputController guiInputController = new GuiInputController();
    @Getter
    private final PlayerInputController playerInputController = new PlayerInputController();
    private final CameraInputController cameraInputController = new CameraInputController();
    private final DebugInputController debugInputController = new DebugInputController();

    public InputHandler() {
        mouseConsumers[0][1] = action -> onMouseLeftClick();
        mouseConsumers[0][0] = action -> onMouseLeftRelease();
        mouseConsumers[1][1] = action -> onMouseRightClick();
        mouseConsumers[1][0] = action -> onMouseRightRelease();
    }

    public void init(long window) {
        mouse.init(window, this);
        keyboard.init(window, this);

        guiInputController.init();
        playerInputController.init();
        cameraInputController.init();
        debugInputController.init();
    }

    public void update() {
        playerInputController.update();
    }

    public void input(int key) {
        if (!guiInputController.input(key)) {
            playerInputController.input(key);
            debugInputController.input(key);
        }
    }

    public void textInput(int key) {
        guiInputController.textInput(key);
    }

    public void release(int key) {
        playerInputController.release(key);
    }

    public void onMouseLeftClick() {
        if (!guiInputController.onMouseLeftClick()) {
            playerInputController.onMouseLeftClick();
        }
    }

    public void onMouseLeftRelease() {
        if (!guiInputController.onMouseLeftRelease()) {
            playerInputController.onMouseLeftRelease();
        }
    }

    public void onMouseRightClick() {
        if (!guiInputController.onMouseRightClick()) {
            playerInputController.onMouseRightClick();
        }
    }

    public void onMouseRightRelease() {
        guiInputController.onMouseRightRelease();
    }

    public void mouseInput(int button, int action) {
        mouseConsumers[button][action].input(action);
    }

    public void mouseMove(float x, float y) {
        cameraInputController.mouseMove(x, y);
    }

    public void scroll(float y) {
        guiInputController.scroll(y);
        cameraInputController.scroll(y);
    }
}