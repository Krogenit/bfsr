package net.bfsr.engine.renderer.font.string;

import net.bfsr.engine.renderer.buffer.BufferType;
import net.bfsr.engine.renderer.font.StringCache;

public abstract class AbstractStringRenderer {
    public abstract void init();

    public abstract int render(String string, StringCache stringCache, int fontSize, int x, int y, float r, float g, float b, float a, int maxWidth, int indent,
                               BufferType bufferType);

    public abstract AbstractGLString createGLString();
    public abstract void addString(AbstractGLString glString, BufferType bufferType);
    public abstract void addString(AbstractGLString glString, float x, float y, BufferType bufferType);
    public abstract void addString(AbstractGLString glString, float x, float y, float scaleX, float scaleY, BufferType bufferType);
    public abstract void addString(AbstractGLString glString, float lastX, float lastY, float x, float y, float scaleX, float scaleY, BufferType bufferType);
    public abstract void addStringInterpolated(AbstractGLString glString, float lastX, float lastY, float x, float y, BufferType bufferType);
    public abstract void addStringWithShadow(AbstractGLString glString, float lastX, float lastY, float x, float y, float scaleX, float scaleY,
                                             float shadowOffsetX, float shadowOffsetY, BufferType bufferType);
}