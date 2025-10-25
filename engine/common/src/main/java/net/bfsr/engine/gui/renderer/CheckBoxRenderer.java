package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.CheckBox;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.primitive.Primitive;

public class CheckBoxRenderer extends RectangleOutlinedRenderer {
    private static final Primitive CHECK_PRIMITIVE = new Primitive(-0.2f, -0.4f, 0.0f, 1.0f, -0.5f, 0.25f, 1.0f,
            1.0f, -0.5f, 0.25f, 1.0f, 0.0f, 0.5f, 0.5f, 0.0f, 0.0f);

    private final CheckBox checkBox;
    private int checkId;

    public CheckBoxRenderer(CheckBox checkBox) {
        super(checkBox);
        this.checkBox = checkBox;
    }

    @Override
    protected void create() {
        super.create();

        guiRenderer.addPrimitive(CHECK_PRIMITIVE);

        int x = guiObject.getSceneX();
        int y = guiObject.getSceneY();
        int width = guiObject.getWidth();
        int height = guiObject.getHeight();

        idList.add(checkId = guiRenderer.addCentered(x + 4, y + 4, width - 8, height - 8, 205 / 255.0f, 205 / 255.0f, 205 / 255.0f, 1.0f));
    }

    @Override
    public void renderBody() {
        super.renderBody();
        if (checkBox.isChecked()) {
            guiRenderer.render();
            guiRenderer.addDrawCommand(checkId, CHECK_PRIMITIVE.getBaseVertex());
            renderer.lineWidth(2.0f);
            guiRenderer.render(GL.GL_LINES);
            renderer.lineWidth(1.0f);
        }
    }

    @Override
    protected void setLastUpdateValues() {
        super.setLastUpdateValues();

        int x = guiObject.getSceneX();
        int y = guiObject.getSceneY();
        int width = guiObject.getWidth();
        int height = guiObject.getHeight();
        guiRenderer.setLastPosition(checkId, x + 4 + (width - 8) / 2, y + 4 + (height - 8) / 2);
    }

    @Override
    public void updatePosition() {
        super.updatePosition();

        int x = guiObject.getSceneX();
        int y = guiObject.getSceneY();
        int width = guiObject.getWidth();
        int height = guiObject.getHeight();
        guiRenderer.setPosition(checkId, x + 4 + (width - 8) / 2, y + 4 + (height - 8) / 2);
    }
}
