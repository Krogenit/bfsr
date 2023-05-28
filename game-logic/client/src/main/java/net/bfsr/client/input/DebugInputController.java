package net.bfsr.client.input;

import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.command.Command;
import net.bfsr.engine.Engine;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.network.packet.client.PacketCommand;
import net.bfsr.network.packet.client.PacketPauseGame;
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
        if (guiManager.isActive()) return false;

        if (keyboard.isKeyDown(KEY_LEFT_CONTROL)) {
            if (key == KEY_F) {
                Vector2f pos = mouse.getWorldPosition(Engine.renderer.camera);
                core.sendTCPPacket(
                        new PacketCommand(Command.SPAWN_SHIP, String.valueOf((int) pos.x), String.valueOf((int) pos.y))
                );
                return true;
            } else if (key == KEY_P) {
                core.setPaused(!core.isPaused());
                core.sendTCPPacket(new PacketPauseGame());
                return true;
            } else if (key == KEY_R) {
                core.getGlobalRenderer().reloadShaders();
                return true;
            } else if (key == KEY_B) {
                ClientSettings.SHOW_DEBUG_BOXES.setValue(!ClientSettings.SHOW_DEBUG_BOXES.getBoolean());
                return true;
            }
        }

        return false;
    }
}