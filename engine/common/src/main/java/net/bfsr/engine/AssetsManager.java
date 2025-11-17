package net.bfsr.engine;

import net.bfsr.engine.renderer.texture.AbstractTexture;
import net.bfsr.engine.renderer.texture.AbstractTextureLoader;
import net.bfsr.engine.renderer.texture.TextureData;
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

    public AbstractTexture getTexture(TextureData textureData) {
        return textureLoader.getTexture(textureData);
    }

    public AbstractSoundBuffer getSound(Path path) {
        return soundLoader.getBuffer(path);
    }

    public void clear() {
        textureLoader.clear();
        soundLoader.clear();
    }
}