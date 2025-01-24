package net.bfsr.client.input;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.Client;
import net.bfsr.client.settings.ClientSettings;
import net.bfsr.command.Command;
import net.bfsr.engine.Engine;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.network.packet.client.PacketCommand;
import net.bfsr.network.packet.client.PacketPauseGame;
import org.joml.Vector2f;

import static net.bfsr.engine.input.Keys.KEY_B;
import static net.bfsr.engine.input.Keys.KEY_F;
import static net.bfsr.engine.input.Keys.KEY_LEFT_CONTROL;
import static net.bfsr.engine.input.Keys.KEY_P;
import static net.bfsr.engine.input.Keys.KEY_R;

@RequiredArgsConstructor
class DebugInputController extends InputController {
    private final Client client;
    private final AbstractMouse mouse = Engine.getMouse();
    private final AbstractKeyboard keyboard = Engine.getKeyboard();
    private final AbstractRenderer renderer = Engine.getRenderer();

    @Override
    public boolean input(int key) {
        if (keyboard.isKeyDown(KEY_LEFT_CONTROL)) {
            if (key == KEY_F) {
                Vector2f pos = mouse.getWorldPosition(renderer.getCamera());
                client.sendTCPPacket(new PacketCommand(Command.SPAWN_SHIP, String.valueOf((int) pos.x), String.valueOf((int) pos.y)));
                return true;
            } else if (key == KEY_P) {
                client.setPaused(!client.isPaused());
                client.sendTCPPacket(new PacketPauseGame());
                return true;
            } else if (key == KEY_R) {
                renderer.reloadShaders();
                return true;
            } else if (key == KEY_B) {
                ClientSettings.SHOW_DEBUG_BOXES.setValue(!ClientSettings.SHOW_DEBUG_BOXES.getBoolean());
                return true;
            }
        }

        return false;
    }
}