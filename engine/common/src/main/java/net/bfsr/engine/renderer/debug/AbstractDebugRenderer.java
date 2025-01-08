package net.bfsr.engine.renderer.debug;

import org.jbox2d.collision.AABB;
import org.joml.Vector4f;

public interface AbstractDebugRenderer {
    void init();
    void addCommand(int count);
    void addVertex(float x, float y, Vector4f color);
    void render(int mode);
    void renderAABB(AABB aabb, Vector4f color);
    void reload();
    void clear();
    void reset();
}