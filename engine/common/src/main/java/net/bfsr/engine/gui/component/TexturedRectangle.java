package net.bfsr.engine.gui.component;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.renderer.RectangleTexturedRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class TexturedRectangle extends GuiObject {
    private RectangleTexturedRenderer renderer;

    public TexturedRectangle(TextureRegister textureRegister) {
        this(textureRegister, 0, 0, 0, 0);
    }

    public TexturedRectangle(AbstractTexture texture) {
        this(texture, 0, 0);
    }

    public TexturedRectangle(TextureRegister textureRegister, int width, int height) {
        this(textureRegister, 0, 0, width, height);
    }

    public TexturedRectangle(AbstractTexture texture, int width, int height) {
        this(texture, 0, 0, width, height);
    }

    public TexturedRectangle(TextureRegister textureRegister, int x, int y, int width, int height) {
        this(Engine.assetsManager.getTexture(textureRegister), x, y, width, height);
    }

    public TexturedRectangle(AbstractTexture texture, int x, int y, int width, int height) {
        super(x, y, width, height);
        setRenderer(new RectangleTexturedRenderer(this, texture));
    }

    public void setRenderer(RectangleTexturedRenderer renderer) {
        this.renderer = renderer;
        super.setRenderer(renderer);
    }

    public AbstractTexture getTexture() {
        return renderer.getTexture();
    }
}
