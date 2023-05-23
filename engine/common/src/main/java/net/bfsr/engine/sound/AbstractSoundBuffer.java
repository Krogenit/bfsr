package net.bfsr.engine.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class AbstractSoundBuffer {
    @Getter
    protected final int buffer;

    public abstract void cleanup();
}