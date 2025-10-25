package net.bfsr.engine;

import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.renderer.texture.TextureRegister;
import net.bfsr.engine.sound.AbstractSoundBuffer;
import net.bfsr.engine.sound.AbstractSoundLoader;
import net.bfsr.engine.sound.SoundRegistry;

import java.nio.file.Path;

public class AssetsManager {
    private final AbstractTextureLoader textureLoader;
    private final AbstractSoundLoader soundLoader;

    public AssetsManager(AbstractTextureLoader textureLoader, AbstractSoundLoader soundLoader) {
        this.textureLoader = textureLoader;
        this.soundLoader = soundLoader;
    }

    public AbstractTexture createTexture(int width, int height) {
        return textureLoader.createTexture(width, height);
    }

    public AbstractTexture createDummyTexture() {
        return textureLoader.createDummyTexture();
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

    public AbstractTexture getTexture(Path path, int wrap, int filter) {
        return textureLoader.getTexture(path, wrap, filter);
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

    public void clear() {
        textureLoader.clear();
        soundLoader.clear();
    }
}