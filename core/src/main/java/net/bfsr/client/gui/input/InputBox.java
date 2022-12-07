package net.bfsr.client.gui.input;

import net.bfsr.client.font.FontRegistry;
import net.bfsr.client.font.FontType;
import net.bfsr.client.font.GUIText;
import net.bfsr.client.font.TextMeshCreator;
import net.bfsr.client.input.Keyboard;
import net.bfsr.client.input.Mouse;
import net.bfsr.client.language.Lang;
import net.bfsr.client.particle.EnumParticlePositionType;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.shader.BaseShader;
import net.bfsr.client.sound.GuiSoundSource;
import net.bfsr.client.sound.SoundRegistry;
import net.bfsr.client.texture.Texture;
import net.bfsr.collision.AxisAlignedBoundingBox;
import net.bfsr.core.Core;
import net.bfsr.entity.TextureObject;
import net.bfsr.math.EnumZoomFactor;
import net.bfsr.math.Transformation;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

public class InputBox extends TextureObject {

    private AxisAlignedBoundingBox aabb;
    protected GUIText text, typingText;
    protected boolean isTyping;
    protected boolean collided, renderCursor;
    private int cursorTimer, cursorMaxTimer = 25;
    protected int cursorPosition, cursorPositionEnd;
    protected boolean wasEndPos;
    private final EnumInputType[] types;
    protected final FontType font = FontRegistry.XOLONIUM;
    private final Vector2f baseScale;
    protected final Vector2f fontSize;
    protected final Vector2f startFontSize;
    private float maxLineSize = 0.175f;
    protected final Vector2f baseTextOffset;
    private final Vector2f textOffset;
    private final Vector2f emptyOffset;
    protected final Vector4f textColor;
    protected final Vector4f selectionColor;
    protected EnumParticlePositionType posType;

    public InputBox(Texture texture, Vector2f pos, Vector2f scale, String text, EnumParticlePositionType posType, Vector2f fontSize, Vector2f emptyOffset, Vector2f baseTextOffset, EnumInputType... types) {
        super(texture, Transformation.getOffsetByScale(pos), new Vector2f(scale.x * Transformation.guiScale.x, scale.y * Transformation.guiScale.y));
        this.baseScale = scale;
        this.posType = posType;
        this.types = types;
        this.textColor = new Vector4f(1, 1, 1, 1);
        this.aabb = new AxisAlignedBoundingBox(new Vector2f(this.position.x + origin.x, this.position.y + origin.y), new Vector2f(this.position.x - origin.x, this.position.y - origin.y));
        this.selectionColor = new Vector4f(0.7f, 0.8f, 1.0f, 0.5f);
        this.startFontSize = fontSize;
        this.baseTextOffset = baseTextOffset;
        this.fontSize = new Vector2f(0.8f * Transformation.guiScale.x * fontSize.x, 1f * Transformation.guiScale.y * fontSize.y);
        this.text = new GUIText(Lang.getString(text), this.fontSize, font, Transformation.getOffsetByScale(new Vector2f(pos.x + emptyOffset.x, pos.y + emptyOffset.y)), textColor, true, posType);
        this.emptyOffset = emptyOffset;
        this.textOffset = new Vector2f(baseTextOffset.x * Transformation.guiScale.x, baseTextOffset.y * Transformation.guiScale.y);
        setZoomFactor(EnumZoomFactor.Gui);
    }

    public InputBox(Texture texture, Vector2f pos, String text, EnumParticlePositionType posType, Vector2f fontSize, Vector2f emptyOffset, Vector2f baseTextOffset, EnumInputType... types) {
        this(texture, pos, new Vector2f(300, 50), text, posType, fontSize, emptyOffset, baseTextOffset, types);
    }

    @Override
    public void setScale(float x, float y) {
        this.baseScale.x = x;
        this.baseScale.y = y;
        this.scale.x = x * Transformation.guiScale.x;
        this.scale.y = y * Transformation.guiScale.y;
    }

    @Override
    public void setScale(Vector2f scale) {
        this.baseScale.x = scale.x;
        this.baseScale.y = scale.y;
        this.scale.x = scale.x * Transformation.guiScale.x;
        this.scale.y = scale.y * Transformation.guiScale.y;
    }

    public void setEmptyOffset(Vector2f emptyOffset) {
        this.emptyOffset.x = emptyOffset.x;
        this.emptyOffset.y = emptyOffset.y;
    }

    public void setTextOffset(Vector2f baseTextOffset) {
        this.baseTextOffset.x = baseTextOffset.x;
        this.baseTextOffset.y = baseTextOffset.y;
        this.textOffset.x = baseTextOffset.x * Transformation.guiScale.x;
        this.textOffset.y = baseTextOffset.y * Transformation.guiScale.y;
    }

    public void init() {
        this.fontSize.x = 0.8f * Transformation.guiScale.x * startFontSize.x;
        this.fontSize.y = 1f * Transformation.guiScale.y * startFontSize.y;
        text.setFontSize(fontSize);
        if (typingText != null) {
            typingText.setFontSize(fontSize);
            typingText.updateText(typingText.getTextString());
        }
//		text.updateText(text.getTextString());
    }

    @Override
    public void setPosition(Vector2f pos) {
        this.position = Transformation.getOffsetByScale(pos);
        this.scale.x = baseScale.x * Transformation.guiScale.x;
        this.scale.y = baseScale.y * Transformation.guiScale.y;
        this.origin.x = -scale.x / 2.0f;
        this.origin.y = -scale.y / 2.0f;
        this.textOffset.x = baseTextOffset.x * Transformation.guiScale.x;
        this.textOffset.y = baseTextOffset.y * Transformation.guiScale.y;
        this.aabb = new AxisAlignedBoundingBox(new Vector2f(this.position.x + origin.x, this.position.y + origin.y), new Vector2f(this.position.x - origin.x, this.position.y - origin.y));
        this.text.setPosition(Transformation.getOffsetByScale(new Vector2f(pos.x + emptyOffset.x, pos.y + emptyOffset.y)));
        if (typingText != null) {
            //Vector2f textOffset = Transformation.getOffsetByScale(this.textOffset);
            Vector2f pos2 = new Vector2f(position.x + textOffset.x, position.y + textOffset.y);
            typingText.setPosition(pos2);
        }
    }

    @Override
    public void setPosition(float x, float y) {
        this.position = Transformation.getOffsetByScale(new Vector2f(x, y));
        this.scale.x = baseScale.x * Transformation.guiScale.x;
        this.scale.y = baseScale.y * Transformation.guiScale.y;
        this.origin.x = -scale.x / 2.0f;
        this.origin.y = -scale.y / 2.0f;
        this.textOffset.x = baseTextOffset.x * Transformation.guiScale.x;
        this.textOffset.y = baseTextOffset.y * Transformation.guiScale.y;
        this.aabb = new AxisAlignedBoundingBox(new Vector2f(this.position.x + origin.x, this.position.y + origin.y), new Vector2f(this.position.x - origin.x, this.position.y - origin.y));
        this.text.setPosition(Transformation.getOffsetByScale(new Vector2f(x + emptyOffset.x, y + emptyOffset.y)));
        if (typingText != null) {
//			Vector2f textOffset = Transformation.getOffsetByScale(this.textOffset);
            Vector2f pos2 = new Vector2f(position.x + textOffset.x, position.y + textOffset.y);
            typingText.setPosition(pos2);
        }
    }

    public void onMouseLeftClicked() {
        if (isIntersects()) {
            if (!isTyping) setTyping(true);

            if (typingText != null) {
                setCursorPositionByMouse();
                Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
            }
        } else {
            setTyping(false);
        }
    }

    public void input() {
        if (typingText != null && isIntersects()) {
            if (Mouse.isLeftDown()) {
                Vector2f mousePos = Mouse.getPosition();
                Vector2f pos = new Vector2f(mousePos.x - this.position.x - textOffset.x, mousePos.y);
                int endPos = TextMeshCreator.getCursorPositionInLine(typingText.getTextString(), font, fontSize.x, pos);
                if (endPos <= cursorPosition) {
                    cursorPosition = endPos;
                    wasEndPos = false;
                } else {
                    cursorPositionEnd = endPos;
                    wasEndPos = true;
                }
            }
        }
    }

    public void input(int key) {
        if (typingText != null) {
            if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) && key == GLFW.GLFW_KEY_A) {
                cursorPosition = 0;
                cursorPositionEnd = typingText.getTextString().length();
                wasEndPos = true;
            }

            if (key == GLFW.GLFW_KEY_LEFT) {
                if (wasEndPos) cursorPosition = cursorPositionEnd;
                cursorPosition--;
                if (cursorPosition < 0) cursorPosition = 0;
                if (!Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) cursorPositionEnd = cursorPosition;
                else {
                    wasEndPos = false;
                }
            } else if (key == GLFW.GLFW_KEY_RIGHT) {
                if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    if (!wasEndPos) cursorPositionEnd = cursorPosition;
                    cursorPositionEnd++;
                    int lineWidth = typingText.getTextString().length();
                    if (cursorPositionEnd > lineWidth) cursorPositionEnd = lineWidth;
                    wasEndPos = true;
                } else {
                    cursorPosition++;
                    int lineWidth = typingText.getTextString().length();
                    if (cursorPosition > lineWidth) cursorPosition = lineWidth;
                    cursorPositionEnd = cursorPosition;
                }
            }

            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                String prevString = typingText.getTextString();

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
                    typingText.clear();
                    Vector2f pos1 = getPosition();
                    Vector2f pos = new Vector2f(pos1.x + textOffset.x, pos1.y + textOffset.y);
                    typingText = new GUIText(newString, fontSize, font, pos, textColor, false, posType);
                }
            } else if (key == GLFW.GLFW_KEY_DELETE) {
                String prevString = typingText.getTextString();

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
                    typingText.clear();
                    Vector2f pos1 = getPosition();
                    Vector2f pos = new Vector2f(pos1.x + textOffset.x, pos1.y + textOffset.y);
                    typingText = new GUIText(newString, fontSize, font, pos, textColor, false, posType);
                }
            }
        }
    }

    private void setCursorPositionByMouse() {
        if (typingText != null) {
            Vector2f mousePos = Mouse.getPosition();
            Vector2f pos = new Vector2f(mousePos.x - this.position.x - textOffset.x, mousePos.y);
            cursorPosition = TextMeshCreator.getCursorPositionInLine(typingText.getTextString(), font, fontSize.x, pos);
            cursorPositionEnd = cursorPosition;
        }
    }

    public void textInput(int key) {
        if (typingText == null || TextMeshCreator.getLineWidth(typingText.getTextString().substring(0, cursorPosition) +
                typingText.getTextString().substring(cursorPositionEnd), font, fontSize.x) < maxLineSize * fontSize.x) {
            char keyName = (char) key;// GLFW.glfwGetKeyName(key, 0);
            if (font.getLoader().hasCharacter(keyName)) {
                String prevString = "";
                if (typingText != null) {
                    prevString = typingText.getTextString();
                    typingText.clear();
                }

                if (cursorPosition != cursorPositionEnd) {
                    String newString = prevString.substring(0, cursorPosition) + keyName + prevString.substring(cursorPositionEnd);
                    Vector2f pos1 = getPosition();
                    Vector2f pos = new Vector2f(pos1.x + textOffset.x, pos1.y + textOffset.y);
                    typingText = new GUIText(newString, fontSize, font, pos, textColor, false, posType);

                    cursorPositionEnd = cursorPosition;
                    cursorPosition++;
                    cursorPositionEnd++;
                } else {
                    String newString = prevString.substring(0, cursorPosition) + keyName + prevString.substring(cursorPosition);
                    Vector2f pos1 = getPosition();
                    Vector2f pos = new Vector2f(pos1.x + textOffset.x, pos1.y + textOffset.y);
                    typingText = new GUIText(newString, fontSize, font, pos, textColor, false, posType);

                    cursorPosition++;
                    cursorPositionEnd++;
                }
            }
        }
    }

    public void update() {
        if (aabb.isIntersects(Mouse.getPosition())) {
            if (!collided) {
                collided = true;
                Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonCollide));
            }
        } else {
            collided = false;
        }

        if (isTyping) {
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
    public void render(BaseShader shader) {
        super.render(shader);

        Vector2f pos = getPosition();
        float cursorOffset = 5;
        float lineWidth = cursorOffset;

        if (typingText != null && typingText.getTextString().length() > 0) {
            float subLineWidth = TextMeshCreator.getLineWidth(typingText.getTextString().substring(0, cursorPosition), font, fontSize.x);
            lineWidth = subLineWidth * 1400;
            if (lineWidth == 0) lineWidth = cursorOffset;

            if (cursorPositionEnd != cursorPosition) {
                String subString = typingText.getTextString().substring(cursorPosition, cursorPositionEnd);
                subLineWidth = TextMeshCreator.getLineWidth(subString, font, fontSize.x) * 1400 - cursorOffset;
                float scaleOffset = -getScale().x / 2.4f;
                shader.setColor(selectionColor);
                shader.disableTexture();
                shader.setModelViewMatrix(Transformation.getModelViewMatrix(pos.x + scaleOffset + lineWidth + subLineWidth / 2f, pos.y, 0, subLineWidth, getScale().y / 1.7f, EnumZoomFactor.Gui));
                Renderer.quad.render();
                if (wasEndPos) lineWidth += subLineWidth;
            }
        }

        if (renderCursor) {
            shader.disableTexture();
            shader.setColor(getColor());
            shader.setModelViewMatrix(Transformation.getModelViewMatrix(pos.x - getScale().x / 2.4f + lineWidth, pos.y, 0, 1, getScale().y / 1.7f, EnumZoomFactor.Gui));
            Renderer.quad.render();
        }

        shader.enableTexture();
    }

    public void setText(String text) {
        this.text.clear();
        Vector2f pos1 = getPosition();
        Vector2f pos = new Vector2f(pos1.x + textOffset.x, pos1.y + textOffset.y);
        typingText = new GUIText(text, fontSize, font, pos, textColor, false, posType);
    }

    public boolean isIntersects() {
        return aabb.isIntersects(Mouse.getPosition());
    }

    public void setTyping(boolean value) {
        if (!isTyping && value) {
            text.clear();
        } else if (isTyping && !value && (typingText == null || typingText.getTextString().length() == 0)) {
            text = new GUIText(text.getTextString(), fontSize, font, text.getPosition(), textColor, true, posType);
        }

        if (value) {
            setCursorPositionByMouse();
            Core.getCore().getSoundManager().play(new GuiSoundSource(SoundRegistry.buttonClick));
        } else {
            renderCursor = false;
            cursorTimer = cursorMaxTimer;
        }

        this.isTyping = value;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void clear() {
        text.clear();
        if (typingText != null) {
            typingText.clear();
            typingText.setTextString("");
        }

        resetCursorPosition();
    }

    public void resetCursorPosition() {
        cursorPosition = cursorPositionEnd = 0;
    }

    public String getString() {
        if (typingText != null) return typingText.getTextString();
        else return "";
    }

    public void setPosType(EnumParticlePositionType posType) {
        this.posType = posType;
    }

    public void setMaxLineSize(float maxLineSize) {
        this.maxLineSize = maxLineSize;
    }
}
