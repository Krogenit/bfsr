package net.bfsr.client.gui;

import lombok.Getter;
import net.bfsr.client.render.OpenGLHelper;
import net.bfsr.client.render.Renderer;
import net.bfsr.client.render.texture.Texture;
import net.bfsr.client.render.texture.TextureLoader;
import net.bfsr.client.render.texture.TextureRegister;
import net.bfsr.client.shader.BaseShader;

public class TexturedGuiObject extends SimpleGuiObject {
    @Getter
    protected Texture texture;

    public TexturedGuiObject(TextureRegister texture) {
        this(texture, 0, 0, 0, 0);
    }

    public TexturedGuiObject(TextureRegister texture, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.texture = TextureLoader.getTexture(texture);
    }

    @Override
    public TexturedGuiObject setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public void render(BaseShader shader) {
        shader.setColor(color.x, color.y, color.z, color.w);
        shader.enableTexture();
        OpenGLHelper.bindTexture(texture.getId());
        shader.setModelMatrix(modelMatrixBuffer);
        Renderer.quad.renderIndexed();
    }
}