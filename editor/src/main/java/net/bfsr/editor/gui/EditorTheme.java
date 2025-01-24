package net.bfsr.editor.gui;

import net.bfsr.client.font.FontType;
import net.bfsr.engine.gui.component.Button;
import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.gui.component.ComboBox;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.component.MinimizableGuiObject;
import net.bfsr.engine.gui.component.ScrollPane;
import net.bfsr.engine.gui.renderer.RectangleOutlinedRenderer;
import net.bfsr.engine.gui.renderer.inputbox.InputBoxOutlinedRenderer;
import org.joml.Vector4f;

public final class EditorTheme {
    public static final Vector4f BACKGROUND_COLOR = new Vector4f(43 / 255.0f, 45 / 255.0f, 48 / 255.0f, 0.99f);

    private static final float INPUT_COLOR_GRAY = 35 / 255.0f;
    private static final float INPUT_OUTLINE_COLOR_GRAY = 26 / 255.0f;
    private static final float INPUT_OUTLINE_HOVER_COLOR_GRAY = 101 / 255.0f;
    private static final Vector4f INPUT_BOX_COLOR = new Vector4f(INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, INPUT_COLOR_GRAY, 1.0f);
    private static final Vector4f INPUT_BOX_OUTLINE_COLOR =
            new Vector4f(INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, 1.0f);
    private static final Vector4f INPUT_BOX_OUTLINE_HOVER_COLOR =
            new Vector4f(INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY, 1.0f);

    private static final float BUTTON_COLOR_GRAY = 81 / 255.0f;
    private static final float BUTTON_HOVER_COLOR_GRAY = 88 / 255.0f;
    private static final float BUTTON_OUTLINE_COLOR_GRAY = 30 / 255.0f;
    private static final Vector4f BUTTON_COLOR = new Vector4f(BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, 1.0f);
    private static final Vector4f BUTTON_HOVER_COLOR =
            new Vector4f(BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, 1.0f);
    private static final Vector4f BUTTON_OUTLINE_COLOR =
            new Vector4f(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f);

    private static final float CHECKBOX_COLOR_GRAY = 42 / 255.0f;

    public static final float TEXT_COLOR_GRAY = 205 / 255.0f;
    public static final Vector4f TEXT_COLOR = new Vector4f(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);

    private static final Vector4f MINIMIZABLE_HOVER_COLOR = new Vector4f(0.3f, 0.3f, 0.3f, 0.5f);

    public static final Vector4f SELECTION_BLUE_COLOR = new Vector4f(35 / 255.0f, 74 / 255.0f, 108 / 255.0f, 1.0f);

    private static final Vector4f SCROLL_COLOR = new Vector4f(77 / 255.0f, 78 / 255.0f, 81 / 255.0f, 1.0f);
    private static final Vector4f SCROLL_HOVER_COLOR = new Vector4f(92 / 255.0f, 93 / 255.0f, 94 / 255.0f, 1.0f);

    public static final FontType FONT_TYPE = FontType.SEGOE_UI;

    public static MinimizableGuiObject setup(MinimizableGuiObject object) {
        object.setTextColor(TEXT_COLOR).setHoverColor(MINIMIZABLE_HOVER_COLOR);
        return object;
    }

    public static Button setupButton(Button button) {
        button.setRenderer(new RectangleOutlinedRenderer(button));
        button.setTextColor(TEXT_COLOR).setHoverColor(BUTTON_HOVER_COLOR).setColor(BUTTON_COLOR).setOutlineColor(BUTTON_OUTLINE_COLOR)
                .setOutlineHoverColor(BUTTON_OUTLINE_COLOR);
        return button;
    }

    public static Button setupContextMenuButton(Button button) {
        button.setRenderer(new RectangleOutlinedRenderer(button));
        button.setTextColor(TEXT_COLOR).setHoverColor(BUTTON_HOVER_COLOR).setColor(BUTTON_COLOR).setOutlineColor(BUTTON_COLOR)
                .setOutlineHoverColor(BUTTON_HOVER_COLOR);
        return button;
    }

    public static InputBox setupInputBox(InputBox inputBox) {
        inputBox.setRenderer(new InputBoxOutlinedRenderer(inputBox));
        inputBox.setTextColor(TEXT_COLOR).setHoverColor(INPUT_BOX_COLOR).setColor(INPUT_BOX_COLOR)
                .setOutlineColor(INPUT_BOX_OUTLINE_COLOR).setOutlineHoverColor(INPUT_BOX_OUTLINE_HOVER_COLOR);
        return inputBox;
    }

    public static ScrollPane setupScrollPane(ScrollPane container) {
        container.setScrollColor(SCROLL_COLOR);
        container.setScrollHoverColor(SCROLL_HOVER_COLOR);
        return container;
    }

    public static CheckBox setup(CheckBox checkBox) {
        checkBox.setColor(CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, 1.0f)
                .setHoverColor(CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, CHECKBOX_COLOR_GRAY, 1.0f)
                .setOutlineColor(INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, INPUT_OUTLINE_COLOR_GRAY, 1.0f)
                .setOutlineHoverColor(INPUT_OUTLINE_HOVER_COLOR_GRAY, INPUT_OUTLINE_HOVER_COLOR_GRAY,
                        INPUT_OUTLINE_HOVER_COLOR_GRAY, 1.0f);
        return checkBox;
    }

    public static ComboBox<?> setup(ComboBox<?> comboBox) {
        comboBox.setColor(BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, BUTTON_COLOR_GRAY, 1.0f)
                .setHoverColor(BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, BUTTON_HOVER_COLOR_GRAY, 1.0f)
                .setOutlineColor(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f)
                .setOutlineHoverColor(BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, BUTTON_OUTLINE_COLOR_GRAY, 1.0f)
                .setTextColor(TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, TEXT_COLOR_GRAY, 1.0f);
        return comboBox;
    }
}