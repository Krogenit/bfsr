package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.GuiObject;

public class RectangleRotatedRenderer extends GuiObjectRenderer {
    public RectangleRotatedRenderer(GuiObject guiObject) {
        super(guiObject);
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.addRotated(lastX + width / 2, lastY + height / 2, x + width / 2, y + height / 2, guiObject.getLastRotation(),
                    guiObject.getRotation(), width, height, hoverColor);
        } else {
            guiRenderer.addRotated(lastX + width / 2, lastY + height / 2, x + width / 2, y + height / 2, guiObject.getLastRotation(),
                    guiObject.getRotation(), width, height, color);
        }

        super.render(lastX, lastY, x, y, width, height);
    }
}
