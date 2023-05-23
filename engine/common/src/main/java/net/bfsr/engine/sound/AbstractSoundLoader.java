package net.bfsr.engine.sound;

import java.nio.file.Path;

public abstract class AbstractSoundLoader {
    public abstract AbstractSoundBuffer getBuffer(SoundRegistry sound);
    public abstract AbstractSoundBuffer getBuffer(Path path);
}