package net.bfsr.client.gui.ingame;

import net.bfsr.client.gui.GuiStyle;
import net.bfsr.client.gui.input.ChatInput;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.component.Rectangle;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.renderer.font.glyph.Font;
import org.joml.Vector3f;

public class Chat extends Rectangle {
    private final Font font = Engine.getFontManager().getDefaultFont();

    private final int chatMessagesOffsetX = 4;
    private final int chatMessagesOffsetY = 1;
    private final ChatInput chatInput = new ChatInput(318, 25, chatMessagesOffsetX);
    private final ScrollPane scrollPane;

    public Chat() {
        super(320, 170);
        GuiStyle.setupTransparentRectangle(this);
        scrollPane = new ScrollPane(width, 144, 10);
        Vector3f uiColor = GuiStyle.UI_COLOR;
        scrollPane.setScrollColor(uiColor.x, uiColor.y, uiColor.z, 0.1f);
        scrollPane.setScrollHoverColor(uiColor.x, uiColor.y, uiColor.z, 0.2f);

        add(chatInput.atBottom(0, 0));
        add(scrollPane.atTop(0, -1));
    }

    public void addChatMessage(String message) {
        scrollPane.add(new Label(font, message, chatInput.getFontSize()).setMaxWidth(chatInput.getWidth() - 20)
                .atTopLeft(chatMessagesOffsetX, -scrollPane.getTotalHeight() - chatMessagesOffsetY));
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