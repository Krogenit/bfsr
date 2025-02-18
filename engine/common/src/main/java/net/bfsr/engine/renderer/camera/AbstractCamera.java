package net.bfsr.engine.renderer.camera;

import net.bfsr.engine.renderer.AbstractRenderer;
import org.jbox2d.collision.AABB;
import org.joml.Vector2f;
import org.joml.Vector2i;

public interface AbstractCamera {
    void init(int width, int height, AbstractRenderer renderer);
    void resize(int width, int height);
    void update();
    void calculateInterpolatedViewMatrix(float interpolation);
    void bindWorldViewMatrix();
    void bindGUI();

    void zoom(float value);
    void move(float x, float y);
    void moveByMouse(float dx, float dy);

    void setBoundingBox(float minX, float minY, float maxX, float maxY);
    void setPosition(float x, float y);

    float getZoom();
    Vector2f getLastPosition();
    Vector2f getPosition();
    Vector2f getWorldVector(Vector2i position);
    Vector2f getWorldVector(float x, float y);
    AABB getBoundingBox();
    Vector2f getOrigin();
    void clear();
}