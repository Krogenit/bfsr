package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.AbstractSpriteRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;

public class RectangleTexturedRotatedRenderer extends RectangleTexturedRenderer {
    public RectangleTexturedRotatedRenderer(GuiObject guiObject, AbstractTexture texture) {
        super(guiObject, texture);
    }

    @Override
    public void create() {
        idList.add(id = guiRenderer.addCentered(guiObject.getSceneX(), guiObject.getSceneY(), guiObject.getRotation(), guiObject.getWidth(),
                guiObject.getHeight(), guiObject.getColor(), texture));
    }

    @Override
    protected void setBodyLastValues() {
        guiRenderer.setLastPosition(id, lastX + guiObject.getWidth() * 0.5f, lastY + guiObject.getHeight() * 0.5f);
    }

    @Override
    public void updatePosition() {
        guiRenderer.setPosition(id, guiObject.getSceneX() + guiObject.getWidth() * 0.5f,
                guiObject.getSceneY() + guiObject.getHeight() * 0.5f);
    }

    @Override
    protected void renderBody() {
        guiRenderer.addDrawCommand(id, AbstractSpriteRenderer.CENTERED_QUAD_BASE_VERTEX);
    }
}
