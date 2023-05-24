package net.bfsr.client.gui.ingame;

import net.bfsr.client.Core;
import net.bfsr.client.gui.GuiManager;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.gui.input.InputChat;
import net.bfsr.client.gui.scroll.Scroll;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.AbstractRenderer;
import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.string.AbstractStringRenderer;
import net.bfsr.engine.renderer.gui.AbstractGUIRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.TextureRegister;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private final AbstractRenderer renderer = Engine.renderer;
    private final AbstractGUIRenderer guiRenderer = Engine.renderer.guiRenderer;
    private final AbstractStringRenderer stringRenderer = Engine.renderer.stringRenderer;
    private final GuiManager guiManager = Core.get().getGuiManager();
    private final TexturedGuiObject chat = new TexturedGuiObject(TextureRegister.guiChat, 320, 170) {
        @Override
        public void render() {
            super.render();
            guiRenderer.render();

            int lineX = chatInput.getX() + chatInput.getStringOffset().x;
            int lineY = 28 - scroll.getScroll();

            int chatTop = 10;
            int chatBottom = height - 30;

            renderer.glEnable(GL.GL_SCISSOR_TEST);
            renderer.glScissor(0, 38, width - 24, height - 50);

            for (int i = 0; i < lines.size(); i++) {
                String string = lines.get(i);
                int stringHeight = FontType.DEFAULT.getStringCache().getStringHeight(string, chatInput.getFontSize(), width - 40, -1);
                if (lineY >= chatTop && lineY < chatBottom || lineY + stringHeight >= chatTop && lineY + stringHeight < chatBottom) {
                    stringRenderer.render(string, FontType.DEFAULT.getStringCache(), chatInput.getFontSize(), lineX, y + lineY,
                            chatInput.getTextColor().x, chatInput.getTextColor().y, chatInput.getTextColor().z, chatInput.getTextColor().w, width - 40, -1, BufferType.GUI);
                }

                lineY += stringHeight;
            }

            guiRenderer.render();
            renderer.glDisable(GL.GL_SCISSOR_TEST);
        }
    };
    private final InputChat chatInput = new InputChat() {
        @Override
        public void updateMouseHover() {
            if (guiManager.getCurrentGui() == null) {
                super.updateMouseHover();
            }
        }
    };
    private final List<String> lines = new ArrayList<>();
    private final Scroll scroll = new Scroll() {
        @Override
        public void onMouseScroll(float y) {
            if (chatInput.isTyping()) {
                super.onMouseScroll(y);
            }
        }

        @Override
        public void updateMouseHover() {
            if (guiManager.getCurrentGui() == null) {
                super.updateMouseHover();
            }
        }
    };

    public Chat() {
        scroll.setSize(12, 99);
        scroll.setHeightResizeFunction((width, height) -> 122);
        scroll.setViewHeightResizeFunction((width, height) -> 122);
        scroll.setRepositionConsumer((width, height) -> scroll.setPosition(chat.getX() + chat.getWidth() - 24, chat.getY() + 10));
    }

    public void init(GuiInGame gui) {
        gui.registerGuiObject(chat.atBottomLeftCorner(0, -chat.getHeight()));
        gui.registerGuiObject(chatInput.atBottomLeftCorner(10, -chatInput.getHeight() - 10));
        gui.registerGuiObject(scroll);
    }

    public void addChatMessage(String message) {
        lines.add(message);
        scroll.setTotalHeight(scroll.getTotalHeight() + FontType.DEFAULT.getStringCache().getStringHeight(message, chatInput.getFontSize(), chatInput.getWidth() - 40, -1));
        scroll.scrollBottom();
    }

    public boolean onMouseLeftClick() {
        return chatInput.onMouseLeftClick();
    }

    public boolean isActive() {
        return chatInput.isTyping() || scroll.isMovingByMouse();
    }

    public void onMouseLeftRelease() {
        chatInput.onMouseLeftRelease();
    }

    public void scroll(float y) {
        chatInput.onMouseScroll(y);
    }

    public void clear() {
        lines.clear();
    }
}