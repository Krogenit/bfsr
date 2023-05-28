package net.bfsr.engine.renderer.texture;

import java.nio.file.Path;

public abstract class AbstractTextureLoader {
    public static AbstractTexture dummyTexture;

    public abstract void init();

    public abstract AbstractTexture createTexture(int width, int height);
    public abstract AbstractTexture newTexture(int width, int height);
    public abstract AbstractTexture getTexture(TextureRegister texture, int wrap, int filter);
    public abstract AbstractTexture getTexture(TextureRegister texture);
    public abstract AbstractTexture getTexture(Path path);
}