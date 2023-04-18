package net.bfsr.client.renderer.font;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

@Getter
@Setter
public
class StringParams {
    private Vector4f color = new Vector4f();
    private int x, y;
    private int height;

    public void addHeight(int height) {
        this.height += height;
    }

    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }

    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }
}