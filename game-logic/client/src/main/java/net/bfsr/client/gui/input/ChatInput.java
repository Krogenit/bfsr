package net.bfsr.client.gui.input;

import net.bfsr.client.Client;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.renderer.inputbox.EmptyInputRenderer;
import net.bfsr.network.packet.common.PacketChatMessage;

public class ChatInput extends InputBox {
    public ChatInput() {
        super(298, 25, Lang.getString("gui.chat.typeSomething"), 14, 4, 0);
        setCursorHeight(16);
        setMaxLineSize(290);
        setRenderer(new EmptyInputRenderer(this));
    }

    @Override
    protected void onEnterPressed() {
        super.onEnterPressed();

        String input = label.getString().trim();
        if (input.length() > 0) {
            Client.get().sendTCPPacket(new PacketChatMessage(Client.get().getPlayerName() + ": " + input));
        }

        label.setString("");
        resetCursorPosition();
        enableTyping();
    }

    public int getFontSize() {
        return label.getFontSize();
    }
}