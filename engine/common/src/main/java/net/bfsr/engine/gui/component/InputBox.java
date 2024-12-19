package net.bfsr.engine.gui.component;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;
import net.bfsr.engine.gui.renderer.inputbox.InputBoxRenderer;
import net.bfsr.engine.gui.renderer.inputbox.TexturedInputBoxRenderer;
import net.bfsr.engine.input.AbstractKeyboard;
import net.bfsr.engine.input.AbstractMouse;
import net.bfsr.engine.renderer.font.Font;
import net.bfsr.engine.renderer.font.glyph.GlyphsBuilder;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.util.RunnableUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector4f;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static net.bfsr.engine.input.Keys.KEY_A;
import static net.bfsr.engine.input.Keys.KEY_BACKSPACE;
import static net.bfsr.engine.input.Keys.KEY_C;
import static net.bfsr.engine.input.Keys.KEY_DELETE;
import static net.bfsr.engine.input.Keys.KEY_ENTER;
import static net.bfsr.engine.input.Keys.KEY_ESCAPE;
import static net.bfsr.engine.input.Keys.KEY_LEFT;
import static net.bfsr.engine.input.Keys.KEY_LEFT_CONTROL;
import static net.bfsr.engine.input.Keys.KEY_LEFT_SHIFT;
import static net.bfsr.engine.input.Keys.KEY_RIGHT;
import static net.bfsr.engine.input.Keys.KEY_V;

public class InputBox extends GuiObject {
    @Getter
    protected final Label label;
    private final Label emptyLabel;
    @Getter
    protected boolean typing;
    private int cursorTimer;
    private final int cursorMaxTimer = 30;
    @Getter
    private int cursorPosition, cursorPositionEnd;
    @Setter
    private int maxLineSize;
    @Getter
    protected final Vector2i stringOffset;
    @Getter
    protected final Vector4f textColor = new Vector4f(1.0f);
    @Setter
    @Getter
    private int cursorHeight;
    private final int maxStringOffsetX;
    private long lastSelectTime;
    private final long doubleClickTime = 300;
    private long lastDoubleClickTime;
    private final long timeBeforeSelectionAvailable = 500;
    private final int stringOffsetMovingThreshold = 2;
    @Setter
    private Runnable onUnselectedRunnable = RunnableUtils.EMPTY_RUNNABLE;
    private final AbstractKeyboard keyboard = Engine.keyboard;
    private final AbstractMouse mouse = Engine.mouse;
    private InputBoxRenderer renderer;
    private final GlyphsBuilder glyphsBuilder;

    public InputBox(int width, int height, String string, Font font, int fontSize, int stringOffsetX, int stringOffsetY,
                    int maxLineSize) {
        super(width, height);
        this.stringOffset = new Vector2i(stringOffsetX, font.getGlyphsBuilder().getCenteredOffsetY(string, height, fontSize) +
                stringOffsetY);
        this.maxStringOffsetX = stringOffset.x;
        this.glyphsBuilder = font.getGlyphsBuilder();

        int stringX = stringOffset.x;
        int stringY = stringOffset.y;
        this.label = new Label(font, fontSize, textColor.x, textColor.y, textColor.z, textColor.w).atBottomLeft(stringX,
                stringY);
        add(this.emptyLabel = new Label(font, string, fontSize, textColor.x, textColor.y, textColor.z, textColor.w).atBottomLeft(
                stringX, stringY));

        this.maxLineSize = maxLineSize;
        this.cursorHeight = (int) (height / 1.7f);
        this.renderer = new InputBoxRenderer(this);
    }

    public InputBox(TextureRegister texture, int width, int height, String string, Font font, int fontSize, int stringOffsetX,
                    int stringOffsetY) {
        this(width, height, string, font, fontSize, stringOffsetX, stringOffsetY,
                (int) (width / 1.2f));
        setRenderer(renderer = new TexturedInputBoxRenderer(this, texture));
    }

    public InputBox(TextureRegister texture, int width, int height, String string, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(texture, width, height, string, Font.XOLONIUM_FT, fontSize, stringOffsetX, stringOffsetY);
    }

    public InputBox(int width, int height, String string, Font font, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(width, height, string, font, fontSize, stringOffsetX, stringOffsetY, (int) (width / 1.2f));
    }

    public InputBox(int width, int height, String string, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(width, height, string, Font.XOLONIUM_FT, fontSize, stringOffsetX, stringOffsetY);
    }

    public InputBox(TextureRegister texture, String string, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(texture, 300, 50, string, fontSize, stringOffsetX, stringOffsetY);
    }

    @Override
    public GuiObject mouseLeftClick() {
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

        return super.mouseLeftClick();
    }

    @Nullable
    @Override
    public GuiObject mouseRightClick() {
        if (!mouseHover && typing) {
            disableTyping();
            onUnselected();
        }

        return super.mouseLeftClick();
    }

    private void onUnselected() {
        onUnselectedRunnable.run();
    }

    @Override
    public boolean input(int key) {
        if (!typing) return false;

        if (keyboard.isKeyDown(KEY_LEFT_CONTROL)) {
            if (key == KEY_A) {
                selectAll();
            } else if (key == KEY_C) {
                if (cursorPositionEnd != cursorPosition) {
                    String string;
                    if (cursorPosition < cursorPositionEnd) {
                        string = label.getString().substring(cursorPosition, cursorPositionEnd);
                    } else {
                        string = label.getString().substring(cursorPositionEnd, cursorPosition);
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
                if (cursorPosition == cursorPositionEnd) {
                    cursorPosition--;
                    if (cursorPosition < 0) cursorPosition = 0;
                    cursorPositionEnd = cursorPosition;
                    checkCursorOutOfBoundsPosition(cursorPosition);
                } else {
                    cursorPosition = cursorPositionEnd;
                }
                showCursor();
            }
        } else if (key == KEY_RIGHT) {
            if (keyboard.isKeyDown(KEY_LEFT_SHIFT)) {
                cursorPositionEnd++;
                int lineWidth = label.getString().length();
                if (cursorPositionEnd > lineWidth) cursorPositionEnd = lineWidth;
                checkCursorOutOfBoundsPosition(cursorPositionEnd);
            } else {
                if (cursorPosition == cursorPositionEnd) {
                    cursorPosition++;
                    int lineWidth = label.getString().length();
                    if (cursorPosition > lineWidth) cursorPosition = lineWidth;
                    cursorPositionEnd = cursorPosition;
                    checkCursorOutOfBoundsPosition(cursorPosition);
                } else {
                    cursorPosition = cursorPositionEnd;
                }
                showCursor();
            }
        } else if (key == KEY_BACKSPACE) {
            String prevString = label.getString();

            if (prevString.length() > 0) {
                String newString;
                if (cursorPositionEnd == cursorPosition) {
                    if (cursorPosition == prevString.length()) {
                        newString = prevString.substring(0, cursorPosition - 1);
                        cursorPosition--;
                        cursorPositionEnd--;
                    } else if (cursorPosition > 0) {
                        newString = prevString.substring(0, cursorPosition - 1) + prevString.substring(cursorPosition);
                        cursorPosition--;
                        cursorPositionEnd--;
                    } else newString = prevString;
                } else {
                    if (cursorPosition < cursorPositionEnd) {
                        newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPositionEnd);
                        cursorPositionEnd = cursorPosition;
                    } else {
                        newString = prevString.substring(0, cursorPositionEnd) + prevString.substring(cursorPosition);
                        cursorPosition = cursorPositionEnd;
                    }
                }
                label.setString(newString);
                onStringChanged();
                checkCursorOutOfBoundsPosition(cursorPosition);
            }
        } else if (key == KEY_DELETE) {
            String prevString = label.getString();

            if (prevString.length() > 0) {
                int sl = prevString.length();
                String newString;
                if (cursorPositionEnd == cursorPosition) {
                    if (cursorPosition < sl) // If we have chars after cursor
                        newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPosition + 1);
                    else newString = prevString;// Else save string
                } else {
                    if (cursorPosition < cursorPositionEnd) {
                        newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPositionEnd);
                        cursorPositionEnd = cursorPosition;
                    } else {
                        newString = prevString.substring(0, cursorPositionEnd) + prevString.substring(cursorPosition);
                        cursorPosition = cursorPositionEnd;
                    }
                }
                label.setString(newString);
                onStringChanged();
            }
        } else if (key == KEY_ENTER) {
            onEnterPressed();
        } else if (key == KEY_ESCAPE) {
            disableTyping();
        }

        renderer.onCursorChanged();

        return true;
    }

    protected void onEnterPressed() {
        disableTyping();
        onUnselected();
    }

    private void onStringChanged() {
        if (!label.getString().isEmpty()) {
            showString();
        } else if (!typing) {
            showBlankString();
        }
    }

    private void checkCursorOutOfBoundsPosition(int cursorPosition) {
        checkCursorOutOfBoundsPosition(glyphsBuilder.getWidth(label.getString().substring(0, cursorPosition), label.getFontSize()),
                stringOffsetMovingThreshold);
    }

    private void checkCursorOutOfBoundsPosition(int localPosX, int viewOffsetX) {
        int cursorPositionX = localPosX + stringOffset.x;
        int maxCursorPosition = width - viewOffsetX;

        if (cursorPositionX < viewOffsetX && stringOffset.x < viewOffsetX) {
            stringOffset.x += viewOffsetX - cursorPositionX;
            if (stringOffset.x > maxStringOffsetX) {
                stringOffset.x = maxStringOffsetX;
            }
        }

        int minStringOffsetX = -(label.getWidth() - width + maxStringOffsetX);
        if (cursorPositionX > maxCursorPosition && minStringOffsetX < 0) {
            stringOffset.x -= cursorPositionX - maxCursorPosition;
            if (stringOffset.x < minStringOffsetX) {
                stringOffset.x = minStringOffsetX;
            }
        }

        label.atBottomLeft(stringOffset.x, stringOffset.y);
        label.updatePositionAndSize();
    }

    private void selectAll() {
        cursorPosition = 0;
        cursorPositionEnd = label.getString().length();
        renderer.onCursorChanged();
    }

    private void setCursorPositionByMouse() {
        cursorPosition = cursorPositionEnd = label.getCursorPositionInLine(mouse.getPosition().x - getSceneX() - stringOffset.x);
        renderer.onCursorChanged();
    }

    @Override
    public boolean textInput(int key) {
        if (!typing) return false;

        char keyName = (char) key;
        insertString(String.valueOf(keyName));
        return true;
    }

    private void insertString(String string) {
        String prevString = "";
        if (label.getString().length() > 0) {
            prevString = label.getString();
        }

        if (cursorPosition == cursorPositionEnd) {
            String newString = prevString.substring(0, cursorPosition) + string + prevString.substring(cursorPosition);
            if (glyphsBuilder.getWidth(newString, label.getFontSize()) < maxLineSize) {
                label.setString(newString);

                cursorPosition = cursorPositionEnd += string.length();
                checkCursorOutOfBoundsPosition(cursorPosition);
                onStringChanged();
            }
        } else {
            String newString;
            if (cursorPosition < cursorPositionEnd) {
                newString = prevString.substring(0, cursorPosition) + string + prevString.substring(cursorPositionEnd);
                cursorPositionEnd = cursorPosition;
            } else {
                newString = prevString.substring(0, cursorPositionEnd) + string + prevString.substring(cursorPosition);
                cursorPosition = cursorPositionEnd;
            }
            if (glyphsBuilder.getWidth(newString, label.getFontSize()) < maxLineSize) {
                label.setString(newString);
                cursorPosition = cursorPositionEnd += string.length();
                checkCursorOutOfBoundsPosition(cursorPosition);
                onStringChanged();
            }
        }

        renderer.onCursorChanged();
    }

    @Override
    public void onMouseHover() {
        super.onMouseHover();
        mouse.changeCursor(mouse.getInputCursor());
    }

    @Override
    public void onMouseStopHover() {
        super.onMouseStopHover();
        mouse.changeCursor(mouse.getDefaultCursor());
    }

    @Override
    public void remove() {
        super.remove();
        mouse.changeCursor(mouse.getDefaultCursor());
    }

    @Override
    public void update() {
        super.update();

        if (typing) {
            if (mouse.isLeftDown() && System.currentTimeMillis() - lastDoubleClickTime > timeBeforeSelectionAvailable) {
                float selectionPositionX = mouse.getPosition().x - getSceneX() - stringOffset.x;
                cursorPositionEnd = label.getCursorPositionInLine(selectionPositionX);
                checkCursorOutOfBoundsPosition((int) selectionPositionX, 10);
                renderer.onCursorChanged();
            }

            if (cursorPositionEnd == cursorPosition) {
                if (cursorTimer > 0) {
                    cursorTimer--;
                }

                if (cursorTimer == 0) {
                    renderer.setRenderCursor(!renderer.isRenderCursor());
                    cursorTimer = cursorMaxTimer;
                }
            } else {
                renderer.setRenderCursor(false);
            }
        }
    }

    private void showCursor() {
        renderer.setRenderCursor(true);
        cursorTimer = cursorMaxTimer;
    }

    private void hideCursor() {
        renderer.setRenderCursor(false);
        cursorTimer = cursorMaxTimer;
    }

    public InputBox setString(String string) {
        this.label.setString(string);
        onStringChanged();
        return this;
    }

    private void showString() {
        remove(emptyLabel);
        addIfAbsent(label);
    }

    private void showBlankString() {
        remove(label);
        addIfAbsent(emptyLabel);
    }

    public void enableTyping() {
        setCursorPositionByMouse();
        showCursor();
        typing = true;
        showString();
    }

    private void disableTyping() {
        hideCursor();
        cursorPosition = cursorPositionEnd = 0;
        typing = false;
        if (label.getString().isEmpty()) {
            showBlankString();
        }
    }

    protected void resetCursorPosition() {
        cursorPosition = cursorPositionEnd = 0;
    }

    @Override
    public InputBox setTextColor(float r, float g, float b, float a) {
        emptyLabel.setColor(r, g, b, a);
        label.setColor(r, g, b, a);
        return this;
    }

    public InputBox setTextColor(Vector4f color) {
        emptyLabel.setColor(color);
        label.setColor(color);
        return this;
    }

    @Override
    public GuiObject setRenderer(GuiObjectRenderer renderer) {
        if (renderer instanceof InputBoxRenderer inputBoxRenderer) {
            this.renderer = inputBoxRenderer;
        }
        return super.setRenderer(renderer);
    }

    public String getString() {
        return label.getString();
    }

    @Override
    public void clear() {
        resetCursorPosition();
    }
}