package net.bfsr.client.input;

import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.network.packet.client.PacketCommand;
import net.bfsr.client.network.packet.client.PacketPauseGame;
import net.bfsr.client.settings.Option;
import net.bfsr.command.Command;
import net.bfsr.engine.Engine;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import org.joml.Vector2f;

import static net.bfsr.engine.input.Keys.*;

public class DebugInputController extends InputController {
    private GuiManager guiManager;
    private Core core;
    private final AbstractMouse mouse = Engine.mouse;
    private final AbstractKeyboard keyboard = Engine.keyboard;

    @Override
    public void init() {
        core = Core.get();
        guiManager = core.getGuiManager();
    }

    @Override
    public boolean input(int key) {
        if (guiManager.getCurrentGui() == null && !guiManager.getGuiInGame().isActive()) {
            if (key == KEY_F) {
                Vector2f pos = mouse.getWorldPosition(Engine.renderer.camera);

                if (core.getNetworkSystem() != null)
                    core.sendTCPPacket(new PacketCommand(Command.SPAWN_SHIP, String.valueOf(pos.x), String.valueOf(pos.y)));

                return true;
            } else if (keyboard.isKeyDown(KEY_LEFT_CONTROL)) {
                if (key == KEY_P) {
                    Engine.setPaused(!Engine.isPaused());
                    core.sendTCPPacket(new PacketPauseGame());
                    return true;
                } else if (key == KEY_R) {
                    core.getWorldRenderer().reloadShaders();
                    return true;
                } else if (key == KEY_B) {
                    Option.SHOW_DEBUG_BOXES.setValue(!Option.SHOW_DEBUG_BOXES.getBoolean());
                    return true;
                }
            }
        }

        return false;
    }
}