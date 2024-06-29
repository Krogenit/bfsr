package net.bfsr.engine.gui.component;

import net.bfsr.engine.gui.renderer.RectangleTexturedRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class TexturedRectangle extends GuiObject {
    private RectangleTexturedRenderer renderer;

    public TexturedRectangle(TextureRegister textureRegister) {
        setRenderer(new RectangleTexturedRenderer(this, textureRegister));
    }

    public TexturedRectangle(TextureRegister textureRegister, int x, int y, int width, int height) {
        super(x, y, width, height);
        setRenderer(new RectangleTexturedRenderer(this, textureRegister));
    }

    public TexturedRectangle(TextureRegister textureRegister, int width, int height) {
        super(width, height);
        setRenderer(new RectangleTexturedRenderer(this, textureRegister));
    }

    public void setRenderer(RectangleTexturedRenderer renderer) {
        this.renderer = renderer;
        super.setRenderer(renderer);
    }

    public AbstractTexture getTexture() {
        return renderer.getTexture();
    }
}
