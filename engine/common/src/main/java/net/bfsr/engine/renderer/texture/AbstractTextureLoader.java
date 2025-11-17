package net.bfsr.engine.renderer.texture;

import lombok.Getter;

@Getter
public abstract class AbstractTextureLoader {
    public abstract AbstractTexture createTexture(int width, int height);
    public abstract AbstractTexture createDummyTexture();

    public abstract AbstractTexture newTexture(int width, int height);

    public abstract AbstractTexture getTexture(TextureData textureData);

    public abstract void clear();
}