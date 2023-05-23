package net.bfsr.engine.sound;

import org.lwjgl.openal.AL10;

public class SoundBuffer extends AbstractSoundBuffer {
    public SoundBuffer(int buffer) {
        super(buffer);
    }

    @Override
    public void cleanup() {
        AL10.alDeleteBuffers(buffer);
    }
}