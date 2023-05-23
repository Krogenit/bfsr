package net.bfsr.engine;

import lombok.AllArgsConstructor;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.sound.AbstractSoundLoader;

@AllArgsConstructor
public class AssetsManager {
    public final AbstractTextureLoader textureLoader;
    public final AbstractSoundLoader soundLoader;

    public void init() {
        textureLoader.init();
    }
}