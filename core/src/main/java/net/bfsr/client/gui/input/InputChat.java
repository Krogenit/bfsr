package net.bfsr.client.gui.input;

import net.bfsr.client.gui.scroll.Scroll;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.instanced.BufferType;
import net.bfsr.core.Core;
import net.bfsr.network.packet.common.PacketChatMessage;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class InputChat extends InputBox {
    private final List<String> lines = new ArrayList<>();
    private final Scroll scroll = new Scroll();

    public InputChat() {
        super(null, 0, 0, 225, 100, Lang.getString("gui.chat.typeSomething"), 16, 18, 62);
        scroll.setSize(12, 99);
        scroll.setRepositionConsumer((width, height) -> scroll.setPosition(x + this.width - 22, y + 10));
        scroll.setHeightResizeConsumer((width, height) -> 122);
        scroll.setViewHeightResizeFunction((width, height) -> 122);
        setCursorHeight(16);
        setMaxLineSize(240);
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ENTER) {
            String input = stringObject.getString().trim();
            if (input.length() > 0) {
                Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketChatMessage(Core.getCore().getPlayerName() + ": " + input));
            }
            stringObject.update("");
            resetCursorPosition();
        }
    }

    public void addNewLineToChat(String newLine) {
        lines.add(newLine);
        scroll.setTotalHeight(scroll.getTotalHeight() + FontType.DEFAULT.getStringCache().getStringHeight(newLine, fontSize, width - 40, -1));
        scroll.scrollBottom();
    }

    @Override
    public void update() {
        super.update();
        scroll.update();
    }

    @Override
    public void render() {
        renderString();

        int lineX = stringOffset.x;
        int lineY = 28 - scroll.getScroll();

        int chatTop = 10;
        int chatBottom = height - 30;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, 38, width, height - 50);
        for (int i = 0; i < lines.size(); i++) {
            String string = lines.get(i);
            int stringHeight = FontType.DEFAULT.getStringCache().getStringHeight(string, fontSize, width - 40, -1);
            if (lineY >= chatTop && lineY < chatBottom || lineY + stringHeight >= chatTop && lineY + stringHeight < chatBottom) {
                Core.getCore().getRenderer().getStringRenderer().render(string, FontType.DEFAULT.getStringCache(), fontSize, x + lineX, y + lineY, textColor.x, textColor.y,
                        textColor.z, textColor.w, width - 40, -1, BufferType.GUI);
            }

            lineY += stringHeight;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        renderSelectionAndCursor();

        scroll.render();
    }

    @Override
    public void onMouseLeftClick() {
        scroll.onMouseLeftClick();
        super.onMouseLeftClick();
    }

    @Override
    public void onMouseLeftRelease() {
        scroll.onMouseLeftRelease();
    }

    @Override
    public void scroll(float y) {
        if (typing) {
            scroll.scroll(y);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        scroll.resize(width, height);
    }

    @Override
    public void clear() {
        super.clear();
        lines.clear();
    }

    public boolean isActive() {
        return typing || scroll.isMovingByMouse();
    }
}
