package net.bfsr.engine.input;

import lombok.RequiredArgsConstructor;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

@RequiredArgsConstructor
public final class Keyboard extends AbstractKeyboard {
    private final long window;

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

    @Override
    public void clear() {}
}