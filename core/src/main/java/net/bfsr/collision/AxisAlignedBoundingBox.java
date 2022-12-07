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

    public void set(float minX, float minY, float maxX, float maxY) {
        this.min.x = minX;
        this.min.y = minY;
        this.max.x = maxX;
        this.max.y = maxY;
    }

    public void setMinX(float x) {
        this.min.x = x;
    }

    public void setMinY(float y) {
        this.min.y = y;
    }

    public void setMaxX(float x) {
        this.max.x = x;
    }

    public void setMaxY(float y) {
        this.max.y = y;
    }

    public boolean isIntersects(Vector2f vect) {
        return vect.x >= min.x && vect.x < max.x && vect.y >= min.y && vect.y < max.y;
    }

    public boolean isIntersects(Vector2d vect) {
        return vect.x >= min.x && vect.x < max.x && vect.y >= min.y && vect.y < max.y;
    }

    public boolean isIntersects(AxisAlignedBoundingBox aabb) {
        return min.x <= aabb.max.x && max.x >= aabb.min.x && min.y <= aabb.max.y && max.y >= aabb.min.y;
    }

    @Override
    public String toString() {
        return "AABB [min=" + min + ", max=" + max + "]";
    }

    public Vector2f getMax() {
        return max;
    }

    public Vector2f getMin() {
        return min;
    }

    public AxisAlignedBoundingBox translate(Vector2f pos) {
        TRANSLATED_AABB.min.x = min.x + pos.x;
        TRANSLATED_AABB.min.y = min.y + pos.y;
        TRANSLATED_AABB.max.x = max.x + pos.x;
        TRANSLATED_AABB.max.y = max.y + pos.y;
        return TRANSLATED_AABB;
    }
}