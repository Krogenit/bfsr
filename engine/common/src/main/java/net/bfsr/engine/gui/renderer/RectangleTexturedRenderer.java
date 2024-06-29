package net.bfsr.engine.gui.renderer;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.GuiObject;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;

@Getter
public class RectangleTexturedRenderer extends GuiObjectRenderer {
    protected final AbstractTexture texture;

    public RectangleTexturedRenderer(GuiObject guiObject, TextureRegister textureRegister) {
        super(guiObject);
        this.texture = Engine.assetsManager.getTexture(textureRegister);
    }

    @Override
    public void render(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor, texture);
        } else {
            guiRenderer.add(lastX, lastY, x, y, width, height, color, texture);
        }

        super.render(lastX, lastY, x, y, width, height);
    }
}
