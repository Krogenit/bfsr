package net.bfsr.engine.renderer.primitive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class Primitive {
    private final float x1;
    private final float y1;
    private final float u1;
    private final float v1;
    private final float x2;
    private final float y2;
    private final float u2;
    private final float v2;
    private final float x3;
    private final float y3;
    private final float u3;
    private final float v3;
    private final float x4;
    private final float y4;
    private final float u4;
    private final float v4;

    @Setter
    private int baseVertex;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Primitive primitive = (Primitive) o;
        return Float.compare(x1, primitive.x1) == 0 && Float.compare(y1, primitive.y1) == 0 &&
                Float.compare(u1, primitive.u1) == 0 && Float.compare(v1, primitive.v1) == 0 &&
                Float.compare(x2, primitive.x2) == 0 && Float.compare(y2, primitive.y2) == 0 &&
                Float.compare(u2, primitive.u2) == 0 && Float.compare(v2, primitive.v2) == 0 &&
                Float.compare(x3, primitive.x3) == 0 && Float.compare(y3, primitive.y3) == 0 &&
                Float.compare(u3, primitive.u3) == 0 && Float.compare(v3, primitive.v3) == 0 &&
                Float.compare(x4, primitive.x4) == 0 && Float.compare(y4, primitive.y4) == 0 &&
                Float.compare(u4, primitive.u4) == 0 && Float.compare(v4, primitive.v4) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, y1, u1, v1, x2, y2, u2, v2, x3, y3, u3, v3, x4, y4, u4, v4);
    }
}
