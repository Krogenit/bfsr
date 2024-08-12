package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.input.ChatInput;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.gui.component.TexturedRectangle;
import net.bfsr.engine.gui.renderer.RectangleTexturedRenderer;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class Chat extends TexturedRectangle {
    private final ChatInput chatInput = new ChatInput();
    private final ScrollPane scrollPane;
    private final int chatMessagesOffsetY = 8;

    public Chat() {
        super(TextureRegister.guiChat, 320, 170);
        scrollPane = new ScrollPane(width - 29, 122, 12);

        add(chatInput.atBottomLeft(10, -chatInput.getHeight() - 10));
        add(scrollPane.atTopLeft(18, 10));
        setRenderer(new RectangleTexturedRenderer(this, TextureRegister.guiChat));
    }

    public void addChatMessage(String message) {
        int y = scrollPane.getTotalHeight() + (scrollPane.getTotalHeight() == 0 ? chatMessagesOffsetY : 0);
        scrollPane.add(new Label(Font.NOTOSANS_REGULAR_FT, message, 0, y, chatInput.getFontSize()).setMaxWidth(chatInput.getWidth() - 20)
                .atTopLeft(0, y));
        scrollPane.scrollBottom();
    }

    public boolean isActive() {
        return chatInput.isTyping() || scrollPane.isMovingByMouse();
    }

    @Override
    public void clear() {
        super.clear();
        scrollPane.clear();
    }
}