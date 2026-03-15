package net.bfsr.engine.sound;

import java.nio.file.Path;

public abstract class AbstractSoundLoader {
    public abstract int getBuffer(Path path);
    public abstract void clear();
}