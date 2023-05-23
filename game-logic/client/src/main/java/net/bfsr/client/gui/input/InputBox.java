package net.bfsr.client.gui.input;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.Core;
import net.bfsr.client.font.StringObject;
import net.bfsr.client.gui.GuiObject;
import net.bfsr.client.gui.GuiObjectsHandler;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.engine.Engine;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.font.FontType;
import net.bfsr.engine.renderer.font.StringCache;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.sound.SoundRegistry;
import net.bfsr.engine.sound.SoundSource;
import net.bfsr.util.RunnableUtils;
import org.joml.Vector2i;
import org.joml.Vector4f;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static net.bfsr.engine.input.Keys.*;

public class InputBox extends TexturedGuiObject {
    protected final StringObject stringObject;
    private final StringObject emptyStringObject;
    @Getter
    protected boolean typing;
    private boolean renderCursor;
    private int cursorTimer;
    private final int cursorMaxTimer = 30;
    private int cursorPosition, cursorPositionEnd;
    protected final StringCache stringCache;
    @Getter
    protected int fontSize;
    @Setter
    private int maxLineSize;
    @Getter
    protected final Vector2i stringOffset;
    @Getter
    protected final Vector4f textColor = new Vector4f(1.0f);
    private final Vector4f selectionColor = new Vector4f(0.7f, 0.8f, 1.0f, 0.5f);
    @Setter
    private int cursorHeight;
    @Setter
    private SoundRegistry collideSound;
    private final int maxStringOffsetX;
    private long lastSelectTime;
    private final long doubleClickTime = 400;
    private long lastDoubleClickTime;
    private final long timeBeforeSelectionAvailable = 500;
    private final int stringOffsetMovingThreshold = 2;
    @Setter
    private Runnable onUnselectedRunnable = RunnableUtils.EMPTY_RUNNABLE;
    private final AbstractKeyboard keyboard = Engine.keyboard;
    private final AbstractMouse mouse = Engine.mouse;

    public InputBox(TextureRegister texture, int width, int height, String string, StringCache stringCache, int fontSize, int stringOffsetX, int stringOffsetY, int maxLineSize) {
        super(texture, width, height);
        this.stringCache = stringCache;
        this.fontSize = fontSize;
        this.stringOffset = new Vector2i(stringOffsetX, stringCache.getCenteredYOffset(string, height, fontSize) + stringOffsetY);

        this.maxStringOffsetX = stringOffset.x;
        int stringX = x + stringOffset.x;
        int stringY = y + stringOffset.y;

        this.stringObject = new StringObject(stringCache, fontSize, textColor.x, textColor.y, textColor.z, textColor.w).setPosition(stringX, stringY);
        this.emptyStringObject = new StringObject(stringCache, string, fontSize, textColor.x, textColor.y, textColor.z, textColor.w).setPosition(stringX, stringY).compile();

        this.maxLineSize = maxLineSize;
        this.cursorHeight = (int) (height / 1.7f);
    }

    public InputBox(int width, int height, String string, StringCache stringCache, int fontSize, int stringOffsetX, int stringOffsetY, int maxLineSize) {
        this(null, width, height, string, stringCache, fontSize, stringOffsetX, stringOffsetY, maxLineSize);
    }

    public InputBox(int width, int height, String string, FontType fontType, int fontSize, int stringOffsetX, int stringOffsetY, int maxLineSize) {
        this(null, width, height, string, fontType.getStringCache(), fontSize, stringOffsetX, stringOffsetY, maxLineSize);
    }

    public InputBox(TextureRegister texture, int width, int height, String string, FontType fontType, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(texture, width, height, string, fontType.getStringCache(), fontSize, stringOffsetX, stringOffsetY, (int) (width / 1.2f));
    }

    public InputBox(TextureRegister texture, int width, int height, String string, StringCache stringCache, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(texture, width, height, string, stringCache, fontSize, stringOffsetX, stringOffsetY, (int) (width / 1.2f));
    }

    public InputBox(TextureRegister texture, int width, int height, String string, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(texture, width, height, string, FontType.XOLONIUM, fontSize, stringOffsetX, stringOffsetY);
    }

    public InputBox(int width, int height, String string, FontType fontType, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(null, width, height, string, fontType, fontSize, stringOffsetX, stringOffsetY);
    }

    public InputBox(int width, int height, String string, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(width, height, string, FontType.XOLONIUM, fontSize, stringOffsetX, stringOffsetY);
    }

    public InputBox(TextureRegister texture, String string, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(texture, 300, 50, string, fontSize, stringOffsetX, stringOffsetY);
    }

    @Override
    public boolean onMouseLeftClick() {
        boolean mouseHover = isMouseHover();

        if (mouseHover) {
            long now = System.currentTimeMillis();
            if (now - lastSelectTime <= doubleClickTime) {
                selectAll();
                lastDoubleClickTime = now;
            } else {
                enableTyping();
            }
            lastSelectTime = now;
        } else {
            if (typing) {
                disableTyping();
                onUnselected();
            }
        }

        return mouseHover;
    }

    @Override
    public void onOtherGuiObjectMouseLeftClick(GuiObject guiObject) {
        if (typing) {
            disableTyping();
            onUnselected();
        }
    }

    @Override
    public void onOtherGuiObjectMouseRightClick(GuiObject guiObject) {
        if (typing) {
            disableTyping();
            onUnselected();
        }
    }

    private void onUnselected() {
        onUnselectedRunnable.run();
    }

    @Override
    public void input(int key) {
        if (!typing) return;

        if (keyboard.isKeyDown(KEY_LEFT_CONTROL)) {
            if (key == KEY_A) {
                selectAll();
            } else if (key == KEY_C) {
                if (cursorPositionEnd != cursorPosition) {
                    String string;
                    if (cursorPosition < cursorPositionEnd) {
                        string = stringObject.getString().substring(cursorPosition, cursorPositionEnd);
                    } else {
                        string = stringObject.getString().substring(cursorPositionEnd, cursorPosition);
                    }
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(string), null);
                }
            } else if (key == KEY_V) {
                Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        insertString((String) transferable.getTransferData(DataFlavor.stringFlavor));
                    } catch (UnsupportedFlavorException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        if (key == KEY_LEFT) {
            if (keyboard.isKeyDown(KEY_LEFT_SHIFT)) {
                cursorPositionEnd--;
                if (cursorPositionEnd < 0) cursorPositionEnd = 0;
                checkCursorOutOfBoundsPosition(cursorPositionEnd);
            } else {
                if (cursorPosition != cursorPositionEnd) {
                    cursorPosition = cursorPositionEnd;
                } else {
                    cursorPosition--;
                    if (cursorPosition < 0) cursorPosition = 0;
                    cursorPositionEnd = cursorPosition;
                    checkCursorOutOfBoundsPosition(cursorPosition);
                }
                showCursor();
            }
        } else if (key == KEY_RIGHT) {
            if (keyboard.isKeyDown(KEY_LEFT_SHIFT)) {
                cursorPositionEnd++;
                int lineWidth = stringObject.getString().length();
                if (cursorPositionEnd > lineWidth) cursorPositionEnd = lineWidth;
                checkCursorOutOfBoundsPosition(cursorPositionEnd);
            } else {
                if (cursorPosition != cursorPositionEnd) {
                    cursorPosition = cursorPositionEnd;
                } else {
                    cursorPosition++;
                    int lineWidth = stringObject.getString().length();
                    if (cursorPosition > lineWidth) cursorPosition = lineWidth;
                    cursorPositionEnd = cursorPosition;
                    checkCursorOutOfBoundsPosition(cursorPosition);
                }
                showCursor();
            }
        } else if (key == KEY_BACKSPACE) {
            String prevString = stringObject.getString();

            if (prevString.length() > 0) {
                String newString;
                if (cursorPositionEnd != cursorPosition) {
                    if (cursorPosition < cursorPositionEnd) {
                        newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPositionEnd);
                        cursorPositionEnd = cursorPosition;
                    } else {
                        newString = prevString.substring(0, cursorPositionEnd) + prevString.substring(cursorPosition);
                        cursorPosition = cursorPositionEnd;
                    }
                } else {
                    if (cursorPosition == prevString.length()) {
                        newString = prevString.substring(0, cursorPosition - 1);
                        cursorPosition--;
                        cursorPositionEnd--;
                    } else if (cursorPosition > 0) {
                        newString = prevString.substring(0, cursorPosition - 1) + prevString.substring(cursorPosition);
                        cursorPosition--;
                        cursorPositionEnd--;
                    } else newString = prevString;
                }
                stringObject.setString(newString);
                checkCursorOutOfBoundsPosition(cursorPosition);
            }
        } else if (key == KEY_DELETE) {
            String prevString = stringObject.getString();

            if (prevString.length() > 0) {
                int sl = prevString.length();
                String newString;
                if (cursorPositionEnd != cursorPosition) {
                    if (cursorPosition < cursorPositionEnd) {
                        newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPositionEnd);
                        cursorPositionEnd = cursorPosition;
                    } else {
                        newString = prevString.substring(0, cursorPositionEnd) + prevString.substring(cursorPosition);
                        cursorPosition = cursorPositionEnd;
                    }
                } else {
                    if (cursorPosition < sl) // If we have chars after cursor
                        newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPosition + 1);
                    else newString = prevString;// Else save string
                }
                stringObject.setString(newString);
            }
        } else if (key == KEY_ENTER) {
            disableTyping();
            onUnselected();
        }
    }

    private void checkCursorOutOfBoundsPosition(int cursorPosition) {
        checkCursorOutOfBoundsPosition(stringObject.getStringCache().getStringWidth(stringObject.getString().substring(0, cursorPosition), stringObject.getFontSize()),
                stringOffsetMovingThreshold);
    }

    private void checkCursorOutOfBoundsPosition(int localPosX, int viewOffsetX) {
        int cursorPositionX = x + localPosX + stringOffset.x;
        int minCursorPositionX = x + viewOffsetX;
        int maxCursorPosition = x + width - viewOffsetX;
        int stringPosition = x + stringOffset.x;

        if (cursorPositionX < minCursorPositionX && stringPosition < minCursorPositionX) {
            int diff = minCursorPositionX - cursorPositionX;
            stringOffset.x += diff;
            stringObject.setPosition(stringObject.getX() + diff, stringObject.getYForScroll());

            int maxStringOffset = x + maxStringOffsetX;
            if (stringPosition + diff > maxStringOffset) {
                diff = stringPosition + diff - maxStringOffset;
                stringOffset.x -= diff;
                stringObject.setPosition(stringObject.getX() - diff, stringObject.getYForScroll());
            }
        }

        if (cursorPositionX > maxCursorPosition && stringPosition <= x + stringOffset.x) {
            int diff = cursorPositionX - maxCursorPosition;

            stringOffset.x -= diff;
            stringObject.setPosition(stringObject.getX() - diff, stringObject.getYForScroll());

            int maxStringOffset = x + width - maxStringOffsetX;
            if (stringPosition + stringObject.getWidth() - diff < maxStringOffset) {
                diff = maxStringOffset - (stringPosition + stringObject.getWidth() - diff);
                stringOffset.x += diff;
                stringObject.setPosition(stringObject.getX() + diff, stringObject.getYForScroll());
            }
        }
    }

    private void selectAll() {
        cursorPosition = 0;
        cursorPositionEnd = stringObject.getString().length();
    }

    private void setCursorPositionByMouse() {
        cursorPosition = cursorPositionEnd = stringObject.getCursorPositionInLine(mouse.getPosition().x - x - stringOffset.x);
    }

    @Override
    public void textInput(int key) {
        if (!typing) return;

        char keyName = (char) key;
        insertString(String.valueOf(keyName));
    }

    private void insertString(String string) {
        String prevString = "";
        if (stringObject.getString().length() > 0) {
            prevString = stringObject.getString();
        }

        if (cursorPosition != cursorPositionEnd) {
            String newString;
            if (cursorPosition < cursorPositionEnd) {
                newString = prevString.substring(0, cursorPosition) + string + prevString.substring(cursorPositionEnd);
                cursorPositionEnd = cursorPosition;
            } else {
                newString = prevString.substring(0, cursorPositionEnd) + string + prevString.substring(cursorPosition);
                cursorPosition = cursorPositionEnd;
            }
            if (stringObject.getStringCache().getStringWidth(newString, stringObject.getFontSize()) < maxLineSize) {
                stringObject.setString(newString);
                cursorPosition = cursorPositionEnd += string.length();
                checkCursorOutOfBoundsPosition(cursorPosition);
            }
        } else {
            String newString = prevString.substring(0, cursorPosition) + string + prevString.substring(cursorPosition);
            if (stringObject.getStringCache().getStringWidth(newString, stringObject.getFontSize()) < maxLineSize) {
                stringObject.setString(newString);

                cursorPosition = cursorPositionEnd += string.length();
                checkCursorOutOfBoundsPosition(cursorPosition);
            }
        }
    }

    @Override
    public void onMouseHover() {
        mouse.changeCursor(mouse.getInputCursor());
        if (collideSound != null) {
            Core.get().getSoundManager().play(new SoundSource(collideSound));
        }
    }

    @Override
    public void onMouseStopHover() {
        mouse.changeCursor(mouse.getDefaultCursor());
    }

    @Override
    public void onUnregistered(GuiObjectsHandler gui) {
        super.onUnregistered(gui);
        mouse.changeCursor(mouse.getDefaultCursor());
    }

    @Override
    public void update() {
        super.update();
        emptyStringObject.update();
        stringObject.update();

        if (typing) {
            if (mouse.isLeftDown() && System.currentTimeMillis() - lastDoubleClickTime > timeBeforeSelectionAvailable) {
                float selectionPositionX = mouse.getPosition().x - x - stringOffset.x;
                cursorPositionEnd = stringObject.getCursorPositionInLine(selectionPositionX);
                checkCursorOutOfBoundsPosition((int) selectionPositionX, 10);
            }

            if (cursorPositionEnd == cursorPosition) {
                if (cursorTimer > 0) {
                    cursorTimer--;
                }

                if (cursorTimer == 0) {
                    renderCursor = !renderCursor;
                    cursorTimer = cursorMaxTimer;
                }
            } else {
                renderCursor = false;
            }
        }
    }

    @Override
    public void render() {
        super.render();

        if (stringOffset.x < 0) {
            Engine.renderer.guiRenderer.render();
            Engine.renderer.glEnable(GL.GL_SCISSOR_TEST);
            Engine.renderer.glScissor(x + 1, Engine.renderer.getScreenHeight() - y - height, width - 2, height);
        }

        renderString();
        renderSelectionAndCursor();

        if (stringOffset.x < 0) {
            Engine.renderer.guiRenderer.render();
            Engine.renderer.glDisable(GL.GL_SCISSOR_TEST);
        }
    }

    void renderString() {
        if (stringObject.getString().isEmpty()) {
            emptyStringObject.render();
        } else {
            stringObject.render();
        }
    }

    void renderSelectionAndCursor() {
        float lineWidth;
        int lastCursorY = lastY + height / 2 - cursorHeight / 2;
        int cursorY = y + height / 2 - cursorHeight / 2;
        if (stringObject.getString().length() > 0) {
            if (cursorPositionEnd != cursorPosition) {
                int leftStringWidth;
                int rightStringWidth;
                if (cursorPosition < cursorPositionEnd) {
                    leftStringWidth = stringObject.getStringCache().getStringWidth(stringObject.getString().substring(0, cursorPosition), fontSize);
                    rightStringWidth = stringObject.getStringCache().getStringWidth(stringObject.getString().substring(cursorPosition, cursorPositionEnd), fontSize);
                } else {
                    leftStringWidth = stringObject.getStringCache().getStringWidth(stringObject.getString().substring(0, cursorPositionEnd), fontSize);
                    rightStringWidth = stringObject.getStringCache().getStringWidth(stringObject.getString().substring(cursorPositionEnd, cursorPosition), fontSize);
                }
                Engine.renderer.guiRenderer.add(lastX + stringOffset.x + leftStringWidth, lastCursorY, x + leftStringWidth + stringOffset.x, cursorY,
                        rightStringWidth, cursorHeight, selectionColor.x, selectionColor.y, selectionColor.z, selectionColor.w);
                return;
            } else {
                lineWidth = stringCache.getStringWidth(stringObject.getString().substring(0, cursorPosition), fontSize);
            }
        } else {
            lineWidth = 0;
        }

        if (renderCursor) {
            Vector4f stringColor = stringObject.getColor();
            Engine.renderer.guiRenderer.add(lastX + stringOffset.x + lineWidth, lastCursorY, x + stringOffset.x + lineWidth, cursorY, 1, cursorHeight,
                    stringColor.x, stringColor.y, stringColor.z, stringColor.w);
        }
    }

    private void showCursor() {
        renderCursor = true;
        cursorTimer = cursorMaxTimer;
    }

    private void hideCursor() {
        renderCursor = false;
        cursorTimer = cursorMaxTimer;
    }

    public InputBox setString(String string) {
        this.stringObject.setString(string);
        return this;
    }

    public void enableTyping() {
        setCursorPositionByMouse();
        showCursor();
        Core.get().getSoundManager().play(new SoundSource(SoundRegistry.buttonClick));
        typing = true;
    }

    private void disableTyping() {
        hideCursor();
        cursorPosition = cursorPositionEnd = 0;
        typing = false;
    }

    @Override
    public InputBox setPosition(int x, int y) {
        super.setPosition(x, y);
        int stringX = x + stringOffset.x;
        int stringY = y + stringOffset.y;
        this.stringObject.setPosition(stringX, stringY);
        this.emptyStringObject.setPosition(stringX, stringY);
        return this;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        int stringY = y + stringOffset.y;
        this.stringObject.setY(stringY);
        this.emptyStringObject.setY(stringY);
    }

    @Override
    public InputBox setTextColor(float r, float g, float b, float a) {
        emptyStringObject.setColor(r, g, b, a);
        stringObject.setColor(r, g, b, a);
        return this;
    }

    public InputBox setTextColor(Vector4f color) {
        emptyStringObject.setColor(color);
        stringObject.setColor(color);
        return this;
    }

    void resetCursorPosition() {
        cursorPosition = cursorPositionEnd = 0;
    }

    public String getString() {
        return stringObject.getString();
    }

    @Override
    public void clear() {
        resetCursorPosition();
    }
}