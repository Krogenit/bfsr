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

    public void union(float minX, float minY, float maxX, float maxY) {
        this.minX = Math.min(this.minX, minX);
        this.minY = Math.min(this.minY, minY);
        this.maxX = Math.max(this.maxX, maxX);
        this.maxY = Math.max(this.maxY, maxY);
    }

    public void union(AABB aabb) {
        union(aabb.minX, aabb.minY, aabb.maxX, aabb.maxY);
    }
}