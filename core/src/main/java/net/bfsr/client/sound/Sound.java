package net.bfsr.client.sound;

import org.lwjgl.openal.AL10;

public class Sound {
    private final int bufferPointer;
    private final int sourcePointer;

    public Sound(int buffer, int source) {
        this.bufferPointer = buffer;
        this.sourcePointer = source;
    }

    public int getBufferPointer() {
        return bufferPointer;
    }

    public int getSourcePointer() {
        return sourcePointer;
    }

    public void play() {
        AL10.alSourcePlay(sourcePointer);
    }
}
