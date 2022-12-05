package net.bfsr.client.font_new;

import lombok.Getter;

@Getter
public class GlyphAndColor {
    private final float x1, y1, x2, y2;
    private final float u1, v1, u2, v2;
    private final float r, g, b, a;

    public GlyphAndColor(float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2, float r, float g, float b, float a) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
