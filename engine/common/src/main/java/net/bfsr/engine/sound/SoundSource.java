package net.bfsr.engine.sound;

import net.bfsr.engine.Engine;

public class SoundSource {
    private final int source;

    public SoundSource(AbstractSoundBuffer soundBuffer, float volume, boolean loop, boolean relative) {
        this.source = Engine.soundManager.createSoundSource(soundBuffer.getBuffer(), loop, relative);
        setGain(volume);
        setRolloffFactor(3.0f);
        setReferenceDistance(90.0f);
    }

    public SoundSource(AbstractSoundBuffer soundBuffer, float volume, boolean loop, boolean relative, float x, float y) {
        this(soundBuffer, volume, loop, relative);
        setPosition(x, y);
    }

    public SoundSource(AbstractSoundBuffer soundBuffer, float volume, float x, float y) {
        this(soundBuffer, volume, false, false, x, y);
    }

    public SoundSource(SoundRegistry soundName, boolean loop, boolean relative) {
        this(Engine.assetsManager.soundLoader.getBuffer(soundName), soundName.getVolume(), loop, relative);
    }

    public SoundSource(SoundRegistry soundName) {
        this(Engine.assetsManager.soundLoader.getBuffer(soundName), soundName.getVolume(), false, true);
    }

    public void setPosition(float x, float y) {
        Engine.soundManager.setPosition(source, x, y);
    }

    public void setGain(float gain) {
        Engine.soundManager.setGain(source, gain);
    }

    public void setRolloffFactor(float value) {
        Engine.soundManager.setRollOffFactor(source, value);
    }

    public void setReferenceDistance(float value) {
        Engine.soundManager.setReferenceDistance(source, value);
    }

    public void play() {
        Engine.soundManager.play(source);
    }

    public boolean isPlaying() {
        return Engine.soundManager.isPlaying(source);
    }

    public void pause() {
        Engine.soundManager.pause(source);
    }

    public void stop() {
        Engine.soundManager.stop(source);
    }

    public void clear() {
        stop();
        Engine.soundManager.delete(source);
    }
}