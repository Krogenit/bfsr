package net.bfsr.engine.sound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.openal.AL10;

@Getter
@RequiredArgsConstructor
class SoundBuffer extends AbstractSoundBuffer {
    protected final int buffer;

    @Override
    public void clear() {
        AL10.alDeleteBuffers(buffer);
    }
}