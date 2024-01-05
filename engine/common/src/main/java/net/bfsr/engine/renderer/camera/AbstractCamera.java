package net.bfsr.engine.renderer.camera;

import net.bfsr.engine.util.AABB;
import org.joml.Vector2f;

public abstract class AbstractCamera {
    public abstract void init(int width, int height);
    public abstract void resize(int width, int height);
    public abstract void update();
    public abstract void calculateInterpolatedViewMatrix(float interpolation);
    public abstract void bindInterpolatedWorldViewMatrix();
    public abstract void bindWorldViewMatrix();
    public abstract void bindGUI();

    public abstract void zoom(float v);
    public abstract void move(float x, float y);
    public abstract void moveByMouse(float x, float y);

    public abstract void setBoundingBox(float minX, float minY, float maxX, float maxY);
    public abstract float getLastZoom();
    public abstract float getZoom();
    public abstract Vector2f getLastPosition();
    public abstract Vector2f getPosition();
    public abstract Vector2f getWorldVector(Vector2f position);
    public abstract AABB getBoundingBox();
    public abstract Vector2f getOrigin();
}