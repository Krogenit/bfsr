package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.GuiObject;

public class RectangleOutlinedRenderer extends RectangleRenderer {
    private int bodyId;

    public RectangleOutlinedRenderer(GuiObject guiObject) {
        super(guiObject);
    }

    @Override
    protected void create() {
        super.create();
        guiRenderer.setColor(id, outlineColor);
        idList.add(bodyId = guiRenderer.add(guiObject.getSceneX() + 1, guiObject.getSceneY() + 1, guiObject.getWidth() - 2,
                guiObject.getHeight() - 2, color));
    }

    @Override
    protected void setLastUpdateValues() {
        super.setLastUpdateValues();
        guiRenderer.setLastPosition(bodyId, guiObject.getSceneX() + 1, guiObject.getSceneY() + 1);
        guiRenderer.setLastSize(bodyId, guiObject.getWidth() - 2, guiObject.getHeight() - 2);
    }

    @Override
    protected void renderBody() {
        super.renderBody();
        guiRenderer.addDrawCommand(bodyId);
    }

    @Override
    public void onMouseHover() {
        guiRenderer.setColor(id, outlineHoverColor);
        guiRenderer.setColor(bodyId, hoverColor);
    }

    @Override
    public void onMouseStopHover() {
        guiRenderer.setColor(id, outlineColor);
        guiRenderer.setColor(bodyId, color);
    }

    @Override
    public void updatePosition() {
        super.updatePosition();
        guiRenderer.setPosition(bodyId, guiObject.getSceneX() + 1, guiObject.getSceneY() + 1);
    }

    @Override
    public void updateSize() {
        super.updateSize();
        guiRenderer.setSize(bodyId, guiObject.getWidth() - 2, guiObject.getHeight() - 2);
    }
}