package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.GuiObject;

public class RectangleRenderer extends GuiObjectRenderer {
    public RectangleRenderer(GuiObject guiObject) {
        super(guiObject);
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor);
        } else {
            guiRenderer.add(lastX, lastY, x, y, width, height, color);
        }

        super.render(lastX, lastY, x, y, width, height);
    }
}
