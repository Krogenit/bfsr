package net.bfsr.client.sound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.sound.AbstractSoundBuffer;

@RequiredArgsConstructor
@Getter
public class SoundEffect {
    private final AbstractSoundBuffer soundBuffer;
    private final float volume;
}