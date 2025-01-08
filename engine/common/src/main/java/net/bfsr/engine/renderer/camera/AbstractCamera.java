package net.bfsr.engine.renderer.camera;

import org.jbox2d.collision.AABB;
import org.joml.Vector2f;

public interface AbstractCamera {
    void init(int width, int height);
    void resize(int width, int height);
    void update();
    void calculateInterpolatedViewMatrix(float interpolation);
    void bindWorldViewMatrix();
    void bindGUI();

    void zoom(float value);
    void move(float x, float y);
    void moveByMouse(float dx, float dy);

    void setBoundingBox(float minX, float minY, float maxX, float maxY);
    float getZoom();
    Vector2f getLastPosition();
    Vector2f getPosition();
    Vector2f getWorldVector(Vector2f position);
    AABB getBoundingBox();
    Vector2f getOrigin();
    void clear();
}