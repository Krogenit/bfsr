package net.bfsr.client.gui.input;

import net.bfsr.client.gui.scroll.Scroll;
import net.bfsr.client.language.Lang;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.shader.ShaderProgram;
import net.bfsr.core.Core;
import net.bfsr.math.EnumZoomFactor;
import net.bfsr.math.Transformation;
import net.bfsr.network.packet.common.PacketChatMessage;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class InputChat extends InputBox {
    private final List<String> lines = new ArrayList<>();
    private float offsetByScroll;
    private final Scroll scroll = new Scroll();
    private final List<String> linesToDraw = new ArrayList<>();

    public InputChat() {
        super(null, 0, 0, 225, 100, "gui.chat.typeSomething", 10, -94, 61);
        setMaxLineSize(0.31f);

        scroll.setPosition(0, 0);
        scroll.setSize(12, 99);
        scroll.setViewHeightResizeFunction((width, height) -> 100);
    }

    public void addEmptyText() {
        if (!typing && stringObject.getString().isEmpty()) stringObject.update(Lang.getString(stringObject.getString()));
    }

    @Override
    public void input(int key) {
        super.input(key);

        if (key == GLFW.GLFW_KEY_ENTER) {
            String input = stringObject.getString().trim();
            if (input.length() > 0) {
                Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketChatMessage(Core.getCore().getPlayerName() + ": " + input));
            } else {
                //TODO: remove test message
                Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketChatMessage(Core.getCore().getPlayerName() + ": " + "а а заа заз аз ах ах ха  х а  х а х а х а х а х а х ха"));
            }
            stringObject.update("");
            resetCursorPosition();
        }
    }

    public void addNewLineToChat(String newLine) {
        int indexOf = newLine.indexOf(':');
        String text = newLine.substring(indexOf);

        int index = font.getStringCache().sizeStringToWidth(text, fontSize, 64);
        if (index != 0 && index < text.length()) {
            String sub = text.substring(0, index);
            lines.add(newLine.substring(0, indexOf) + sub);
            text = text.substring(index);
            lines.add(text);
        } else {
            lines.add(newLine.substring(0, indexOf) + text);
        }
        updateLines();
    }

    @Override
    public void update() {
        super.update();

        if (typing) {
            scroll.setTotalHeight(lines.size());
        }
    }

    private void updateLines() {
        int startLine = lines.size() - scroll.getScroll() - (int) offsetByScroll;
        if (startLine < 0) startLine = 0;
        int maxLine = lines.size() - (int) offsetByScroll;
        linesToDraw.clear();
        for (int i = startLine; i < maxLine; i++) {
            if (i == lines.size()) break;
            String s = lines.get(i);
            linesToDraw.add(s);
        }
    }

    @Override
    public void render(BaseShader shader) {
        //TODO: почистить весь мусор, доработать чат
        int linesCount = linesToDraw.size();
        float lineHeight = FontType.DEFAULT.getStringCache().getHeight("A", 14);
        Vector2f pos1 = new Vector2f(-114 * Transformation.guiScale.x, 44 * Transformation.guiScale.y - linesCount * lineHeight);

        for (String string : linesToDraw) {
            Core.getCore().getRenderer().getStringRenderer().render(string, FontType.DEFAULT.getStringCache(), fontSize, (int) (x + pos1.x), (int) (y + pos1.y), textColor.x, textColor.y, textColor.z, textColor.w);
            pos1.y += FontType.DEFAULT.getStringCache().getHeight(string, 14);
        }

        float cursorOffset = 5;
        float lineWidth = cursorOffset;
        float scaleOffsetX = (stringOffset.x - 3) * Transformation.guiScale.x;
        float scaleOffsetY = (stringOffset.y + 6.0f) * Transformation.guiScale.y;
        float cursorSize = 15.0f * Transformation.guiScale.y;

        if (stringObject.getString().length() > 0) {
            float subLineWidth = font.getStringCache().getStringWidth(stringObject.getString().substring(0, cursorPosition), fontSize);
            lineWidth = subLineWidth * 1400;
            if (lineWidth == 0) lineWidth = cursorOffset;

            if (cursorPositionEnd != cursorPosition) {
                String subString = stringObject.getString().substring(cursorPosition, cursorPositionEnd);
                subLineWidth = font.getStringCache().getStringWidth(subString, fontSize) - cursorOffset;
                shader.enable();
                shader.setColor(selectionColor.x, selectionColor.y, selectionColor.z, selectionColor.w);
                shader.disableTexture();
                shader.setModelMatrix(Transformation.getModelViewMatrix(x + scaleOffsetX + lineWidth + subLineWidth / 2.0f, y + scaleOffsetY, 0, subLineWidth, cursorSize, EnumZoomFactor.Gui)
                        .get(ShaderProgram.MATRIX_BUFFER));
                Renderer.centeredQuad.renderIndexed();
                if (leftToRightSelection) lineWidth += subLineWidth;
            }
        }

        if (renderCursor) {
            shader.enable();
            shader.disableTexture();
            shader.setColor(color.x, color.y, color.z, color.w);
            shader.setModelMatrix(Transformation.getModelViewMatrix(x + scaleOffsetX + lineWidth, y + scaleOffsetY, 0, 1, cursorSize, EnumZoomFactor.Gui).get(ShaderProgram.MATRIX_BUFFER));
            Renderer.centeredQuad.renderIndexed();
        }

        shader.enableTexture();

        scroll.render(shader);
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
            setOffsetByScroll(scroll.getScroll());
            scroll.setSize(12, 99);
            scroll.setPosition(0, 0);
        }
    }

    public List<String> getLines() {
        return lines;
    }

    public void setOffsetByScroll(float offsetByScroll) {
        this.offsetByScroll = offsetByScroll;
        updateLines();
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
