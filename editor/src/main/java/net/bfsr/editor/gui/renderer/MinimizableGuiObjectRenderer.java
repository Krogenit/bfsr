package net.bfsr.editor.gui.renderer;

import net.bfsr.editor.gui.component.MinimizableGuiObject;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.renderer.GuiObjectRenderer;
import org.joml.Vector4f;

import java.util.List;

public class MinimizableGuiObjectRenderer extends GuiObjectRenderer {
    private static final int TRIANGLE_HALF_WIDTH = 4;
    private static final int TRIANGLE_HALF_HEIGHT = 4;

    private final MinimizableGuiObject minimizableGuiObject;

    public MinimizableGuiObjectRenderer(MinimizableGuiObject guiObject) {
        super(guiObject);
        this.minimizableGuiObject = guiObject;
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        renderBase(lastX, lastY, x, y, width, height);

        if (minimizableGuiObject.isCanMaximize()) {
            float interpolation = renderer.getInterpolation();
            renderTriangle((int) (lastX + (x - lastX) * interpolation + 10),
                    (int) (lastY + (y - lastY) * interpolation + minimizableGuiObject.getBaseHeight() / 2));
        }

        List<GuiObject> guiObjects = minimizableGuiObject.getNonHideableObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).render(guiRenderer, lastX, lastY, x, y);
        }
    }

    protected void renderBase(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor.x, hoverColor.y, hoverColor.z, hoverColor.w);
        }
    }

    private void renderTriangle(int centerX, int centerY) {
        Vector4f textColor = minimizableGuiObject.getLabel().getColor();

        if (minimizableGuiObject.isMaximized()) {
            guiRenderer.addPrimitive(centerX - TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT, centerX,
                    centerY + TRIANGLE_HALF_HEIGHT, centerX + TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT,
                    centerX - TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT, textColor.x, textColor.y, textColor.z, textColor.w, 0);
        } else {
            guiRenderer.addPrimitive(centerX - TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT,
                    centerX - TRIANGLE_HALF_WIDTH, centerY + TRIANGLE_HALF_HEIGHT, centerX + TRIANGLE_HALF_WIDTH, centerY,
                    centerX - TRIANGLE_HALF_WIDTH, centerY - TRIANGLE_HALF_HEIGHT, textColor.x, textColor.y, textColor.z, textColor.w, 0);
        }
    }
}
