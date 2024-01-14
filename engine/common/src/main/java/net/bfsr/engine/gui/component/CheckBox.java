package net.bfsr.engine.gui.component;

import lombok.Setter;
import net.bfsr.engine.gui.object.TexturedGuiObject;
import net.bfsr.engine.renderer.opengl.GL;
import net.bfsr.engine.renderer.texture.TextureRegister;

@Setter
public class CheckBox extends TexturedGuiObject {
    private boolean value;

    public CheckBox(TextureRegister texture, int width, int height, boolean value) {
        super(texture);
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
            guiRenderer.render();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            guiRenderer.addPrimitive(x + 4, centerY, centerX - 2, y + height - 6, centerX - 2, y + height - 6,
                    x + width - 4, y + 5, 0.9f, 0.9f, 0.9f, 1.0f, 0);
            renderer.lineWidth(3.0f);
            guiRenderer.render(GL.GL_LINES);
            renderer.lineWidth(1.0f);
        }
    }

    public boolean isChecked() {
        return value;
    }
}