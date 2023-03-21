package net.bfsr.math;

import org.joml.Vector2f;

public final class RotationHelper {
    public static Vector2f rotate(float sin, float cos, float x, float y) {
        return new Vector2f(cos * x - sin * y, sin * x + cos * y);
    }

    public static Vector2f rotate(float rotation, float x, float y) {
        float sin = LUT.sin(rotation);
        float cos = LUT.cos(rotation);
        return new Vector2f(cos * x - sin * y, sin * x + cos * y);
    }

    public static void rotate(float rotation, float x, float y, Vector2f dest) {
        float sin = LUT.sin(rotation);
        float cos = LUT.cos(rotation);
        dest.x = cos * x - sin * y;
        dest.y = sin * x + cos * y;
    }

    public static Vector2f angleToVelocity(float angle, float length) {
        return new Vector2f(LUT.cos(angle) * length, LUT.sin(angle) * length);
    }

    public static void angleToVelocity(float angle, float length, Vector2f dest) {
        dest.x = LUT.cos(angle) * length;
        dest.y = LUT.sin(angle) * length;
    }

    public static void angleToVelocity(float sin, float cos, float length, Vector2f dest) {
        dest.x = cos * length;
        dest.y = sin * length;
    }
}