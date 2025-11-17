package net.bfsr.client.assets;

import lombok.Getter;
import net.bfsr.engine.util.PathHelper;

import java.nio.file.Path;

@Getter
public enum SoundRegistry {
    buttonCollide("sound/gui/buttonCollide.ogg", 0.01f),
    buttonClick("sound/gui/buttonClick.ogg", 0.1f);

    private final Path path;
    private final float volume;

    SoundRegistry(String path, float volume) {
        this.path = PathHelper.convertPath(path);
        this.volume = volume;
    }
}