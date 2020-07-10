package ru.krogenit.bfsr.client.sound;

import static org.lwjgl.openal.AL10.*;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;

import ru.krogenit.bfsr.client.loader.SoundLoader;

public class SoundSource {
	private final int sourceId;

    public SoundSource(SoundRegistry soundName, boolean loop, boolean relative) {
        this.sourceId = alGenSources();
        if (loop) {
            alSourcei(sourceId, AL_LOOPING, AL_TRUE);
        }
        if (relative) {
            alSourcei(sourceId, AL_SOURCE_RELATIVE, AL_TRUE);
        }
        setBuffer(SoundLoader.getBufferId(soundName));
        setGain(soundName.getVolume());
        setRolloffFactor(3f);
        setReferenceDistance(900f);
    }

    public void setBuffer(int bufferId) {
        stop();
        alSourcei(sourceId, AL_BUFFER, bufferId);
    }
    
    public void setPosition(Vector2d position) {
        alSource3f(sourceId, AL_POSITION, (float) position.x, (float) position.y, 0);
    }
    
    public void setPosition(Vector2f position) {
        alSource3f(sourceId, AL_POSITION, position.x, position.y, 0);
    }

    public void setPosition(Vector3f position) {
        alSource3f(sourceId, AL_POSITION, position.x, position.y, position.z);
    }

    public void setSpeed(Vector3f speed) {
        alSource3f(sourceId, AL_VELOCITY, speed.x, speed.y, speed.z);
    }

    public void setGain(float gain) {
        alSourcef(sourceId, AL_GAIN, gain);
    }

    public void setProperty(int param, float value) {
        alSourcef(sourceId, param, value);
    }
    
    /*
     * How quicly sound will drop by distance
     */
    public void setRolloffFactor(float value) {
    	alSourcef(sourceId, AL_ROLLOFF_FACTOR, value);
    }
    
    public void setReferenceDistance(float value) {
    	alSourcef(sourceId, AL_REFERENCE_DISTANCE, value);
    }
    
    public void setMaxDistance(float value) {
    	alSourcef(sourceId, AL_MAX_DISTANCE, value);
    }

    public void play() {
        alSourcePlay(sourceId);
    }

    public boolean isPlaying() {
        return alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void pause() {
        alSourcePause(sourceId);
    }

    public void stop() {
        alSourceStop(sourceId);
    }
    
    public void clear() {
		stop();
        alDeleteSources(sourceId);
    }
}
