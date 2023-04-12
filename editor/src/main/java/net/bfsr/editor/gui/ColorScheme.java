package net.bfsr.editor.gui;

import net.bfsr.client.gui.button.Button;
import net.bfsr.client.gui.input.InputBox;
import net.bfsr.editor.gui.component.MinimizableGuiObject;
import org.joml.Vector4f;

public final class ColorScheme {
    public static final Vector4f BACKGROUND_COLOR = new Vector4f(43 / 255.0f, 45 / 255.0f, 48 / 255.0f, 0.99f);

    public static final float INPUT_COLOR_GRAY = 35 / 255.0f;
    public static final float INPUT_OUTLINE_COLOR_GRAY = 26 / 255.0f;
    public static final float INPUT_OUTLINE_HOVER_COLOR_GRAY = 101 / 255.0f;
    public static final Vector4f INPUT_BOX_COLOR = new Vector4f(INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, 1.0f);
    public static final Vector4f INPUT_BOX_OUTLINE_COLOR = new Vector4f(INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, 1.0f);
    public static final Vector4f INPUT_BOX_OUTLINE_HOVER_COLOR = new Vector4f(INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY, 1.0f);

    public static final float BUTTON_COLOR_GRAY = 81 / 255.0f;
    public static final float BUTTON_HOVER_COLOR_GRAY = 88 / 255.0f;
    public static final float BUTTON_OUTLINE_COLOR_GRAY = 30 / 255.0f;
    public static final Vector4f BUTTON_COLOR = new Vector4f(BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, 1.0f);
    public static final Vector4f BUTTON_HOVER_COLOR = new Vector4f(BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, 1.0f);
    public static final Vector4f BUTTON_OUTLINE_COLOR = new Vector4f(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f);

    public static final float CHECKBOX_COLOR_GRAY = 42 / 255.0f;

    public static final float TEXT_COLOR_GRAY = 205 / 255.0f;
    public static final Vector4f TEXT_COLOR = new Vector4f(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);

    public static final Vector4f MINIMIZABLE_COLOR = new Vector4f(0.3f, 0.3f, 0.3f, 0.5f);

    public static MinimizableGuiObject setupColors(MinimizableGuiObject object) {
        object.setTextColor(TEXT_COLOR).setHoverColor(MINIMIZABLE_COLOR);
        return object;
    }

    public static Button setupButtonColors(Button button) {
        button.setTextColor(TEXT_COLOR).setHoverColor(BUTTON_HOVER_COLOR).setColor(BUTTON_COLOR).setOutlineColor(BUTTON_OUTLINE_COLOR).setOutlineHoverColor(BUTTON_OUTLINE_COLOR);
        return button;
    }

    public static Button setupContextMenuButtonColors(Button button) {
        button.setTextColor(TEXT_COLOR).setHoverColor(BUTTON_HOVER_COLOR).setColor(BUTTON_COLOR).setOutlineColor(BUTTON_COLOR).setOutlineHoverColor(BUTTON_HOVER_COLOR);
        return button;
    }

    public static InputBox setupInputBoxColors(InputBox inputBox) {
        inputBox.setTextColor(TEXT_COLOR).setHoverColor(INPUT_BOX_COLOR).setColor(INPUT_BOX_COLOR).setOutlineColor(INPUT_BOX_OUTLINE_COLOR).setOutlineHoverColor(INPUT_BOX_OUTLINE_HOVER_COLOR);
        return inputBox;
    }
}