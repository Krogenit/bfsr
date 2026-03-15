package net.bfsr.client.gui;

import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.gui.component.Slider;
import net.bfsr.engine.gui.renderer.RectangleOutlinedRenderer;
import net.bfsr.engine.gui.renderer.inputbox.InputBoxOutlinedRenderer;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class GuiStyle {
    private static final float BLACK_COLOR = 0 / 255.0f;
    public static final Vector3f UI_COLOR = new Vector3f(0.7f, 0.85f, 1.0f);

    private static final Vector4f RECTANGLE_COLOR_TRANSPARENT = new Vector4f(BLACK_COLOR, BLACK_COLOR, BLACK_COLOR, 0.25f);
    private static final Vector4f RECTANGLE_OUTLINE_COLOR_TRANSPARENT = new Vector4f(UI_COLOR.x, UI_COLOR.y, UI_COLOR.z, 0.5f);

    private static final Vector4f BUTTON_COLOR_TRANSPARENT = new Vector4f(UI_COLOR.x, UI_COLOR.y, UI_COLOR.z, 0.1f);
    private static final Vector4f BUTTON_HOVER_COLOR_TRANSPARENT = new Vector4f(UI_COLOR.x, UI_COLOR.y, UI_COLOR.z, 0.25f);
    private static final Vector4f BUTTON_OUTLINE_COLOR_TRANSPARENT = new Vector4f(UI_COLOR.x, UI_COLOR.y, UI_COLOR.z, 0.4f);
    private static final Vector4f BUTTON_HOVER_OUTLINE_COLOR_TRANSPARENT = new Vector4f(UI_COLOR.x, UI_COLOR.y, UI_COLOR.z, 0.4f);

    public static GuiObject setupTransparentRectangle(GuiObject guiObject) {
        RectangleOutlinedRenderer renderer = new RectangleOutlinedRenderer(guiObject);
        renderer.setOutlineSize(1);
        guiObject.setRenderer(renderer);
        guiObject.setColor(RECTANGLE_COLOR_TRANSPARENT).setHoverColor(RECTANGLE_COLOR_TRANSPARENT)
                .setOutlineColor(RECTANGLE_OUTLINE_COLOR_TRANSPARENT).setOutlineHoverColor(RECTANGLE_OUTLINE_COLOR_TRANSPARENT);
        return guiObject;
    }

    public static GuiObject setupTransparentButton(GuiObject button) {
        RectangleOutlinedRenderer renderer = new RectangleOutlinedRenderer(button);
        renderer.setOutlineSize(1);
        button.setRenderer(renderer);
        button.setColor(BUTTON_COLOR_TRANSPARENT).setHoverColor(BUTTON_HOVER_COLOR_TRANSPARENT)
                .setOutlineColor(BUTTON_OUTLINE_COLOR_TRANSPARENT).setOutlineHoverColor(BUTTON_HOVER_OUTLINE_COLOR_TRANSPARENT);
        return button;
    }

    public static Slider setupTransparentSlider(Slider slider) {
        slider.setRenderer(new RectangleOutlinedRenderer(slider));
        slider.setColor(BUTTON_COLOR_TRANSPARENT).setHoverColor(BUTTON_HOVER_COLOR_TRANSPARENT)
                .setOutlineColor(BUTTON_OUTLINE_COLOR_TRANSPARENT).setOutlineHoverColor(BUTTON_HOVER_OUTLINE_COLOR_TRANSPARENT);

        setupTransparentButton(slider.getMovingValue());

        return slider;
    }

    public static InputBox setupTransparentInputBox(InputBox inputBox) {
        InputBoxOutlinedRenderer renderer = new InputBoxOutlinedRenderer(inputBox);
        inputBox.setRenderer(renderer);
        inputBox.setColor(RECTANGLE_COLOR_TRANSPARENT).setHoverColor(RECTANGLE_COLOR_TRANSPARENT)
                .setOutlineColor(RECTANGLE_OUTLINE_COLOR_TRANSPARENT).setOutlineHoverColor(RECTANGLE_OUTLINE_COLOR_TRANSPARENT);
        return inputBox;
    }
}
