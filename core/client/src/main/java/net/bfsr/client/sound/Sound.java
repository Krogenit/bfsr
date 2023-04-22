package net.bfsr.client.sound;

import lombok.AllArgsConstructor;
import net.bfsr.client.core.Core;

@AllArgsConstructor
public class Sound {
    private final SoundBuffer soundBuffer;
    private final float volume;

    public void play(float x, float y) {
        SoundSourceEffect source = new SoundSourceEffect(soundBuffer, volume, x, y);
        Core.get().getSoundManager().play(source);
    }
}