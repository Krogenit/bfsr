package net.bfsr.engine.renderer.gui;

import net.bfsr.engine.event.EventBus;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import org.joml.Vector4f;

public abstract class AbstractGUIRenderer {
    public abstract void init();

    public abstract void render();
    public abstract void render(int mode);

    public abstract void add(float x, float y, float width, float height, Vector4f color);
    public abstract void add(float x, float y, float width, float height, float r, float g, float b, float a);
    public abstract void add(float lastX, float lastY, float x, float y, float width, float height, Vector4f color);
    public abstract void add(float lastX, float lastY, float x, float y, float width, float height, float r, float g, float b, float a);

    public abstract void add(float lastX, float lastY, float x, float y, float width, float height, Vector4f color,
                             AbstractTexture texture);
    public abstract void add(float lastX, float lastY, float x, float y, float width, float height, float r, float g, float b, float a,
                             AbstractTexture texture);
    public abstract void add(float x, float y, float width, float height, Vector4f color, AbstractTexture texture);
    public abstract void add(float x, float y, float width, float height, float r, float g, float b, float a, AbstractTexture texture);

    public abstract void addRotated(float x, float y, float rotation, float width, float height, Vector4f color);
    public abstract void addRotated(float x, float y, float lastRotation, float rotation, float width, float height, Vector4f color);
    public abstract void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width,
                                    float height, float r, float g, float b, float a);
    public abstract void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width,
                                    float height, Vector4f color);
    public abstract void addRotated(float x, float y, float rotation, float width, float height, Vector4f color, AbstractTexture texture);
    public abstract void addRotated(float x, float y, float rotation, float width, float height, float r, float g, float b, float a,
                                    AbstractTexture texture);
    public abstract void addRotated(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos,
                                    float width, float height, float r, float g, float b, float a, AbstractTexture texture);
    public abstract void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width,
                                    float height, Vector4f color, AbstractTexture texture);
    public abstract void addRotated(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float width,
                                    float height, float r, float g, float b, float a, AbstractTexture texture);

    public abstract void addCentered(float x, float y, float width, float height, float r, float g, float b, float a,
                                     AbstractTexture texture);

    public abstract void addPrimitive(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float r, float g,
                                      float b, float a, long textureHandle);

    public abstract EventBus getEventBus();
}