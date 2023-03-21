package net.bfsr.client.gui;

import lombok.Getter;
import net.bfsr.client.renderer.instanced.GUIRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.texture.TextureRegister;

public class TexturedGuiObject extends SimpleGuiObject {
    @Getter
    protected Texture texture;

    public TexturedGuiObject(TextureRegister texture) {
        this(texture, 0, 0, 0, 0);
    }

    public TexturedGuiObject(TextureRegister texture, int x, int y, int width, int height) {
        super(x, y, width, height);
        if (texture != null) this.texture = TextureLoader.getTexture(texture);
    }

    @Override
    public TexturedGuiObject setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public void render() {
        if (rotation != 0) {
            GUIRenderer.addGUIElementToRenderPipeLine(lastX, lastY, x, y, lastRotation, rotation, width, height, color.x, color.y, color.z, color.w, texture);
        } else {
            GUIRenderer.addGUIElementToRenderPipeLine(lastX, lastY, x, y, width, height, color.x, color.y, color.z, color.w, texture);
        }
    }

    @Override
    public void renderNoInterpolation() {
        GUIRenderer.addGUIElementToRenderPipeLine(x, y, width, height, color.x, color.y, color.z, color.w, texture);
    }
}