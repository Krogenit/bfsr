package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.gui.component.MinimizableGuiObject;
import net.bfsr.engine.renderer.primitive.Primitive;
import org.joml.Vector4f;

import java.util.List;

public class MinimizableGuiObjectRenderer extends SimpleGuiObjectRenderer {
    private static final int TRIANGLE_WIDTH = 8;
    private static final int TRIANGLE_HEIGHT = 8;

    private static final Primitive TRIANGLE_PRIMITIVE_DATA = new Primitive(-0.5f, -0.5f, 0.0f, 1.0f, 0.5f, 0, 1.0f, 1.0f, -0.5f, 0.5f, 1.0f,
            0.0f, -0.5f, -0.5f, 0.0f, 0.0f);
    private static final Primitive TRIANGLE_MAXIMIZED_PRIMITIVE_DATA = new Primitive(-0.5f, 0.5f, 0.0f, 1.0f, 0, -0.5f, 1.0f,
            1.0f, 0.5f, 0.5f, 1.0f, 0.0f, -0.5f, -0.5f, 0.0f, 0.0f);

    private final MinimizableGuiObject minimizableGuiObject;

    private int triangleId = -1, maximizedTriangleId = -1;

    public MinimizableGuiObjectRenderer(MinimizableGuiObject guiObject) {
        super(guiObject);
        this.minimizableGuiObject = guiObject;
    }

    @Override
    public void create() {
        int x = guiObject.getSceneX();
        int y = guiObject.getSceneY();
        int width = guiObject.getWidth();
        int height = guiObject.getHeight();

        addBaseToScene(x, y, width, height);
        addTrianglesToScene(x + 10, y + height - minimizableGuiObject.getBaseHeight() / 2);
    }

    private void addBaseToScene(int x, int y, int width, int height) {
        idList.add(id = guiRenderer.add(x, y, width, height, color));
    }

    private void addTrianglesToScene(int centerX, int centerY) {
        guiRenderer.addPrimitive(TRIANGLE_MAXIMIZED_PRIMITIVE_DATA);
        guiRenderer.addPrimitive(TRIANGLE_PRIMITIVE_DATA);

        Vector4f textColor = minimizableGuiObject.getLabel().getColor();
        idList.add(maximizedTriangleId = guiRenderer.add(centerX, centerY, TRIANGLE_WIDTH, TRIANGLE_HEIGHT, textColor));
        idList.add(triangleId = guiRenderer.add(centerX, centerY, TRIANGLE_WIDTH, TRIANGLE_HEIGHT, textColor));
    }

    @Override
    public void render() {
        renderBase();

        if (minimizableGuiObject.isCanMaximize()) {
            renderTriangle();
        }

        List<GuiObject> guiObjects = minimizableGuiObject.getNonHideableObjects();
        for (int i = 0; i < guiObjects.size(); i++) {
            guiObjects.get(i).getRenderer().render();
        }
    }

    protected void renderBase() {
        if (guiObject.isMouseHover()) {
            guiRenderer.addDrawCommand(id);
        }
    }

    private void renderTriangle() {
        if (minimizableGuiObject.isMaximized()) {
            guiRenderer.addDrawCommand(maximizedTriangleId, TRIANGLE_MAXIMIZED_PRIMITIVE_DATA.getBaseVertex());
        } else {
            guiRenderer.addDrawCommand(triangleId, TRIANGLE_PRIMITIVE_DATA.getBaseVertex());
        }
    }

    @Override
    protected void setLastUpdateValues() {
        int x = guiObject.getSceneX();
        int y = guiObject.getSceneY();
        int height = guiObject.getHeight();
        int centerX = x + 10;
        int centerY = y + height - minimizableGuiObject.getBaseHeight() / 2;
        guiRenderer.setLastPosition(id, x, y);
        guiRenderer.setLastSize(id, guiObject.getWidth(), height);
        guiRenderer.setLastPosition(maximizedTriangleId, centerX, centerY);
        guiRenderer.setLastPosition(triangleId, centerX, centerY);
    }

    @Override
    public void updatePosition() {
        int x = guiObject.getSceneX();
        int y = guiObject.getSceneY();
        int height = guiObject.getHeight();
        int centerX = x + 10;
        int centerY = y + height - minimizableGuiObject.getBaseHeight() / 2;
        guiRenderer.setPosition(id, x, y);
        guiRenderer.setPosition(maximizedTriangleId, centerX, centerY);
        guiRenderer.setPosition(triangleId, centerX, centerY);
    }

    @Override
    public void updatePosition(int x, int y) {
        int height = guiObject.getHeight();
        int centerX = x + 10;
        int centerY = y + height - minimizableGuiObject.getBaseHeight() / 2;
        guiRenderer.setPosition(id, x, y);
        guiRenderer.setPosition(maximizedTriangleId, centerX, centerY);
        guiRenderer.setPosition(triangleId, centerX, centerY);
    }
}
