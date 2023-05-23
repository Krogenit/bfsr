package net.bfsr.engine.sound;

import org.joml.Vector2f;
import org.joml.Vector3f;

public abstract class AbstractSoundListener {
    public abstract void setGain(float value);
    public abstract void setExponentClampedDistanceModel();
    public abstract void setOrientation(Vector3f at, Vector3f up);
    public abstract void setPosition(Vector2f position);
}