package net.bfsr.client.input;

import static org.lwjgl.glfw.GLFW.*;

public final class Keyboard {
    private static long window;

    public void init(long window, InputHandler inputHandler) {
        Keyboard.window = window;

        glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            if (action == GLFW_RELEASE) {
                inputHandler.release(key);
            } else {
                inputHandler.input(key);
            }
        });
        glfwSetCharCallback(window, (windowHandle, key) -> inputHandler.textInput(key));
    }

    public static boolean isKeyDown(int keyCode) {
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }
}