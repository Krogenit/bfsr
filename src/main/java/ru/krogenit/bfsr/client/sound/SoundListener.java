package ru.krogenit.bfsr.client.sound;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC10.*;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class SoundListener {
	public SoundListener() {
        this(new Vector3f(0, 0, 0));
    }

    public SoundListener(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
        alListener3f(AL_VELOCITY, 0, 0, 0);
    }
    
    public void setExponentDistanceModel() {
    	alDistanceModel(AL_EXPONENT_DISTANCE);
    }
    
    public void setExponentClampedDistanceModel() {
    	alDistanceModel(AL_EXPONENT_DISTANCE_CLAMPED);
    }

    public void setSpeed(Vector3f speed) {
        alListener3f(AL_VELOCITY, speed.x, speed.y, speed.z);
    }
    
    public void setPosition(Vector2f position) {
        alListener3f(AL_POSITION, position.x, position.y, -500);
    }

    public void setPosition(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
    }
    
    public void setGain(float volume) {
        alListenerf(AL_GAIN, volume);
    }

    public void setOrientation(Vector3f at, Vector3f up) {
        float[] data = new float[6];
        data[0] = at.x;
        data[1] = at.y;
        data[2] = at.z;
        data[3] = up.x;
        data[4] = up.y;
        data[5] = up.z;
        alListenerfv(AL_ORIENTATION, data);
    }
}
