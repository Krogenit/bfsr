package net.bfsr.client.sound;

public class SoundSourceEffect extends SoundSource {
    public SoundSourceEffect(SoundBuffer soundBuffer, float volume, float x, float y) {
        super(soundBuffer, volume, false, false, x, y);
    }
}