package net.bfsr.engine.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.camera.AbstractCamera;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

@RequiredArgsConstructor
public final class Mouse extends AbstractMouse {
    private final long window;
    private final AbstractRenderer renderer;

    @Getter
    private final Vector2f screenPosition = new Vector2f();
    private final Vector2f lastPosition = new Vector2f();
    @Getter
    private final Vector2i guiPosition = new Vector2i();
    @Getter
    public final long inputCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_IBEAM_CURSOR);
    @Getter
    public final long defaultCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);

    @Override
    public void setInputHandler(AbstractInputHandler inputHandler) {
        GLFW.glfwSetCursorPosCallback(window, (windowHandle, xPos, yPos) -> {
            lastPosition.set(screenPosition.x, screenPosition.y);
            screenPosition.set((float) xPos, (float) yPos);
            guiPosition.set((int) xPos, renderer.getScreenHeight() - (int) yPos);
            inputHandler.mouseMove(screenPosition.x - lastPosition.x, screenPosition.y - lastPosition.y);
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
    public Vector2f getWorldPosition(AbstractCamera camera) {
        return camera.getWorldVector(guiPosition);
    }

    @Override
    public void clear() {
        GLFW.glfwDestroyCursor(inputCursor);
        GLFW.glfwDestroyCursor(defaultCursor);
    }
}