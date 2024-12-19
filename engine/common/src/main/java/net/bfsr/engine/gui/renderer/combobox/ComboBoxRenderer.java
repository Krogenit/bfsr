package net.bfsr.engine.gui.renderer.combobox;

import net.bfsr.engine.gui.component.ComboBox;
import net.bfsr.engine.gui.renderer.RectangleOutlinedRenderer;
import net.bfsr.engine.renderer.primitive.Primitive;
import org.joml.Vector4f;

public class ComboBoxRenderer extends RectangleOutlinedRenderer {
    private static final Primitive TRIANGLE_PRIMITIVE = new Primitive(-0.5f, 0.2857f, 0.0f, 1.0f, 0.0f, -0.2857f, 1.0f,
            1.0f, 0.5f, 0.2857f, 1.0f, 0.0f, -0.5f, 0.2857f, 0.0f, 0.0f);

    private final ComboBox<?> comboBox;
    private final int triangleXOffset = 4;
    private int triangleId = -1;

    public ComboBoxRenderer(ComboBox<?> comboBox) {
        super(comboBox);
        this.comboBox = comboBox;
    }

    @Override
    protected void create() {
        super.create();
        guiRenderer.addPrimitive(TRIANGLE_PRIMITIVE);

        int x = guiObject.getSceneX();
        int y = guiObject.getSceneY();
        int width = guiObject.getWidth();
        int height = guiObject.getHeight();

        int triangleHalfWidth = 7;
        int triangleHalfHeight = 4;
        int triangleX = x + width - triangleHalfWidth - triangleXOffset;
        int triangleY = y + height / 2;
        Vector4f color = comboBox.getLabel().getColor();

        idList.add(triangleId = guiRenderer.add(triangleX, triangleY, triangleHalfWidth << 1, triangleHalfHeight << 1, color));
    }

    @Override
    public void renderBody() {
        super.renderBody();
        guiRenderer.addDrawCommand(triangleId, TRIANGLE_PRIMITIVE.getBaseVertex());
    }
}
