package net.bfsr.engine.sound;

public abstract class AbstractSoundManager {
    public abstract void setGain(float value);
    public abstract void updateListenerPosition(float x, float y, float z);
    public abstract void updateGain(float value);

    public abstract SoundSource play(int soundBuffer, float volume);
    public abstract SoundSource play(int soundBuffer, float volume, float x, float y);
    public abstract SoundSource play(int soundBuffer, float volume, float x, float y, float pitch);
    public abstract void play(SoundEffect soundEffect, float x, float y);

    public abstract int createSoundSource(int bufferId, boolean loop, boolean relative);

    public abstract void setPosition(int source, float x, float y, float z);
    public abstract void setGain(int source, float gain);
    public abstract void setRollOffFactor(int source, float value);
    public abstract void setReferenceDistance(int source, float value);
    public abstract void setMaxDistance(int source, float value);
    public abstract void setPitch(int source, float value);

    public abstract boolean isPlaying(int source);
    public abstract void pause(int source);
    public abstract void stop(int source);
    public abstract void delete(int source);

    public abstract void clear();
}