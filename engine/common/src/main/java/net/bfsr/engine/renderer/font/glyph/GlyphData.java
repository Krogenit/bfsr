package net.bfsr.engine.renderer.font.glyph;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.renderer.primitive.PrimitiveData;

@Getter
public class GlyphData extends PrimitiveData {
    private float x, y;
    private float width, height;
    @Setter
    private int baseInstance = -1;
    private final float r, g, b, a;
    private final long textureHandle;

    public GlyphData(int baseVertex, float x, float y, float r, float g, float b, float a, long textureHandle) {
        this(baseVertex, x, y, 1.0f, 1.0f, r, g, b, a, textureHandle);
    }

    public GlyphData(int baseVertex, float x, float y, float width, float height, float r, float g, float b, float a, long textureHandle) {
        super(baseVertex);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.textureHandle = textureHandle;
    }

    public void scale(float sx, float sy) {
        x *= sx;
        y *= sy;
        width *= sx;
        height *= sy;
    }
}
