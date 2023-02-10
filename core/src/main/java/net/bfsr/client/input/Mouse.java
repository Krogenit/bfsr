package net.bfsr.client.input;

import net.bfsr.client.camera.Camera;
import net.bfsr.client.gui.Gui;
import net.bfsr.core.Core;
import net.bfsr.world.WorldClient;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public final class Mouse {
    private static long window;
    private static final Vector2f position = new Vector2f(), lastPosition = new Vector2f();
    private static final MouseConsumer[][] mouseConsumers = new MouseConsumer[2][2];

    public static void init(long window) {
        Mouse.window = window;

        mouseConsumers[0][1] = action -> guiAndWorldInput(Gui::onMouseLeftClicked, WorldClient::onMouseLeftClicked);
        mouseConsumers[0][0] = action -> guiAndWorldInput(Gui::onMouseLeftRelease, WorldClient::onMouseLeftRelease);
        mouseConsumers[1][1] = action -> guiAndWorldInput(Gui::onMouseRightClicked, WorldClient::onMouseRightClicked);
        mouseConsumers[1][0] = action -> guiInput(Gui::onMouseRightRelease);

        GLFW.glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            lastPosition.set(position.x, position.y);
            position.set((float) xpos, (float) ypos);
            Core.get().getRenderer().getCamera().mouseMove(position.x - lastPosition.x, position.y - lastPosition.y);
        });
        GLFW.glfwSetCursorEnterCallback(window, (windowHandle, entered) -> {});
        GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mode) -> {
            if (button < 2) {
                mouseConsumers[button][action].input(action);
            }
        });
        GLFW.glfwSetScrollCallback(window, (windowHandle, x, y) -> {
            float floatY = (float) y;
            guiInput(gui -> gui.onMouseScroll(floatY));
            Core.get().getRenderer().getCamera().scroll(floatY);
        });
    }

    private static void guiInput(Consumer<Gui> consumer) {
        Gui gui = Core.get().getCurrentGui();
        if (gui != null) {
            consumer.accept(gui);
        }

        consumer.accept(Core.get().getGuiInGame());
    }

    private static void guiAndWorldInput(Consumer<Gui> guiConsumer, Consumer<WorldClient> worldConsumer) {
        guiInput(guiConsumer);
        WorldClient world = Core.get().getWorld();
        if (world != null) worldConsumer.accept(world);
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
        return position;
    }

    public static Vector2f getWorldPosition(Camera camera) {
        return camera.getWorldVector(position);
    }
}
