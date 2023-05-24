package net.bfsr.engine.math;

import org.joml.Math;

public final class MathUtils {
    public static final float PI = (float) Math.PI;
    public static final float MINUS_PI = -PI;
    public static final float TWO_PI = (float) (Math.PI * 2.0);
    public static final float THREE_PI = (float) (Math.PI * 3.0);
    public static final float HALF_PI = (float) (Math.PI / 2.0);

    public static float lerpAngle(float start, float end) {
        return (((end - start) % TWO_PI) + THREE_PI) % TWO_PI - PI;
    }

    public static float fastAtan2(float y, float x) {
        float ax = x >= 0.0f ? x : -x, ay = y >= 0.0f ? y : -y;
        float a = Math.min(ax, ay) / Math.max(ax, ay);
        float s = a * a;
        float r = ((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a;
        if (ay > ax)
            r = 1.57079637f - r;
        if (x < 0.0)
            r = 3.14159274f - r;
        return y >= 0 ? r : -r;
    }
}