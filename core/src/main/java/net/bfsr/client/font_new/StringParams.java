package net.bfsr.client.font_new;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

@Getter
@Setter
public class StringParams {
    private Vector4f color = new Vector4f();
    private float x, y;
    private float scale;
    private int height;

    public void addHeight(int height) {
        this.height += height;
    }
}
