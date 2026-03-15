package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.texture.AbstractTexture;

public class SimpleGuiObjectRenderer extends GuiObjectRenderer {
    protected int id = -1;

    public SimpleGuiObjectRenderer(GuiObject guiObject) {
        super(guiObject);
    }

    @Override
    protected void create() {
        idList.add(id = guiRenderer.add(guiObject.getSceneX(), guiObject.getSceneY(), guiObject.getWidth(), guiObject.getHeight(),
                activeColor));
    }

    @Override
    protected void setLastUpdateValues() {
        setBodyLastValues();
    }

    protected void setBodyLastValues() {
        guiRenderer.setLastPosition(id, lastX, lastY);
        guiRenderer.setLastSize(id, guiObject.getWidth(), guiObject.getHeight());
        guiRenderer.setLastColor(id, activeColor);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        renderBody();
        super.render(mouseX, mouseY);
    }

    protected void renderBody() {
        guiRenderer.addDrawCommand(id);
    }

    @Override
    public void onMouseHover() {
        activeColor = hoverColor;
        activeOutlineColor = outlineHoverColor;
        guiRenderer.setColor(id, activeColor);
    }

    @Override
    public void onMouseStopHover() {
        activeColor = color;
        activeOutlineColor = outlineColor;
        guiRenderer.setColor(id, activeColor);
    }

    @Override
    public void updatePosition() {
        guiRenderer.setPosition(id, guiObject.getSceneX(), guiObject.getSceneY());
    }

    @Override
    public void updatePosition(int x, int y) {
        guiRenderer.setPosition(id, x, y);
    }

    @Override
    public void updateRotation() {
        guiRenderer.setRotation(id, guiObject.getRotation());
    }

    @Override
    public void updateSize() {
        guiRenderer.setSize(id, guiObject.getWidth(), guiObject.getHeight());
    }

    @Override
    public void updateColor() {
        guiRenderer.setColor(id, guiObject.getColor());
    }

    public void setTexture(AbstractTexture texture) {
        guiRenderer.setTexture(id, texture.getTextureHandle());
    }
}
