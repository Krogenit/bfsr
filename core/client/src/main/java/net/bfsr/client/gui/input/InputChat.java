package net.bfsr.client.gui.input;

import net.bfsr.client.core.Core;
import net.bfsr.client.language.Lang;
import net.bfsr.client.network.packet.common.PacketChatMessage;
import org.lwjgl.glfw.GLFW;

public class InputChat extends InputBox {
    public InputChat() {
        super(298, 25, Lang.getString("gui.chat.typeSomething"), 16, 4, 0);
        setCursorHeight(16);
        setMaxLineSize(290);
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ENTER) {
            String input = stringObject.getString().trim();
            if (input.length() > 0) {
                Core.get().sendTCPPacket(new PacketChatMessage(Core.get().getPlayerName() + ": " + input));
            }
            stringObject.setString("");
            resetCursorPosition();
        }
    }

    @Override
    public void render() {
        renderString();
        renderSelectionAndCursor();
    }
}