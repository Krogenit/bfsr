package net.bfsr.engine.gui.component;

import net.bfsr.engine.gui.renderer.RectangleTexturedRotatedRenderer;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class TexturedRotatedRectangle extends GuiObject {
    public TexturedRotatedRectangle(TextureRegister textureRegister) {
        setRenderer(new RectangleTexturedRotatedRenderer(this, textureRegister));
    }

    public TexturedRotatedRectangle(TextureRegister textureRegister, int x, int y, int width, int height) {
        super(x, y, width, height);
        setRenderer(new RectangleTexturedRotatedRenderer(this, textureRegister));
    }

    public TexturedRotatedRectangle(TextureRegister textureRegister, int width, int height) {
        super(width, height);
        setRenderer(new RectangleTexturedRotatedRenderer(this, textureRegister));
    }
}
