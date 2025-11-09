package net.bfsr.engine.renderer.texture;

import lombok.Getter;
import net.bfsr.engine.renderer.constant.TextureFilter;
import net.bfsr.engine.renderer.constant.TextureWrap;

import java.nio.file.Path;

@Getter
public abstract class AbstractTextureLoader {
    public abstract AbstractTexture createTexture(int width, int height);
    public abstract AbstractTexture createDummyTexture();

    public abstract AbstractTexture newTexture(int width, int height);

    public abstract AbstractTexture getTexture(TextureRegister texture, TextureWrap wrap, TextureFilter filter);
    public abstract AbstractTexture getTexture(TextureRegister texture);
    public abstract AbstractTexture getTexture(Path path, TextureWrap wrap, TextureFilter filter);
    public abstract AbstractTexture getTexture(Path path);

    public abstract void clear();
}