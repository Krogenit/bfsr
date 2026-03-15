package net.bfsr.engine.sound;

import org.joml.Vector3f;

public abstract class AbstractSoundListener {
    public abstract void setGain(float value);
    public abstract void setOrientation(Vector3f at, Vector3f up);
    public abstract void setPosition(float x, float y, float z);
    public abstract Vector3f getPosition();
}