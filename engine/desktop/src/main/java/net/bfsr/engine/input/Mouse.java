package net.bfsr.engine.input;

import lombok.Getter;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public final class Mouse extends AbstractMouse {
    private long window;
    private final Vector2f position = new Vector2f(), lastPosition = new Vector2f();
    @Getter
    public final long inputCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
    @Getter
    public final long defaultCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);

    @Override
    public void init(long window) {
        this.window = window;
    }

    @Override
    public void setInputHandler(AbstractInputHandler inputHandler) {
        GLFW.glfwSetCursorPosCallback(window, (windowHandle, xPos, yPos) -> {
            lastPosition.set(position.x, position.y);
            position.set((float) xPos, (float) yPos);
            inputHandler.mouseMove(position.x - lastPosition.x, position.y - lastPosition.y);
        });
        GLFW.glfwSetCursorEnterCallback(window, (windowHandle, entered) -> {});
        GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mode) -> {
            if (button < 2) {
                inputHandler.mouseInput(button, action);
            }
        });
        GLFW.glfwSetScrollCallback(window, (windowHandle, x, y) -> inputHandler.scroll((float) y));
    }

    @Override
    public void changeCursor(long cursor) {
        GLFW.glfwSetCursor(window, cursor);
    }

    @Override
    public boolean isLeftDown() {
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean isRightDown() {
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
    }

    public boolean isMiddleDown() {
        return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;
    }

    @Override
    public Vector2f getPosition() {
        return position;
    }

    @Override
    public Vector2f getWorldPosition(AbstractCamera camera) {
        return camera.getWorldVector(position);
    }
}