package net.bfsr.engine.renderer.debug;

import net.bfsr.engine.util.AABB;
import org.joml.Vector4f;

public abstract class AbstractDebugRenderer {
    public abstract void init();
    public abstract void bind();
    public abstract void addCommand(int count);
    public abstract void addVertex(float x, float y, Vector4f color);
    public abstract void render(int mode);
    public abstract void renderAABB(AABB aabb);
    public abstract void clear();
}