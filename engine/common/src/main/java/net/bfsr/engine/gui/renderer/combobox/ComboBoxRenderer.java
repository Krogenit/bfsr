package net.bfsr.engine.gui.renderer.combobox;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.ComboBox;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;
import org.joml.Vector4f;

public class ComboBoxRenderer extends GuiObjectRenderer {
    private final ComboBox<?> comboBox;
    private final int triangleXOffset = 4;

    public ComboBoxRenderer(ComboBox<?> comboBox) {
        super(comboBox);
        this.comboBox = comboBox;
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

        int triangleWidth = 14;
        int triangleHalfWidth = triangleWidth / 2;
        int triangleHeight = 8;
        int triangleHalfHeight = triangleHeight / 2;
        float interpolation = Engine.renderer.getInterpolation();
        int triangleX = (int) (lastX + (x - lastX) * interpolation + width - triangleWidth / 2 -
                triangleXOffset);
        int triangleY = (int) (lastY + (y - lastY) * interpolation + height / 2);

        Vector4f color = comboBox.getLabel().getColor();
        guiRenderer.addPrimitive(triangleX - triangleHalfWidth, triangleY - triangleHalfHeight, triangleX,
                triangleY + triangleHalfHeight, triangleX + triangleHalfWidth, triangleY - triangleHalfHeight,
                triangleX - triangleHalfWidth, triangleY - triangleHalfHeight, color.x, color.y, color.z, color.w, 0);
    }
}
