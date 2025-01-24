package net.bfsr.engine.sound;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractSoundBuffer {
    public abstract int getBuffer();
    public abstract void clear();
}