package net.bfsr.engine.renderer.font.string;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public abstract class AbstractGLString {
    public abstract void init(int glyphCount);

    public abstract void checkBuffers(int length);
    public abstract void flipBuffers();
    public abstract void clearBuffers();

    public abstract void setWidth(int i);
    public abstract void setHeight(int height);

    public abstract FloatBuffer getVertexBuffer();
    public abstract ByteBuffer getMaterialBuffer();
    public abstract int getHeight();
}