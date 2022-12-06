package net.bfsr.client.input;

import net.bfsr.client.camera.Camera;
import net.bfsr.client.gui.Gui;
import net.bfsr.core.Core;
import net.bfsr.world.WorldClient;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class Mouse {
    private static final Vector2f pos = new Vector2f(), prevPos = new Vector2f();
    private static final Vector2f deltaPos = new Vector2f();

    private static long window;

    private static boolean isActive;

    private static final MouseConsumer[][] mouseConsumers = new MouseConsumer[2][2];

    private interface MouseConsumer {
        void input(int action);
    }

    public static void init(long window) {
        Mouse.window = window;

        mouseConsumers[0][1] = action -> guiAndWorldInput(Gui::onMouseLeftClicked, WorldClient::onMouseLeftClicked);
        mouseConsumers[0][0] = action -> guiAndWorldInput(Gui::onMouseLeftRelease, WorldClient::onMouseLeftRelease);
        mouseConsumers[1][1] = action -> guiAndWorldInput(Gui::onMouseRightClicked, WorldClient::onMouseRightClicked);
        mouseConsumers[1][0] = action -> guiInput(Gui::onMouseRightRelease);

        GLFW.glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> pos.set((float) xpos, (float) ypos));
        GLFW.glfwSetCursorEnterCallback(window, (windowHandle, entered) -> isActive = entered);
        GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mode) -> {
            if (button < 2) {
                mouseConsumers[button][action].input(action);
            }
        });
        GLFW.glfwSetScrollCallback(window, (windowHandle, x, y) -> {
            float floatY = (float) y;
            guiInput(gui -> gui.onMouseScroll(floatY));
            Core.getCore().getRenderer().getCamera().scroll(floatY);
        });
    }

    private static void guiInput(Consumer<Gui> consumer) {
        Gui gui = Core.getCore().getCurrentGui();
        if (gui != null) {
            consumer.accept(gui);
        }

        consumer.accept(Core.getCore().getGuiInGame());
    }

    private static void guiAndWorldInput(Consumer<Gui> guiConsumer, Consumer<WorldClient> worldConsumer) {
        guiInput(guiConsumer);
        WorldClient world = Core.getCore().getWorld();
        if (world != null) worldConsumer.accept(world);
    }

    public static void updateState() {
        deltaPos.x = 0;
        deltaPos.y = 0;

        if (prevPos.x > 0 && prevPos.y > 0 && isActive) {
            double deltax = pos.x - prevPos.x;
            double deltay = pos.y - prevPos.y;
            boolean rotateX = deltax != 0;
            boolean rotateY = deltay != 0;
            if (rotateX) {
                deltaPos.x = (float) deltax;
            }
            if (rotateY) {
                deltaPos.y = (float) deltay;
            }
        }

        prevPos.x = pos.x;
        prevPos.y = pos.y;
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

    public static Vector2f getDelta() {
        return deltaPos;
    }

    public static Vector2f getPosition() {
        return pos;
    }

    public static Vector2f getWorldPosition(Camera cam) {
        return cam.getWorldVector(pos);
    }
}
