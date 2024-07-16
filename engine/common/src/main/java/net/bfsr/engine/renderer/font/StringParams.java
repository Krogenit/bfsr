package net.bfsr.engine.renderer.font;

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

    void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }
}