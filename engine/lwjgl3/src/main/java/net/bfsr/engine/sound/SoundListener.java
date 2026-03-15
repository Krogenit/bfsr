package net.bfsr.engine.sound;

import lombok.Getter;
import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_ORIENTATION;
import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.AL_VELOCITY;
import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.AL10.alListener3f;
import static org.lwjgl.openal.AL10.alListenerf;
import static org.lwjgl.openal.AL10.alListenerfv;
import static org.lwjgl.openal.AL11.AL_EXPONENT_DISTANCE_CLAMPED;

@Getter
public class SoundListener extends AbstractSoundListener {
    private final Vector3f position;

    SoundListener(Vector3f position) {
        this.position = position;
        alListener3f(AL_POSITION, position.x, position.y, position.z);
        alListener3f(AL_VELOCITY, 0, 0, 0);
    }

    public void setExponentClampedDistanceModel() {
        alDistanceModel(AL_EXPONENT_DISTANCE_CLAMPED);
    }

    public void setVelocity(Vector3f speed) {
        alListener3f(AL_VELOCITY, speed.x, speed.y, speed.z);
    }

    @Override
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        alListener3f(AL_POSITION, x, y, z);
    }

    @Override
    public void setGain(float value) {
        alListenerf(AL_GAIN, value);
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