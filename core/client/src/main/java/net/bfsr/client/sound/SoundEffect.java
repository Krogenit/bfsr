package net.bfsr.client.sound;

import lombok.RequiredArgsConstructor;
import net.bfsr.client.core.Core;

@RequiredArgsConstructor
public class SoundEffect {
    private final SoundManager soundManager = Core.get().getSoundManager();
    private final SoundBuffer soundBuffer;
    private final float volume;

    public void play(float x, float y) {
        soundManager.play(new SoundSource(soundBuffer, volume, x, y));
    }
}