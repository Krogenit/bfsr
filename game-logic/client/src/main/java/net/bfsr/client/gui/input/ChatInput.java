package net.bfsr.client.gui.input;

import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.language.Lang;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.network.packet.common.PacketChatMessage;

import static net.bfsr.engine.input.Keys.KEY_ENTER;

public class ChatInput extends InputBox {
    private final GuiManager guiManager = Core.get().getGuiManager();
    
    public ChatInput() {
        super(298, 25, Lang.getString("gui.chat.typeSomething"), 16, 4, 0);
        setCursorHeight(16);
        setMaxLineSize(290);
    }

    @Override
    public boolean input(int key) {
        if (!super.input(key)) {
            return false;
        }

        if (key == KEY_ENTER) {
            String input = stringObject.getString().trim();
            if (input.length() > 0) {
                Core.get().sendTCPPacket(new PacketChatMessage(Core.get().getPlayerName() + ": " + input));
            }
            stringObject.setString("");
            resetCursorPosition();
        }

        return true;
    }

    @Override
    public void updateMouseHover() {
        if (guiManager.noGui()) {
            super.updateMouseHover();
        }
    }

    @Override
    public void render() {
        renderString();
        renderSelectionAndCursor();
    }
}