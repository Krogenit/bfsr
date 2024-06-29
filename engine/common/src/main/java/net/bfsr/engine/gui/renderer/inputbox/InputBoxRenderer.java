package net.bfsr.engine.gui.renderer.inputbox;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;
import net.bfsr.engine.renderer.opengl.GL;
import org.joml.Vector2i;
import org.joml.Vector4f;

public class InputBoxRenderer extends GuiObjectRenderer {
    private final Vector4f selectionColor = new Vector4f(0.7f, 0.8f, 1.0f, 0.5f);
    @Setter
    @Getter
    private boolean renderCursor;
    private final InputBox inputBox;

    public InputBoxRenderer(InputBox inputBox) {
        super(inputBox);
        this.inputBox = inputBox;
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        renderBody(lastX, lastY, x, y, width, height);

        boolean stringOffsetBelowZero = inputBox.getStringOffset().x < 0;
        if (stringOffsetBelowZero) {
            guiRenderer.render();
            renderer.glEnable(GL.GL_SCISSOR_TEST);
            renderer.glScissor(x + 1, Engine.renderer.getScreenHeight() - y - height, width - 2, height);
        }

        renderChild(lastX, lastY, x, y);
        renderSelectionAndCursor(lastX, lastY, x, y, height);

        if (stringOffsetBelowZero) {
            guiRenderer.render();
            renderer.glDisable(GL.GL_SCISSOR_TEST);
        }
    }

    protected void renderBody(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor);
        } else {
            guiRenderer.add(lastX, lastY, x, y, width, height, color);
        }
    }

    private void renderSelectionAndCursor(int lastX, int lastY, int x, int y, int height) {
        int cursorHeight = inputBox.getCursorHeight();
        float lineWidth;
        int lastCursorY = lastY + height / 2 - cursorHeight / 2;
        int cursorY = y + height / 2 - cursorHeight / 2;
        Label label = inputBox.getLabel();
        int fontSize = label.getFontSize();
        int cursorPosition = inputBox.getCursorPosition();
        int cursorPositionEnd = inputBox.getCursorPositionEnd();
        Vector2i stringOffset = inputBox.getStringOffset();
        String string = label.getString();
        if (string.length() > 0) {
            if (cursorPositionEnd != cursorPosition) {
                int leftStringWidth;
                int rightStringWidth;
                if (cursorPosition < cursorPositionEnd) {
                    leftStringWidth = label.getStringCache().getStringWidth(string.substring(0, cursorPosition), fontSize);
                    rightStringWidth = label.getStringCache()
                            .getStringWidth(string.substring(cursorPosition, cursorPositionEnd), fontSize);
                } else {
                    leftStringWidth = label.getStringCache().getStringWidth(string.substring(0, cursorPositionEnd), fontSize);
                    rightStringWidth = label.getStringCache()
                            .getStringWidth(string.substring(cursorPositionEnd, cursorPosition), fontSize);
                }
                guiRenderer.add(lastX + stringOffset.x + leftStringWidth, lastCursorY, x + leftStringWidth + stringOffset.x, cursorY,
                        rightStringWidth, cursorHeight, selectionColor.x, selectionColor.y, selectionColor.z, selectionColor.w);
                return;
            } else {
                lineWidth = label.getStringCache().getStringWidth(string.substring(0, cursorPosition), fontSize);
            }
        } else {
            lineWidth = 0;
        }

        if (renderCursor) {
            Vector4f stringColor = label.getColor();
            guiRenderer.add(lastX + stringOffset.x + lineWidth, lastCursorY, x + stringOffset.x + lineWidth, cursorY, 1, cursorHeight,
                    stringColor.x, stringColor.y, stringColor.z, stringColor.w);
        }
    }
}
