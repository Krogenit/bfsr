package net.bfsr.engine.gui.component;

import net.bfsr.engine.Engine;
import net.bfsr.engine.gui.renderer.RectangleTexturedRenderer;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureData;

public class TexturedRectangle extends GuiObject {
    private RectangleTexturedRenderer renderer;

    public TexturedRectangle(TextureData textureData, int x, int y, int width, int height) {
        super(x, y, width, height);
        setRenderer(new RectangleTexturedRenderer(this, Engine.getAssetsManager().getTexture(textureData)));
    }

    public TexturedRectangle(TextureData textureData, int width, int height) {
        this(textureData, 0, 0, width, height);
    }

    public TexturedRectangle(TextureData textureData) {
        this(textureData, 0, 0);
    }

    public void setRenderer(RectangleTexturedRenderer renderer) {
        this.renderer = renderer;
        super.setRenderer(renderer);
    }

    public AbstractTexture getTexture() {
        return renderer.getTexture();
    }
}
