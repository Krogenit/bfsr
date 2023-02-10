package net.bfsr.client.input;

import net.bfsr.client.gui.Gui;
import net.bfsr.core.Core;
import net.bfsr.world.WorldClient;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

public final class Keyboard {
    private static long window;

    public static void init(long win) {
        window = win;

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action != GLFW_RELEASE) {
                WorldClient world = Core.get().getWorld();
                if (world != null) world.input(key);

                guiInput(gui -> gui.input(key));
            }
        });

        glfwSetCharCallback(window, (window, key) -> guiInput(gui -> gui.textInput(key)));
    }

    private static void guiInput(Consumer<Gui> guiConsumer) {
        Gui gui = Core.get().getCurrentGui();
        if (gui != null) {
            guiConsumer.accept(gui);
        }

        guiConsumer.accept(Core.get().getGuiInGame());
    }

    public static boolean isKeyDown(int keyCode) {
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }
}
