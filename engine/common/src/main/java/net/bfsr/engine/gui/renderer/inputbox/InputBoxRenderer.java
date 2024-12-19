package net.bfsr.engine.gui.renderer.inputbox;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.component.Label;
import net.bfsr.engine.gui.renderer.SimpleGuiObjectRenderer;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.opengl.GL;
import org.joml.Vector2i;
import org.joml.Vector4f;

public class InputBoxRenderer extends SimpleGuiObjectRenderer {
    private final Vector4f selectionColor = new Vector4f(0.7f, 0.8f, 1.0f, 0.5f);
    @Setter
    @Getter
    private boolean renderCursor;
    private final InputBox inputBox;
    private final GlyphsBuilder glyphsBuilder;

    private int cursorId;

    public InputBoxRenderer(InputBox inputBox) {
        super(inputBox);
        this.inputBox = inputBox;
        this.glyphsBuilder = inputBox.getLabel().getGlyphsBuilder();
    }

    @Override
    public void create() {
        createBody();

        int sceneX = guiObject.getSceneX();
        int sceneY = guiObject.getSceneY();
        int width = guiObject.getWidth();
        int height = guiObject.getHeight();

        idList.add(cursorId = guiRenderer.add(sceneX, sceneY, width, height, guiObject.getColor()));
    }

    protected void createBody() {
        idList.add(id = guiRenderer.add(guiObject.getSceneX(), guiObject.getSceneY(), guiObject.getWidth(), guiObject.getHeight(),
                guiObject.getColor()));
    }

    @Override
    protected void setLastUpdateValues() {
        super.setLastUpdateValues();
        int cursorHeight = inputBox.getCursorHeight();
        float lineWidth;
        int x = guiObject.getSceneX();
        int cursorY = guiObject.getSceneY() + guiObject.getHeight() / 2 - cursorHeight / 2;
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
                    leftStringWidth = glyphsBuilder.getWidth(string.substring(0, cursorPosition), fontSize);
                    rightStringWidth = glyphsBuilder.getWidth(string.substring(cursorPosition, cursorPositionEnd), fontSize);
                } else {
                    leftStringWidth = glyphsBuilder.getWidth(string.substring(0, cursorPositionEnd), fontSize);
                    rightStringWidth = glyphsBuilder.getWidth(string.substring(cursorPositionEnd, cursorPosition), fontSize);
                }
                guiRenderer.setLastPosition(cursorId, x + leftStringWidth + stringOffset.x, cursorY);
                guiRenderer.setLastSize(cursorId, rightStringWidth, cursorHeight);
                guiRenderer.setLastColor(cursorId, selectionColor.x, selectionColor.y, selectionColor.z, selectionColor.w);
                return;
            } else {
                lineWidth = glyphsBuilder.getWidth(string.substring(0, cursorPosition), fontSize);
            }
        } else {
            lineWidth = 0;
        }

        Vector4f stringColor = label.getColor();
        guiRenderer.setLastPosition(cursorId, x + stringOffset.x + lineWidth, cursorY);
        guiRenderer.setLastSize(cursorId, 1, cursorHeight);
        guiRenderer.setLastColor(cursorId, stringColor.x, stringColor.y, stringColor.z, stringColor.w);
    }

    public void onCursorChanged() {
        int cursorHeight = inputBox.getCursorHeight();
        float lineWidth;
        int x = guiObject.getSceneX();
        int cursorY = guiObject.getSceneY() + guiObject.getHeight() / 2 - cursorHeight / 2;
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
                    leftStringWidth = glyphsBuilder.getWidth(string.substring(0, cursorPosition), fontSize);
                    rightStringWidth = glyphsBuilder.getWidth(string.substring(cursorPosition, cursorPositionEnd), fontSize);
                } else {
                    leftStringWidth = glyphsBuilder.getWidth(string.substring(0, cursorPositionEnd), fontSize);
                    rightStringWidth = glyphsBuilder.getWidth(string.substring(cursorPositionEnd, cursorPosition), fontSize);
                }
                guiRenderer.setPosition(cursorId, x + leftStringWidth + stringOffset.x, cursorY);
                guiRenderer.setSize(cursorId, rightStringWidth, cursorHeight);
                guiRenderer.setColor(cursorId, selectionColor.x, selectionColor.y, selectionColor.z, selectionColor.w);
                return;
            } else {
                lineWidth = glyphsBuilder.getWidth(string.substring(0, cursorPosition), fontSize);
            }
        } else {
            lineWidth = 0;
        }

        Vector4f stringColor = label.getColor();
        guiRenderer.setPosition(cursorId, x + stringOffset.x + lineWidth, cursorY);
        guiRenderer.setSize(cursorId, 1, cursorHeight);
        guiRenderer.setColor(cursorId, stringColor.x, stringColor.y, stringColor.z, stringColor.w);
    }

    @Override
    public void render() {
        renderBody();

        boolean stringOffsetBelowZero = inputBox.getStringOffset().x < 0;
        if (stringOffsetBelowZero) {
            guiRenderer.render();
            renderer.glEnable(GL.GL_SCISSOR_TEST);
            renderer.glScissor(guiObject.getSceneX() + 1, Engine.renderer.getScreenHeight() - guiObject.getSceneY() - guiObject.getHeight(),
                    guiObject.getWidth() - 2, guiObject.getHeight());
        }

        renderChild();
        renderCursor();

        if (stringOffsetBelowZero) {
            guiRenderer.render();
            renderer.glDisable(GL.GL_SCISSOR_TEST);
        }
    }

    private void renderCursor() {
        Label label = inputBox.getLabel();
        int cursorPosition = inputBox.getCursorPosition();
        int cursorPositionEnd = inputBox.getCursorPositionEnd();
        String string = label.getString();
        if (string.length() > 0) {
            if (cursorPositionEnd != cursorPosition) {
                guiRenderer.addDrawCommand(cursorId);
                return;
            }
        }

        if (renderCursor) {
            guiRenderer.addDrawCommand(cursorId);
        }
    }
}
