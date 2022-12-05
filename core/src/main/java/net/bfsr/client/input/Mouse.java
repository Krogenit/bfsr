package net.bfsr.client.input;

import net.bfsr.client.camera.Camera;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class Mouse {
    static Vector2f pos, prevPos, scroll;
    static Vector2f deltaPos;

    static int isLeftDown, isRightDown, isMiddleDown;
    static int isLeftRelease, isRightRelease, isMiddleRelease;
    static int isLeftStartDown, isRightStartDown, isMiddleStartDown;

    static boolean isActive;

    public static void init(long window) {
        GLFW.glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            pos.x = (float) xpos;
            pos.y = (float) ypos;
        });

        GLFW.glfwSetCursorEnterCallback(window, (windowHandle, entered) -> {
            isActive = entered;
        });
        GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mode) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (!isLeftDown() && action == 1) isLeftStartDown = 1;
                isLeftDown = action;
                if (!isLeftDown()) isLeftRelease = 1;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (!isRightDown() && action == 1) isRightStartDown = 1;
                isRightDown = action;
                if (!isRightDown()) isRightRelease = 1;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                if (!isMiddleDown() && action == 1) isMiddleStartDown = 1;
                isMiddleDown = action;
                if (!isMiddleDown()) isMiddleRelease = 1;
            }
        });
        GLFW.glfwSetScrollCallback(window, (windowHandle, xoffset, yoffset) -> {
            scroll.x += xoffset;
            scroll.y += yoffset;
        });

        scroll = new Vector2f();
        pos = new Vector2f();
        prevPos = new Vector2f();
        deltaPos = new Vector2f();
    }

    public void onMouseScroll(double x, double y) {
        scroll.x += x;
        scroll.y += y;
    }

    public static void postUpdateState() {
        isLeftStartDown = 0;
        isRightStartDown = 0;
        isMiddleStartDown = 0;
        isLeftRelease = 0;
        isRightRelease = 0;
        isMiddleRelease = 0;
        scroll.x = 0;
        scroll.y = 0;
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

    public static boolean isLeftRelease() {
        return isLeftRelease == 1;
    }

    public static boolean isRightRelease() {
        return isRightRelease == 1;
    }

    public static boolean isLeftDown() {
        return isLeftDown == 1;
    }

    public static boolean isRightDown() {
        return isRightDown == 1;
    }

    public static boolean isMiddleDown() {
        return isMiddleDown == 1;
    }

    public static boolean isLeftStartDown() {
        return isLeftStartDown == 1;
    }

    public static boolean isRightStartDown() {
        return isRightStartDown == 1;
    }

    public static boolean isMiddleStartDown() {
        return isMiddleStartDown == 1;
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

    public static Vector2f getScroll() {
        return scroll;
    }
}
