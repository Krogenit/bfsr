package net.bfsr.client.gui.component;

import lombok.Setter;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.engine.Engine;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.TextureRegister;

public class CheckBox extends TexturedGuiObject {
    @Setter
    private boolean value;

    public CheckBox(TextureRegister textureRegister, int width, int height, boolean value) {
        super(textureRegister);
        this.value = value;
        setSize(width, height);
    }

    @Override
    public boolean onMouseLeftClick() {
        if (!isMouseHover()) return false;
        value = !value;
        return true;
    }

    @Override
    public void render() {
        renderNoInterpolation();
    }

    @Override
    public void renderNoInterpolation() {
        super.renderNoInterpolation();

        if (value) {
            Engine.renderer.guiRenderer.render();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            Engine.renderer.guiRenderer.addPrimitive(x + 4, centerY, centerX - 2, y + height - 6, centerX - 2, y + height - 6, x + width - 4, y + 5, 0.9f, 0.9f, 0.9f, 1.0f, 0);
            Engine.renderer.glLineWidth(3.0f);
            Engine.renderer.guiRenderer.render(GL.GL_LINES);
            Engine.renderer.glLineWidth(1.0f);
        }
    }

    public boolean isChecked() {
        return value;
    }
}