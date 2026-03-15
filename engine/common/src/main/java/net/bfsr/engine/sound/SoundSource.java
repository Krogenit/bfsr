package net.bfsr.engine.sound;

import lombok.Getter;
import net.bfsr.engine.Engine;

@Getter
public class SoundSource {
    private final AbstractSoundManager soundManager = Engine.getSoundManager();
    private final int source;

    public SoundSource(int soundBuffer, float volume, float x, float y, float z, float pitch, boolean loop, boolean relative) {
        source = soundManager.createSoundSource(soundBuffer, loop, relative);
        setGain(volume);
        setPosition(x, y, z);
        setRolloffFactor(3.0f);
        setReferenceDistance(8.0f);
        setPitch(pitch);
    }

    public SoundSource(int soundBuffer, float volume, float x, float y, float z, float pitch) {
        this(soundBuffer, volume, x, y, z, pitch, false, false);
    }

    public void setPosition(float x, float y, float z) {
        soundManager.setPosition(source, x, y, z);
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

    public void setMaxDistance(float value) {
        soundManager.setMaxDistance(source, value);
    }

    private void setPitch(float value) {
        soundManager.setPitch(source, value);
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