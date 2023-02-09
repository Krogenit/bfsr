package net.bfsr.client.gui.input;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.render.font.FontType;
import net.bfsr.client.render.font.StringCache;
import net.bfsr.client.render.font.string.StringObject;
import net.bfsr.client.render.instanced.BufferType;
import net.bfsr.client.render.instanced.InstancedRenderer;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.core.Core;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

public class InputBox extends TexturedGuiObject {
    protected final StringObject stringObject;
    private final StringObject emptyStringObject;
    @Getter
    protected boolean typing;
    private boolean collided, renderCursor;
    private int cursorTimer;
    private final int cursorMaxTimer = 25;
    private int cursorPosition, cursorPositionEnd, startSelectionCursor;
    private boolean leftToRightSelection;
    protected final FontType font = FontType.XOLONIUM;
    protected int fontSize;
    private float maxLineSize;
    protected final Vector2i stringOffset;
    protected final Vector4f textColor = new Vector4f(1.0f);
    private final Vector4f selectionColor = new Vector4f(0.7f, 0.8f, 1.0f, 0.5f);
    @Setter
    private int cursorHeight;

    InputBox(TextureRegister texture, int x, int y, int width, int height, String string, int fontSize, int stringOffsetX, int stringOffsetY) {
        super(texture, x, y, width, height);
        this.fontSize = fontSize;
        this.stringOffset = new Vector2i(stringOffsetX, stringOffsetY);

        int stringX = x + stringOffsetX;
        int stringY = (int) (y + (height - font.getStringCache().getHeight(string, fontSize)) / 2.0f + font.getStringCache().getAscent(string, fontSize)) + stringOffsetY;

        this.stringObject = new StringObject(font, fontSize, textColor.x, textColor.y, textColor.z, textColor.w);
        this.stringObject.setPosition(stringX, stringY);
        this.emptyStringObject = new StringObject(font, string, fontSize, textColor.x, textColor.y, textColor.z, textColor.w);
        this.emptyStringObject.setPosition(stringX, stringY);
        this.emptyStringObject.compile();

        maxLineSize = width / 1.2f;
        cursorHeight = (int) (height / 1.7f);
    }

    public InputBox(TextureRegister texture, String string, int fontSize, int stringOffsetX, int stringOffsetY) {
        this(texture, 0, 0, 300, 50, string, fontSize, stringOffsetX, stringOffsetY);
    }

    @Override
    public InputBox setPosition(int x, int y) {
        super.setPosition(x, y);
        StringCache stringCache = font.getStringCache();
        int stringX = x + stringOffset.x;
        int stringY = (int) (y + (height - stringCache.getHeight(stringObject.getString(), fontSize)) / 2.0f + stringCache.getAscent(stringObject.getString(), fontSize)) + stringOffset.y;
        this.stringObject.setPosition(stringX, stringY);
        this.emptyStringObject.setPosition(stringX, stringY);
        return this;
    }

    @Override
    public void onMouseLeftClick() {
        setTyping(isIntersects());
    }

    @Override
    public void input(int key) {
        if (typing) {
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_A) {
                startSelectionCursor = cursorPosition = 0;
                cursorPositionEnd = stringObject.getString().length();
                leftToRightSelection = true;
            }

            if (key == GLFW.GLFW_KEY_LEFT) {
                if (leftToRightSelection) cursorPosition = cursorPositionEnd;
                cursorPosition--;
                if (cursorPosition < 0) cursorPosition = 0;
                if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    leftToRightSelection = false;
                } else {
                    cursorPositionEnd = cursorPosition;
                }
            } else if (key == GLFW.GLFW_KEY_RIGHT) {
                if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    if (!leftToRightSelection) cursorPositionEnd = cursorPosition;
                    cursorPositionEnd++;
                    int lineWidth = stringObject.getString().length();
                    if (cursorPositionEnd > lineWidth) cursorPositionEnd = lineWidth;
                    leftToRightSelection = true;
                } else {
                    cursorPosition++;
                    int lineWidth = stringObject.getString().length();
                    if (cursorPosition > lineWidth) cursorPosition = lineWidth;
                    cursorPositionEnd = cursorPosition;
                }
            }

            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                String prevString = stringObject.getString();

                if (prevString.length() > 0) {
                    String newString;
                    if (cursorPositionEnd != cursorPosition) {
                        newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPositionEnd);
                        cursorPositionEnd = cursorPosition;
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
                    stringObject.update(newString);
                }
            } else if (key == GLFW.GLFW_KEY_DELETE) {
                String prevString = stringObject.getString();

                if (prevString.length() > 0) {
                    int sl = prevString.length();
                    String newString;
                    if (cursorPositionEnd != cursorPosition) {
                        newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPositionEnd);
                        cursorPositionEnd = cursorPosition;
                    } else {
                        if (cursorPosition < sl) // If we have chars after cursor
                            newString = prevString.substring(0, cursorPosition) + prevString.substring(cursorPosition + 1);
                        else newString = prevString;// Else save string
                    }
                    stringObject.update(newString);
                }
            }
        }
    }

    private void setCursorPositionByMouse() {
        startSelectionCursor = cursorPosition = cursorPositionEnd = stringObject.getCursorPositionInLine(Mouse.getPosition().x - x - stringOffset.x);
    }

    @Override
    public void textInput(int key) {
        if (typing) {
            char keyName = (char) key;
            String prevString = "";
            if (stringObject.getString().length() > 0) {
                prevString = stringObject.getString();
            }

            if (cursorPosition != cursorPositionEnd) {
                String newString = prevString.substring(0, cursorPosition) + keyName + prevString.substring(cursorPositionEnd);
                if (stringObject.getStringCache().getStringWidth(newString, stringObject.getFontSize()) < maxLineSize) {
                    stringObject.update(newString);

                    cursorPositionEnd = cursorPosition;
                    cursorPosition++;
                    cursorPositionEnd++;
                }
            } else {
                String newString = prevString.substring(0, cursorPosition) + keyName + prevString.substring(cursorPosition);
                if (stringObject.getStringCache().getStringWidth(newString, stringObject.getFontSize()) < maxLineSize) {
                    stringObject.update(newString);

                    cursorPosition++;
                    cursorPositionEnd++;
                }
            }
        }
    }

    @Override
    public void update() {
        if (typing && isIntersects()) {
            if (Mouse.isLeftDown()) {
                int endPos = stringObject.getCursorPositionInLine(Mouse.getPosition().x - x - stringOffset.x);
                if (endPos <= startSelectionCursor) {
                    cursorPosition = endPos;
                    leftToRightSelection = false;
                } else {
                    cursorPositionEnd = endPos;
                    leftToRightSelection = true;
                }
            }
        }

        if (isIntersects()) {
            if (!collided) {
                collided = true;
                Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonCollide));
            }
        } else {
            collided = false;
        }

        if (typing) {
            if (cursorTimer > 0) {
                cursorTimer--;
            }

            if (cursorTimer == 0) {
                renderCursor = !renderCursor;
                cursorTimer = 25;
            }
        }
    }

    @Override
    public void render() {
        super.render();
        renderString();
        renderSelectionAndCursor();
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
        int cursorY = y + stringOffset.y + height / 2 - cursorHeight / 2;
        if (stringObject.getString().length() > 0) {
            if (cursorPositionEnd != cursorPosition) {
                int leftStringWidth = stringObject.getStringCache().getStringWidth(stringObject.getString().substring(0, cursorPosition), fontSize);
                int rightStringWidth = stringObject.getStringCache().getStringWidth(stringObject.getString().substring(cursorPosition, cursorPositionEnd), fontSize);
                InstancedRenderer.INSTANCE.addGUIElementToRenderPipeLine(x + leftStringWidth + stringOffset.x, cursorY, rightStringWidth, cursorHeight, selectionColor.x, selectionColor.y, selectionColor.z,
                        selectionColor.w, 0, BufferType.GUI);
                if (leftToRightSelection) {
                    lineWidth = leftStringWidth + rightStringWidth;
                } else {
                    lineWidth = leftStringWidth;
                }
            } else {
                lineWidth = font.getStringCache().getStringWidth(stringObject.getString().substring(0, cursorPosition), fontSize);
            }
        } else {
            lineWidth = 0;
        }

        if (renderCursor) {
            InstancedRenderer.INSTANCE.addGUIElementToRenderPipeLine(x + stringOffset.x + lineWidth, cursorY, 1, cursorHeight, color.x, color.y, color.z, color.w, 0, BufferType.GUI);
        }
    }

    public InputBox setStringObject(String stringObject) {
        this.stringObject.update(stringObject);
        return this;
    }

    private void setTyping(boolean value) {
        if (value) {
            setCursorPositionByMouse();
            Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
        } else {
            renderCursor = false;
            cursorTimer = cursorMaxTimer;
            cursorPosition = cursorPositionEnd = startSelectionCursor = 0;
        }

        this.typing = value;
    }

    @Override
    public void clear() {
        stringObject.clear();
        resetCursorPosition();
    }

    void resetCursorPosition() {
        cursorPosition = cursorPositionEnd = startSelectionCursor = 0;
    }

    public String getString() {
        return stringObject.getString();
    }

    void setMaxLineSize(float maxLineSize) {
        this.maxLineSize = maxLineSize;
    }
}
