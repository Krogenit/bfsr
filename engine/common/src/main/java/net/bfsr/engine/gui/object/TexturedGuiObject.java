package net.bfsr.engine.gui.object;

import lombok.Getter;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.TextureRegister;
import org.joml.Vector4f;

public class TexturedGuiObject extends SimpleGuiObject {
    @Getter
    protected AbstractTexture texture;
    private boolean centered;

    public TexturedGuiObject(TextureRegister texture) {
        this(texture, 0, 0);
    }

    public TexturedGuiObject(TextureRegister texture, int width, int height) {
        super(width, height);
        if (texture != null) this.texture = Engine.assetsManager.getTexture(texture);
    }

    public TexturedGuiObject(TextureRegister texture, int x, int y, int width, int height) {
        super(x, y, width, height);
        if (texture != null) this.texture = Engine.assetsManager.getTexture(texture);
    }

    @Override
    public TexturedGuiObject setSize(int width, int height) {
        super.setSize(width, height);
        return this;
    }

    @Override
    public void render() {
        if (texture != null) {
            Vector4f color = isMouseHover() ? hoverColor : this.color;

            if (rotation != 0.0f) {
                if (centered) {
                    guiRenderer.add(lastX + width / 2, lastY + height / 2, x + width / 2, y + height / 2, lastRotation, rotation,
                            width, height, color.x, color.y, color.z, color.w, texture);
                } else {
                    guiRenderer.add(lastX, lastY, x, y, lastRotation, rotation, width, height, color.x, color.y, color.z, color.w,
                            texture);
                }
            } else {
                guiRenderer.add(lastX, lastY, x, y, width, height, color.x, color.y, color.z, color.w, texture);
            }
        } else {
            super.render();
        }
    }

    @Override
    public void renderNoInterpolation() {
        if (texture != null) {
            guiRenderer.add(x, y, width, height, color.x, color.y, color.z, color.w, texture);
        } else {
            super.renderNoInterpolation();
        }
    }

    public TexturedGuiObject centered() {
        centered = true;
        return this;
    }
}