package net.bfsr.engine.gui.renderer.inputbox;

import net.bfsr.engine.gui.component.InputBox;

public class InputBoxOutlinedRenderer extends InputBoxRenderer {
    public InputBoxOutlinedRenderer(InputBox inputBox) {
        super(inputBox);
    }

    @Override
    protected void renderBody(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, outlineHoverColor);
            guiRenderer.add(lastX + 1, lastY + 1, x + 1, y + 1, width - 2, height - 2, hoverColor);
        } else {
            guiRenderer.add(lastX, lastY, x, y, width, height, outlineColor);
            guiRenderer.add(lastX + 1, lastY + 1, x + 1, y + 1, width - 2, height - 2, color);
        }
    }
}
