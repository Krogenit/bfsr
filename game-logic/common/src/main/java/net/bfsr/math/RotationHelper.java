package net.bfsr.math;

import net.bfsr.engine.math.LUT;
import org.joml.Vector2f;

public final class RotationHelper {
    public static float rotateX(float sin, float cos, float x, float y) {
        return cos * x - sin * y;
    }

    public static float rotateY(float sin, float cos, float x, float y) {
        return sin * x + cos * y;
    }

    public static void rotate(float sin, float cos, float x, float y, Vector2f dest) {
        dest.x = cos * x - sin * y;
        dest.y = sin * x + cos * y;
    }

    public static void rotate(float rotation, float x, float y, Vector2f dest) {
        float sin = LUT.sin(rotation);
        float cos = LUT.cos(rotation);
        dest.x = cos * x - sin * y;
        dest.y = sin * x + cos * y;
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