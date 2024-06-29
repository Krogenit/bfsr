package net.bfsr.engine.gui.renderer.inputbox;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.component.InputBox;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class TexturedInputBoxRenderer extends InputBoxRenderer {
    private final AbstractTexture texture;

    public TexturedInputBoxRenderer(InputBox inputBox, TextureRegister textureRegister) {
        super(inputBox);
        this.texture = Engine.assetsManager.getTexture(textureRegister);
    }

    @Override
    protected void renderBody(int lastX, int lastY, int x, int y, int width, int height) {
        if (guiObject.isMouseHover()) {
            guiRenderer.add(lastX, lastY, x, y, width, height, hoverColor, texture);
        } else {
            guiRenderer.add(lastX, lastY, x, y, width, height, color, texture);
        }
    }
}
