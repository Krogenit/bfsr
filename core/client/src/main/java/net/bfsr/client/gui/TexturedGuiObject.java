package net.bfsr.client.gui;

import lombok.Getter;
import net.bfsr.client.renderer.gui.GUIRenderer;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.texture.TextureRegister;

public class TexturedGuiObject extends SimpleGuiObject {
    @Getter
    protected Texture texture;
    private boolean centered;

    public TexturedGuiObject(TextureRegister texture) {
        this(texture, 0, 0);
    }

    public TexturedGuiObject(TextureRegister texture, int width, int height) {
        super(width, height);
        if (texture != null) this.texture = TextureLoader.getTexture(texture);
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
        if (texture != null) {
            if (rotation != 0.0f) {
                if (centered) {
                    GUIRenderer.get().add(lastX + width / 2, lastY + height / 2, x + width / 2, y + height / 2, lastRotation, rotation, width, height,
                            color.x, color.y, color.z, color.w, texture);
                } else {
                    GUIRenderer.get().add(lastX, lastY, x, y, lastRotation, rotation, width, height, color.x, color.y, color.z, color.w, texture);
                }
            } else {
                GUIRenderer.get().add(lastX, lastY, x, y, width, height, color.x, color.y, color.z, color.w, texture);
            }
        } else {
            super.render();
        }
    }

    @Override
    public void renderNoInterpolation() {
        if (texture != null) {
            GUIRenderer.get().add(x, y, width, height, color.x, color.y, color.z, color.w, texture);
        } else {
            super.renderNoInterpolation();
        }
    }

    public TexturedGuiObject centered() {
        centered = true;
        return this;
    }
}