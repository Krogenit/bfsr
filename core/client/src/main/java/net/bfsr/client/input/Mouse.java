package net.bfsr.client.input;

import net.bfsr.client.camera.Camera;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public final class Mouse {
    private static long window;
    private static final Vector2f POSITION = new Vector2f(), LAST_POSITION = new Vector2f();
    public static final long INPUT_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
    public static final long DEFAULT_CURSOR = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);

    public void init(long window, InputHandler inputHandler) {
        Mouse.window = window;

        GLFW.glfwSetCursorPosCallback(window, (windowHandle, xPos, yPos) -> {
            LAST_POSITION.set(POSITION.x, POSITION.y);
            POSITION.set((float) xPos, (float) yPos);
            inputHandler.mouseMove(POSITION.x - LAST_POSITION.x, POSITION.y - LAST_POSITION.y);
        });
        GLFW.glfwSetCursorEnterCallback(window, (windowHandle, entered) -> {});
        GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mode) -> {
            if (button < 2) {
                inputHandler.mouseInput(button, action);
            }
        });
        GLFW.glfwSetScrollCallback(window, (windowHandle, x, y) -> inputHandler.scroll((float) y));
    }

    public static void changeCursor(long cursor) {
        GLFW.glfwSetCursor(window, cursor);
    }

    public static boolean isLeftDown() {
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    public static boolean isRightDown() {
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
    }

    public static boolean isMiddleDown() {
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;
    }

    public static Vector2f getPosition() {
        return POSITION;
    }

    public static Vector2f getWorldPosition(Camera camera) {
        return camera.getWorldVector(POSITION);
    }
}