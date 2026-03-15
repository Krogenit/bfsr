package net.bfsr.client.gui.input;

import net.bfsr.client.Client;
import net.bfsr.client.gui.GuiStyle;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.network.packet.common.PacketChatMessage;

public class ChatInput extends InputBox {
    public ChatInput(int width, int height, int inputOffsetX) {
        super(width, height, "", Client.get().getLanguageManager().getString("gui.chat.typeSomething"), 14, inputOffsetX, 0);
        setMaxLineSize(width - 8);
        GuiStyle.setupTransparentInputBox(this);
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