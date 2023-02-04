package net.bfsr.client.render.font;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

@Getter
@Setter
class StringParams {
    private Vector4f color = new Vector4f();
    private float x, y;
    private int height;

    void addHeight(int height) {
        this.height += height;
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }
}
