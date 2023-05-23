package net.bfsr.client.sound;

import lombok.RequiredArgsConstructor;
import net.bfsr.engine.Engine;
import net.bfsr.engine.sound.AbstractSoundBuffer;
import net.bfsr.engine.sound.AbstractSoundManager;
import net.bfsr.engine.sound.SoundSource;

@RequiredArgsConstructor
public class SoundEffect {
    private final AbstractSoundManager soundManager = Engine.soundManager;
    private final AbstractSoundBuffer soundBuffer;
    private final float volume;

    public void play(float x, float y) {
        soundManager.play(new SoundSource(soundBuffer, volume, x, y));
    }
}