package net.bfsr.client.input;

import net.bfsr.client.core.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.network.packet.client.PacketCommand;
import net.bfsr.client.network.packet.client.PacketPauseGame;
import net.bfsr.client.settings.Option;
import net.bfsr.command.Command;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class DebugInputController extends InputController {
    private GuiManager guiManager;
    private Core core;

    @Override
    public void init() {
        core = Core.get();
        guiManager = core.getGuiManager();
    }

    public boolean input(int key) {
        if (guiManager.getCurrentGui() == null && !guiManager.getGuiInGame().isActive()) {
            if (key == GLFW_KEY_F) {
                Vector2f pos = Mouse.getWorldPosition(Core.get().getRenderer().getCamera());

                if (core.getNetworkSystem() != null)
                    core.sendTCPPacket(new PacketCommand(Command.SPAWN_SHIP, String.valueOf(pos.x), String.valueOf(pos.y)));

                return true;
            } else if (Keyboard.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
                if (key == GLFW_KEY_P) {
                    core.setPaused(!core.isPaused());
                    core.sendTCPPacket(new PacketPauseGame());
                    return true;
                } else if (key == GLFW_KEY_R) {
                    core.getRenderer().reloadShaders();
                    return true;
                } else if (key == GLFW_KEY_B) {
                    Option.SHOW_DEBUG_BOXES.setValue(!Option.SHOW_DEBUG_BOXES.getBoolean());
                    return true;
                }
            }
        }

        return false;
    }
}