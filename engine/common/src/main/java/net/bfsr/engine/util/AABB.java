package net.bfsr.engine.util;

import lombok.Getter;

@Getter
public class AABB {
    private float minX, minY, maxX, maxY;

    public void set(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public AABB set(double minX, double minY, double maxX, double maxY) {
        this.minX = (float) minX;
        this.minY = (float) minY;
        this.maxX = (float) maxX;
        this.maxY = (float) maxY;
        return this;
    }

    public boolean overlaps(AABB aabb) {
        return this.minX <= aabb.maxX && this.maxX >= aabb.minX && this.minY <= aabb.maxY && this.maxY >= aabb.minY;
    }
}