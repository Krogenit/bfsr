package net.bfsr.engine.renderer.texture;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractTexture {
    @Getter
    protected final int width;
    @Getter
    protected final int height;
    @Getter
    protected int id;
    @Getter
    @Setter
    protected long textureHandle;

    protected AbstractTexture(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public abstract AbstractTexture create();
    public abstract void bind();
    public abstract void delete();
}