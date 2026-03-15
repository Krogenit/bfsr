package net.bfsr.engine.renderer.debug;

import net.bfsr.engine.renderer.constant.DrawMode;
import org.jbox2d.collision.AABB;
import org.joml.Vector4f;

public interface AbstractDebugRenderer {
    void init();
    void addCommand(int count);
    void addVertex(float x, float y, Vector4f color);
    void render(DrawMode mode);
    void renderAABB(AABB aabb, Vector4f color);
    void reloadShaders();
    void clear();
    void reset();
}