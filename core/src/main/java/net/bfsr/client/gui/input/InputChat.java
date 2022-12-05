package net.bfsr.client.gui.input;

import net.bfsr.client.font.FontRenderer;
import net.bfsr.client.font.TextMeshCreator;
import net.bfsr.client.font_new.FontType;
import net.bfsr.client.gui.Scroll;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.language.Lang;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.BaseShader;
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
    //	private final GUIText chatText;
    private float offsetByScroll;
    private final Scroll scroll;
    private List<String> linesToDraw = new ArrayList<>();

    public InputChat(Vector2f pos, Scroll scroll) {
        super(null, pos, new Vector2f(225, 100), "gui.chat.typeSomething", EnumParticlePositionType.GuiInGame, new Vector2f(0.65f, 0.35f), new Vector2f(-94, 61), new Vector2f(-141, 55), EnumInputType.Any);
//		this.chatText = new GUIText("", this.fontSize, font, Transformation.getOffsetByScale(new Vector2f(pos.x, pos.y)), textColor, 0.69f, false, posType);
        this.scroll = scroll;
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
    }

    @Override
    public void setPosition(Vector2f pos) {
        super.setPosition(pos);
    }

    @Override
    public void init() {
        super.init();
        setMaxLineSize(0.31f);
//		chatText.setFontSize(fontSize);
//		chatText.updateText(chatText.getTextString());
    }

    public void addEmptyText() {
        if (!isTyping && (typingText == null || typingText.getTextString().length() == 0))
            text.updateText(Lang.getString(text.getTextString()));
    }

    @Override
    public void input() {
        super.input();

        if (Keyboard.isKeyPressed(GLFW.GLFW_KEY_ENTER)) {
            String input = typingText.getTextString().trim();
            if (input.length() > 0) {
//				addNewLineToChat(Core.getCore().getPlayerName() + ": " + input);
                Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketChatMessage(Core.getCore().getPlayerName() + ": " + input));
            } else {
                //TODO: remove test message
                Core.getCore().getNetworkManager().scheduleOutboundPacket(new PacketChatMessage(Core.getCore().getPlayerName() + ": " + "а а заа заз аз ах ах ха  х а  х а х а х а х а х а х ха"));
            }
            typingText.updateText("");
            resetCursorPosition();

        }
    }

    public void addNewLineToChat(String newLine) {
        int indexOf = newLine.indexOf(":");
        String text = newLine.substring(indexOf);

        int index = TextMeshCreator.getLineSubPosIfTooBig(text, font, startFontSize.x, 0.644f);
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

    private void updateLines() {
        int startLine = lines.size() - (int) scroll.getVisible() - (int) offsetByScroll;
        if (startLine < 0) startLine = 0;
        int maxLine = lines.size() - (int) offsetByScroll;
//		System.out.println("Line from " + startLine + " to " + maxLine);
//		lines.clear();
//		StringBuilder finalString = new StringBuilder();
        linesToDraw.clear();
        for (int i = startLine; i < maxLine; i++) {
            if (i == lines.size()) break;
            String s = lines.get(i);
            linesToDraw.add(s);
//			finalString.append(s);
//			if(i < lines.size() - 1) {
//				finalString.append(" \n");
//			}
        }

//		chatString = finalString.toString();
//		chatText.updateText(finalString.toString());
    }

    @Override
    public void render(BaseShader shader) {
//		shader.disable();
//		GL11.glPushMatrix();
//		GL11.glMatrixMode(GL11.GL_MODELVIEW);
//		GL11.glLoadIdentity();
//		GL11.glTranslatef(position.x, position.y, 0);
//		GL11.glScalef(scale.x, scale.y, 1);
//		GL11.glBegin(GL11.GL_LINE_LOOP);
//		GL11.glVertex2f(-0.5f, -0.5f);
//		GL11.glVertex2f(-0.5f, 0.5f);
//		GL11.glVertex2f(0.5f, 0.5f);
//		GL11.glVertex2f(0.5f, -0.5f);
//		GL11.glEnd();
//		GL11.glPopMatrix();
//		shader.enable();
        //TODO: почистить весь мусор, доработать чат
        int linesCount = linesToDraw.size();
//		System.out.println("lines: " + linesCount  + " offset: " + offsetByScroll);
//		updateLines();
        Vector2f pos = getPosition();
        float lineHeight = FontType.Default.getStringCache().getHeight("A");
        Vector2f pos1 = new Vector2f(-114 * Transformation.guiScale.x, 44 * Transformation.guiScale.y - linesCount * lineHeight);
//		FontRenderer.getInstance().renderString(FontType.Default, chatString, (int) (pos.x + pos1.x), (int) (pos.y + pos1.y), fontSize.x, fontSize.y, textColor.x, textColor.y, textColor.z, textColor.w, false, EnumZoomFactor.Gui, true, 0.69f);
//		chatText.setPosition(new Vector2f(pos.x + pos1.x, pos.y + pos1.y));
        FontRenderer fontRenderer = FontRenderer.getInstance();

        for (String line : linesToDraw) {
            fontRenderer.renderString(FontType.Default, line, (int) (pos.x + pos1.x), (int) (pos.y + pos1.y), fontSize.x, fontSize.y, textColor.x, textColor.y, textColor.z, textColor.w, false, EnumZoomFactor.Gui, true, 0.69f);
            pos1.y += FontType.Default.getStringCache().getHeight(line);
        }

        float cursorOffset = 5;
        float lineWidth = cursorOffset;
        float scaleOffsetX = (baseTextOffset.x - 3) * Transformation.guiScale.x;
        float scaleOffsetY = (baseTextOffset.y + 6f) * Transformation.guiScale.y;
        float cursorSize = 15f * Transformation.guiScale.y;

        if (typingText != null && typingText.getTextString().length() > 0) {
            float subLineWidth = TextMeshCreator.getLineWidth(typingText.getTextString().substring(0, cursorPosition), font, fontSize.x);
            lineWidth = subLineWidth * 1400;
            if (lineWidth == 0) lineWidth = cursorOffset;

            if (cursorPositionEnd != cursorPosition) {
                String subString = typingText.getTextString().substring(cursorPosition, cursorPositionEnd);
                subLineWidth = TextMeshCreator.getLineWidth(subString, font, fontSize.x) * 1400 - cursorOffset;
                shader.enable();
                shader.setColor(selectionColor);
                shader.disableTexture();
                shader.setModelViewMatrix(Transformation.getModelViewMatrix(pos.x + scaleOffsetX + lineWidth + subLineWidth / 2f, pos.y + scaleOffsetY, 0, subLineWidth, cursorSize, EnumZoomFactor.Gui));
                Renderer.quad.render();
                if (wasEndPos) lineWidth += subLineWidth;
            }
        }

        if (renderCursor) {
            shader.enable();
            shader.disableTexture();
            shader.setColor(getColor());
            shader.setModelViewMatrix(Transformation.getModelViewMatrix(pos.x + scaleOffsetX + lineWidth, pos.y + scaleOffsetY, 0, 1, cursorSize, EnumZoomFactor.Gui));
            Renderer.quad.render();
        }

        shader.enableTexture();
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
//		chatText.clear();
    }
}
