package net.bfsr.client.sound;

import net.bfsr.engine.sound.AbstractSoundBuffer;

public record SoundEffect(AbstractSoundBuffer soundBuffer, float volume) {}