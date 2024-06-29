package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.renderer.opengl.GL;

public class CheckBoxRenderer extends GuiObjectRenderer {
    private final CheckBox checkBox;

    public CheckBoxRenderer(CheckBox checkBox) {
        super(checkBox);
        this.checkBox = checkBox;
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

        if (checkBox.isChecked()) {
            guiRenderer.render();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            guiRenderer.addPrimitive(x + 4, centerY, centerX - 2, y + height - 6, centerX - 2, y + height - 6, x + width - 4, y + 5, 0.9f,
                    0.9f, 0.9f, 1.0f, 0);
            Engine.renderer.lineWidth(3.0f);
            guiRenderer.render(GL.GL_LINES);
            Engine.renderer.lineWidth(1.0f);
        }

        super.render(lastX, lastY, x, y, width, height);
    }
}
