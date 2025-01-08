package net.bfsr.engine.renderer.gui;

import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.primitive.Primitive;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import org.joml.Vector4f;

import java.nio.IntBuffer;

public abstract class AbstractGUIRenderer {
    public abstract void init();

    public abstract void addPrimitive(Primitive primitive);

    public abstract void render();
    public abstract void render(int mode);

    public abstract int add(int x, int y, int width, int height, Vector4f color);
    public abstract int add(int x, int y, int width, int height, float r, float g, float b, float a);
    public abstract int add(int x, int y, float sin, float cos, int width, int height, float r, float g, float b, float a);
    public abstract int add(int x, int y, float sin, float cos, int width, int height, float r, float g, float b, float a,
                            AbstractTexture texture);

    public abstract int add(int x, int y, int width, int height, Vector4f color, AbstractTexture texture);
    public abstract void add(int x, int y, int width, int height, float r, float g, float b, float a, AbstractTexture texture);
    public abstract int add(int x, int y, int width, int height, float r, float g, float b, float a, long textureHandle);
    public abstract int add(int x, int y, float sin, float cos, int width, int height, float r, float g, float b, float a,
                            long textureHandle, int font);

    public abstract int addCentered(int x, int y, int width, int height, Vector4f color);
    public abstract int addCentered(int x, int y, int width, int height, float r, float g, float b, float a);
    public abstract int addCentered(int x, int y, float rotation, int width, int height, Vector4f color, AbstractTexture texture);
    public abstract int addCentered(int x, int y, float sin, float cos, int width, int height, Vector4f color, AbstractTexture texture);
    public abstract int addCentered(int x, int y, float sin, float cos, int width, int height, float r, float g, float b, float a,
                                    long textureHandle);

    public abstract void addDrawCommand(IntBuffer commandBuffer, int count);
    public abstract void addDrawCommand(int id);
    public abstract void addDrawCommand(int id, int baseVertex);
    public abstract void setIndexCount(int id, int count);

    public abstract void setPosition(int id, int x, int y);
    public abstract void setPosition(int id, float x, float y);
    public abstract void setX(int id, int x);
    public abstract void setY(int id, int y);
    public abstract void setRotation(int id, float rotation);
    public abstract void setRotation(int id, float sin, float cos);
    public abstract void setSize(int id, int width, int height);
    public abstract void setWidth(int id, int width);
    public abstract void setHeight(int id, int height);
    public abstract void setColor(int id, Vector4f color);
    public abstract void setColor(int id, float r, float g, float b, float a);
    public abstract void setTexture(int id, long textureHandle);

    public abstract void setLastPosition(int id, float x, float y);
    public abstract void setLastPosition(int id, int x, int y);
    public abstract void setLastRotation(int id, float sin, float cos);
    public abstract void setLastSize(int id, int width, int height);
    public abstract void setLastColor(int id, Vector4f color);
    public abstract void setLastColor(int id, float r, float g, float b, float a);

    public abstract int getRenderObjectsCount();

    public abstract void removeObject(int id);
    public abstract void removeObject(int id, BufferType bufferType);
}