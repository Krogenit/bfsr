package net.bfsr.engine.sound;

import org.joml.Vector2f;

public abstract class AbstractSoundManager {
    public abstract void setAttenuationModel(int model);
    public abstract void setListener(AbstractSoundListener soundListener);
    public abstract void setGain(float value);
    public abstract void updateListenerPosition(Vector2f position);
    public abstract void updateGain(float value);

    public abstract SoundSource play(AbstractSoundBuffer soundBuffer, float volume, float x, float y);
    public abstract SoundSource play(SoundRegistry sound);

    public abstract int createSoundSource(int bufferId, boolean loop, boolean relative);

    public abstract void setPosition(int source, float x, float y);
    public abstract void setGain(int source, float gain);
    public abstract void setRollOffFactor(int source, float value);
    public abstract void setReferenceDistance(int source, float value);
    public abstract boolean isPlaying(int source);
    public abstract void pause(int source);
    public abstract void stop(int source);
    public abstract void delete(int source);

    public abstract void clear();
}