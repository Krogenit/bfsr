package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.GuiObject;

public class RectangleOutlinedRenderer extends GuiObjectRenderer {
    public RectangleOutlinedRenderer(GuiObject guiObject) {
        super(guiObject);
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, outlineHoverColor);
            guiRenderer.add(lastX + 1, lastY + 1, x + 1, y + 1, width - 2, height - 2, hoverColor);
        } else {
            guiRenderer.add(lastX, lastY, x, y, width, height, outlineColor);
            guiRenderer.add(lastX + 1, lastY + 1, x + 1, y + 1, width - 2, height - 2, color);
        }

        super.render(lastX, lastY, x, y, width, height);
    }
}