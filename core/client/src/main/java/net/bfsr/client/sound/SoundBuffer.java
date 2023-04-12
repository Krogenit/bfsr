package net.bfsr.client.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.openal.AL10;

@AllArgsConstructor
public class SoundBuffer {
    @Getter
    private final int bufferId;

    public void cleanup() {
        AL10.alDeleteBuffers(this.bufferId);
    }
}