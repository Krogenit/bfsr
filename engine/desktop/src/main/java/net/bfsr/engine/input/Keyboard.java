package net.bfsr.engine.input;

import static org.lwjgl.glfw.GLFW.*;

public final class Keyboard extends AbstractKeyboard {
    private long window;

    @Override
    public void init(long window) {
        this.window = window;
    }

    @Override
    public void setInputHandler(AbstractInputHandler inputHandler) {
        glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            if (action == GLFW_RELEASE) {
                inputHandler.release(key);
            } else {
                inputHandler.input(key);
            }
        });
        glfwSetCharCallback(window, (windowHandle, key) -> inputHandler.textInput(key));
    }

    @Override
    public boolean isKeyDown(int keyCode) {
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }
}