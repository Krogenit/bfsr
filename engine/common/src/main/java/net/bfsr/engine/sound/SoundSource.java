package net.bfsr.engine.sound;

import lombok.Getter;
import net.bfsr.engine.Engine;

@Getter
public class SoundSource {
    private final AbstractSoundManager soundManager = Engine.getSoundManager();
    private final int source;

    public SoundSource(AbstractSoundBuffer soundBuffer, float volume, boolean loop, boolean relative) {
        source = soundManager.createSoundSource(soundBuffer.getBuffer(), loop, relative);
        setGain(volume);
        setRolloffFactor(3.0f);
        setReferenceDistance(90.0f);
    }

    public SoundSource(AbstractSoundBuffer soundBuffer, float volume, boolean loop,
                       boolean relative, float x, float y) {
        this(soundBuffer, volume, loop, relative);
        setPosition(x, y);
    }

    public SoundSource(AbstractSoundBuffer soundBuffer, float volume, float x, float y) {
        this(soundBuffer, volume, false, false, x, y);
    }

    public SoundSource(SoundRegistry soundName) {
        this(Engine.getAssetsManager().getSound(soundName), soundName.getVolume(), false, true);
    }

    public void setPosition(float x, float y) {
        soundManager.setPosition(source, x, y);
    }

    public void setGain(float gain) {
        soundManager.setGain(source, gain);
    }

    public void setRolloffFactor(float value) {
        soundManager.setRollOffFactor(source, value);
    }

    public void setReferenceDistance(float value) {
        soundManager.setReferenceDistance(source, value);
    }

    public boolean isPlaying() {
        return soundManager.isPlaying(source);
    }

    public void pause() {
        soundManager.pause(source);
    }

    public void stop() {
        soundManager.stop(source);
    }

    public void clear() {
        stop();
        soundManager.delete(source);
    }
}