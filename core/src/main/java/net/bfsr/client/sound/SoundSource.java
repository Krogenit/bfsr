package net.bfsr.client.sound;

import net.bfsr.client.loader.SoundLoader;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;

public class SoundSource {
    private final int sourceId;

    public SoundSource(SoundRegistry soundName, boolean loop, boolean relative) {
        this.sourceId = AL10.alGenSources();
        if (loop) {
            AL10.alSourcei(sourceId, AL10.AL_LOOPING, AL10.AL_TRUE);
        }
        if (relative) {
            AL10.alSourcei(sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
        }
        setBuffer(SoundLoader.getBufferId(soundName));
        setGain(soundName.getVolume());
        setRolloffFactor(3f);
        setReferenceDistance(90f);
    }

    public void setBuffer(int bufferId) {
        stop();
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
    }

    public void setPosition(Vector2d position) {
        AL10.alSource3f(sourceId, AL10.AL_POSITION, (float) position.x, (float) position.y, 0);
    }

    public void setPosition(Vector2f position) {
        AL10.alSource3f(sourceId, AL10.AL_POSITION, position.x, position.y, 0);
    }

    public void setPosition(Vector3f position) {
        AL10.alSource3f(sourceId, AL10.AL_POSITION, position.x, position.y, position.z);
    }

    public void setSpeed(Vector3f speed) {
        AL10.alSource3f(sourceId, AL10.AL_VELOCITY, speed.x, speed.y, speed.z);
    }

    public void setGain(float gain) {
        AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
    }

    public void setProperty(int param, float value) {
        AL10.alSourcef(sourceId, param, value);
    }

    /*
     * How quicly sound will drop by distance
     */
    public void setRolloffFactor(float value) {
        AL10.alSourcef(sourceId, AL10.AL_ROLLOFF_FACTOR, value);
    }

    public void setReferenceDistance(float value) {
        AL10.alSourcef(sourceId, AL10.AL_REFERENCE_DISTANCE, value);
    }

    public void setMaxDistance(float value) {
        AL10.alSourcef(sourceId, AL10.AL_MAX_DISTANCE, value);
    }

    public void play() {
        AL10.alSourcePlay(sourceId);
    }

    public boolean isPlaying() {
        return AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    public void pause() {
        AL10.alSourcePause(sourceId);
    }

    public void stop() {
        AL10.alSourceStop(sourceId);
    }

    public void clear() {
        stop();
        AL10.alDeleteSources(sourceId);
    }
}
