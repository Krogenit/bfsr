package net.bfsr.engine.sound;

import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_EXPONENT_DISTANCE;
import static org.lwjgl.openal.AL11.AL_EXPONENT_DISTANCE_CLAMPED;

public class SoundListener extends AbstractSoundListener {
    public SoundListener(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
        alListener3f(AL_VELOCITY, 0, 0, 0);
    }

    public void setExponentDistanceModel() {
        alDistanceModel(AL_EXPONENT_DISTANCE);
    }

    @Override
    public void setExponentClampedDistanceModel() {
        alDistanceModel(AL_EXPONENT_DISTANCE_CLAMPED);
    }

    public void setSpeed(Vector3f speed) {
        alListener3f(AL_VELOCITY, speed.x, speed.y, speed.z);
    }

    @Override
    public void setPosition(Vector2f position) {
        alListener3f(AL_POSITION, position.x, position.y, -50);
    }

    public void setPosition(Vector3f position) {
        alListener3f(AL_POSITION, position.x, position.y, position.z);
    }

    @Override
    public void setGain(float volume) {
        alListenerf(AL_GAIN, volume);
    }

    @Override
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