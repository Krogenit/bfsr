package net.bfsr.engine.renderer.texture;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

public abstract class AbstractTextureLoader {
    public static AbstractTexture dummyTexture;

    public abstract void init();

    public abstract AbstractTexture createTexture(int width, int height);
    public abstract AbstractTexture newTexture(int width, int height);
    public abstract AbstractTexture getTexture(TextureRegister texture, int wrap, int filter);
    public abstract AbstractTexture getTexture(TextureRegister texture);
    public abstract AbstractTexture getTexture(Path path);

    public abstract void uploadTexture(AbstractTexture texture, int internalFormat, int format, int wrap, int filter, ByteBuffer byteBuffer);
    public abstract void uploadTexture(AbstractTexture texture, int internalFormat, int format, int wrap, int filter, IntBuffer buffer);
    public abstract void uploadEmpty(AbstractTexture texture, int internalFormat, int format);
    public abstract void subImage2D(int id, int x, int y, int width, int height, int format, ByteBuffer byteBuffer);
    public abstract void subImage2D(int id, int x, int y, int width, int height, int format, IntBuffer buffer);
}