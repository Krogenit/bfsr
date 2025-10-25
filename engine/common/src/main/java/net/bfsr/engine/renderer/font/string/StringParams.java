package net.bfsr.engine.renderer.font.string;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

@Getter
class StringParams {
    private final Vector4f color = new Vector4f();
    @Setter
    private float x, y;

    void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }
}