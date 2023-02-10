package net.bfsr.math;

public final class MathUtils {
    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = (float) (Math.PI * 2.0);
    public static final float THREE_PI = (float) (Math.PI * 3.0);
    public static final float HALF_PI = (float) (Math.PI / 2.0);

    public static float lerpAngle(float start, float end) {
        return (((end - start) % TWO_PI) + THREE_PI) % TWO_PI - PI;
    }
}
