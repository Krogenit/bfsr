package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;

public class RectangleRotatedRenderer extends SimpleGuiObjectRenderer {
    public RectangleRotatedRenderer(GuiObject guiObject) {
        super(guiObject);
    }

    @Override
    protected void create() {
        idList.add(id = guiRenderer.addCentered(guiObject.getSceneX(), guiObject.getSceneY(), guiObject.getWidth(), guiObject.getHeight(),
                guiObject.getColor()));
    }

    @Override
    protected void renderBody() {
        guiRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX);
    }
}
