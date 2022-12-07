package net.bfsr.client.input;

import net.bfsr.client.gui.Gui;
import net.bfsr.core.Core;
import net.bfsr.world.WorldClient;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {
    private static long window;

    public static void init(long win) {
        window = win;

        glfwSetKeyCallback(window, (window1, key, scancode, action, mods) -> {
            if (action != GLFW_RELEASE) {
                WorldClient world = Core.getCore().getWorld();
                if (world != null) world.input(key);

                guiInput(gui -> gui.input(key));
            }
        });

        glfwSetCharCallback(window, (window, key) -> guiInput(gui -> gui.textInput(key)));
    }

    private static void guiInput(Consumer<Gui> guiConsumer) {
        Gui gui = Core.getCore().getCurrentGui();
        if (gui != null) {
            guiConsumer.accept(gui);
        }

        guiConsumer.accept(Core.getCore().getGuiInGame());
    }

    public static boolean isKeyDown(int keyCode) {
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }
}
