package net.bfsr.collision;

import org.joml.Vector2d;
import org.joml.Vector2f;

public class AxisAlignedBoundingBox {
    private final Vector2f min;
    private final Vector2f max;

    private static final AxisAlignedBoundingBox TRANSLATED_AABB = new AxisAlignedBoundingBox(new Vector2f(), new Vector2f());

    public AxisAlignedBoundingBox() {
        this(new Vector2f(), new Vector2f());
    }

    public AxisAlignedBoundingBox(Vector2f min, Vector2f max) {
        this.max = max;
        this.min = min;
    }

    public AxisAlignedBoundingBox(float minX, float minY, float maxX, float maxY) {
        this(new Vector2f(minX, minY), new Vector2f(maxX, maxY));
    }

    public void set(float minX, float minY, float maxX, float maxY) {
        min.x = minX;
        min.y = minY;
        max.x = maxX;
        max.y = maxY;
    }

    public void setMinX(float x) {
        min.x = x;
    }

    public void setMinY(float y) {
        min.y = y;
    }

    public void setMaxX(float x) {
        max.x = x;
    }

    public void setMaxY(float y) {
        max.y = y;
    }

    public boolean isIntersects(Vector2f vector) {
        return vector.x >= min.x && vector.x < max.x && vector.y >= min.y && vector.y < max.y;
    }

    public boolean isIntersects(Vector2d vector) {
        return vector.x >= min.x && vector.x < max.x && vector.y >= min.y && vector.y < max.y;
    }

    public boolean isIntersects(AxisAlignedBoundingBox aabb) {
        return min.x <= aabb.max.x && max.x >= aabb.min.x && min.y <= aabb.max.y && max.y >= aabb.min.y;
    }

    @Override
    public String toString() {
        return "AABB [min=" + min + ", max=" + max + "]";
    }

    public AxisAlignedBoundingBox translate(Vector2f pos) {
        TRANSLATED_AABB.min.x = min.x + pos.x;
        TRANSLATED_AABB.min.y = min.y + pos.y;
        TRANSLATED_AABB.max.x = max.x + pos.x;
        TRANSLATED_AABB.max.y = max.y + pos.y;
        return TRANSLATED_AABB;
    }
}
