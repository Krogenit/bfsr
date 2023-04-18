package net.bfsr.client.gui.component;

import lombok.Setter;
import net.bfsr.client.gui.TexturedGuiObject;
import net.bfsr.client.renderer.gui.GUIRenderer;
import net.bfsr.texture.TextureRegister;
import org.lwjgl.opengl.GL11C;

public class CheckBox extends TexturedGuiObject {
    @Setter
    private boolean value;

    public CheckBox(TextureRegister textureRegister, int width, int height, boolean value) {
        super(textureRegister);
        this.value = value;
        setSize(width, height);
    }

    public CheckBox(int width, int height, boolean value) {
        this(null, width, height, value);
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
            GUIRenderer.get().render();
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            GUIRenderer.get().addPrimitive(x + 4, centerY, centerX - 2, y + height - 6, centerX - 2, y + height - 6, x + width - 4, y + 5, 0.9f, 0.9f, 0.9f, 1.0f, 0);
            GL11C.glLineWidth(3.0f);
            GUIRenderer.get().render(GL11C.GL_LINES);
            GL11C.glLineWidth(1.0f);
        }
    }

    public boolean isChecked() {
        return value;
    }
}