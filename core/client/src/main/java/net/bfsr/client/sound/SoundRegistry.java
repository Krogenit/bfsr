package net.bfsr.client.sound;

import lombok.Getter;
import net.bfsr.util.PathHelper;

import java.nio.file.Path;

@Getter
public enum SoundRegistry {
    buttonCollide("sound/gui/buttonCollide.ogg", 0.275f),
    buttonClick("sound/gui/buttonClick.ogg", 2.0f);

    private final Path path;
    private final float volume;

    SoundRegistry(String path, float volume) {
        this.path = PathHelper.convertPath(path);
        this.volume = volume;
    }
}