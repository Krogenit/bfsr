package net.bfsr.engine.gui.renderer.inputbox;

import net.bfsr.engine.gui.component.InputBox;

public class InputBoxOutlinedRenderer extends InputBoxRenderer {
    private int innerBody;

    public InputBoxOutlinedRenderer(InputBox inputBox) {
        super(inputBox);
    }

    @Override
    protected void createBody() {
        idList.add(id = guiRenderer.add(guiObject.getSceneX(), guiObject.getSceneY(), guiObject.getWidth(), guiObject.getHeight(),
                outlineColor));
        idList.add(innerBody = guiRenderer.add(guiObject.getSceneX() + 1, guiObject.getSceneY() + 1, guiObject.getWidth() - 2,
                guiObject.getHeight() - 2, color));
    }

    @Override
    protected void renderBody() {
        super.renderBody();
        guiRenderer.addDrawCommand(innerBody);
    }

    @Override
    public void onMouseHover() {
        guiRenderer.setColor(id, outlineHoverColor);
        guiRenderer.setColor(innerBody, hoverColor);
    }

    @Override
    public void onMouseStopHover() {
        guiRenderer.setColor(id, outlineColor);
        guiRenderer.setColor(innerBody, color);
    }

    @Override
    protected void setBodyLastValues() {
        super.setBodyLastValues();
        guiRenderer.setLastPosition(innerBody, lastX + 1, lastY + 1);
        guiRenderer.setLastColor(innerBody, color);
        guiRenderer.setLastColor(id, outlineColor);
    }

    @Override
    public void updatePosition() {
        super.updatePosition();
        guiRenderer.setPosition(innerBody, guiObject.getSceneX() + 1, guiObject.getSceneY() + 1);
    }
}
