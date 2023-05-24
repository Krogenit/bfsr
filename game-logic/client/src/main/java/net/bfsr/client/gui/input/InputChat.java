package net.bfsr.client.gui.input;

import net.bfsr.client.Core;
import net.bfsr.client.language.Lang;
import net.bfsr.network.packet.common.PacketChatMessage;

import static net.bfsr.engine.input.Keys.KEY_ENTER;

public class InputChat extends InputBox {
    public InputChat() {
        super(298, 25, Lang.getString("gui.chat.typeSomething"), 16, 4, 0);
        setCursorHeight(16);
        setMaxLineSize(290);
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == KEY_ENTER) {
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