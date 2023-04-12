package net.bfsr.client.sound;

public class SoundSourceEffect extends SoundSource {
    public SoundSourceEffect(SoundRegistry soundName, float x, float y) {
        this(SoundLoader.getBuffer(soundName), soundName.getVolume(), x, y);
    }

    public SoundSourceEffect(SoundBuffer soundBuffer, float volume, float x, float y) {
        super(soundBuffer, volume, false, false, x, y);
    }
}