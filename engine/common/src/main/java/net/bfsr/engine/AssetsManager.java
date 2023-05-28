package net.bfsr.engine;

import lombok.AllArgsConstructor;
import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.sound.AbstractSoundBuffer;
import net.bfsr.engine.sound.AbstractSoundLoader;
import net.bfsr.engine.sound.SoundRegistry;

import java.nio.file.Path;

@AllArgsConstructor
public class AssetsManager {
    private final AbstractTextureLoader textureLoader;
    private final AbstractSoundLoader soundLoader;

    public void init() {
        textureLoader.init();
    }

    public AbstractTexture createTexture(int width, int height) {
        return textureLoader.createTexture(width, height);
    }

    public AbstractTexture newTexture(int width, int height) {
        return textureLoader.newTexture(width, height);
    }

    public AbstractTexture getTexture(TextureRegister texture, int wrap, int filter) {
        return textureLoader.getTexture(texture, wrap, filter);
    }

    public AbstractTexture getTexture(TextureRegister texture) {
        return textureLoader.getTexture(texture);
    }

    public AbstractTexture getTexture(Path path) {
        return textureLoader.getTexture(path);
    }

    public AbstractSoundBuffer getSound(SoundRegistry sound) {
        return soundLoader.getBuffer(sound);
    }

    public AbstractSoundBuffer getSound(Path path) {
        return soundLoader.getBuffer(path);
    }
}