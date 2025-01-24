package net.bfsr.engine.gui.component;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.renderer.RectangleTexturedRotatedRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class TexturedRotatedRectangle extends GuiObject {
    private final RectangleTexturedRotatedRenderer renderer;

    public TexturedRotatedRectangle(TextureRegister textureRegister, int width, int height) {
        this(Engine.getAssetsManager().getTexture(textureRegister), width, height);
    }

    public TexturedRotatedRectangle(AbstractTexture texture, int width, int height) {
        super(width, height);
        setRenderer(renderer = new RectangleTexturedRotatedRenderer(this, texture));
    }

    public void setTexture(AbstractTexture texture) {
        renderer.setTexture(texture);
    }

    public AbstractTexture getTexture() {
        return renderer.getTexture();
    }
}
