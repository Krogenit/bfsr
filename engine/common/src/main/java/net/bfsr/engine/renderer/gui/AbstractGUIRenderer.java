package net.bfsr.engine.renderer.gui;

import net.bfsr.engine.renderer.texture.AbstractTexture;

public abstract class AbstractGUIRenderer {
    public abstract void init();

    public abstract void render();
    public abstract void render(int mode);

    public abstract void add(float lastX, float lastY, float x, float y, float lastSin, float lastCos, float sin, float cos, float sizeX, float sizeY,
                             float r, float g, float b, float a, AbstractTexture texture);
    public abstract void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY,
                             float r, float g, float b, float a, AbstractTexture texture);
    public abstract void add(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a,
                             AbstractTexture texture);
    public abstract void add(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, AbstractTexture texture);
    public abstract void add(float x, float y, float rotation, float sizeX, float sizeY, float r, float g, float b, float a, AbstractTexture texture);
    public abstract void add(float lastX, float lastY, float x, float y, float lastRotation, float rotation, float sizeX, float sizeY,
                             float r, float g, float b, float a);
    public abstract void add(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a);
    public abstract void add(float lastX, float lastY, float x, float y, float sizeX, float sizeY, float r, float g, float b, float a);
    public abstract void addCentered(float x, float y, float sizeX, float sizeY, float r, float g, float b, float a, AbstractTexture texture);
    public abstract void addPrimitive(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float r, float g, float b, float a,
                                      long textureHandle);
}