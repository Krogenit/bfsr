package net.bfsr.engine.gui.renderer;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class RectangleTexturedRotatedRenderer extends GuiObjectRenderer {
    private final AbstractTexture texture;

    public RectangleTexturedRotatedRenderer(GuiObject guiObject, TextureRegister texture) {
        super(guiObject);
        this.texture = Engine.assetsManager.getTexture(texture);
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.addRotated(lastX + width / 2, lastY + height / 2, x + width / 2, y + height / 2, guiObject.getLastRotation(),
                    guiObject.getRotation(), width, height, hoverColor, texture);
        } else {
            guiRenderer.addRotated(lastX + width / 2, lastY + height / 2, x + width / 2, y + height / 2, guiObject.getLastRotation(),
                    guiObject.getRotation(), width, height, color, texture);
        }

        super.render(lastX, lastY, x, y, width, height);
    }
}
