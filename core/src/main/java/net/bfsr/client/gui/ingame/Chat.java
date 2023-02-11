package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.input.InputChat;
import net.bfsr.client.renderer.texture.TextureRegister;

public class Chat {
    private final TexturedGuiObject chat = new TexturedGuiObject(TextureRegister.guiChat);

    private final InputChat chatInput = new InputChat();

    public void init(GuiInGame gui) {
        int scaleX = 320;
        int scaleY = 170;
        chat.setSize(scaleX, scaleY);
        chat.atBottomLeftCorner(0, -scaleY);
        gui.registerGuiObject(chat);

        int chatWidth = 320;
        int chatHeight = 170;
        chatInput.setSize(chatWidth, chatHeight);
        chatInput.atBottomLeftCorner(0, -chatHeight);
        gui.registerGuiObject(chatInput);
    }

    public void addChatMessage(String message) {
        chatInput.addNewLineToChat(message);
    }

    public void onMouseLeftClick() {
        chatInput.onMouseLeftClick();
    }

    public boolean isActive() {
        return chatInput.isActive();
    }

    public void onMouseLeftRelease() {
        chatInput.onMouseLeftRelease();
    }

    public void scroll(float y) {
        chatInput.scroll(y);
    }
}
