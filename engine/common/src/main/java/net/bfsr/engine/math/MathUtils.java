package net.bfsr.engine.math;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Transform;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.joml.Math;

import java.util.List;

public final class MathUtils {
    public static final float PI = (float) Math.PI;
    public static final float MINUS_PI = -PI;
    public static final float TWO_PI = (float) (Math.PI * 2.0);
    public static final float THREE_PI = (float) (Math.PI * 3.0);
    public static final float HALF_PI = (float) (Math.PI / 2.0);
    private static final Transform IDENTITY_TRANSFORM = new Transform();
    public static final float DEGREES_TO_RADIANS = 0.017453292519943295f;
    public static final float RADIANS_TO_DEGREES = 57.29577951308232f;

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

    public static void computeAABB(AABB aabb, Body body, Transform transform, AABB cache) {
        List<Fixture> fixtures = body.fixtures;
        if (fixtures.isEmpty()) {
            return;
        }

        fixtures.get(0).getShape().computeAABB(aabb, transform, 0);
        for (int i = 1; i < fixtures.size(); i++) {
            fixtures.get(i).getShape().computeAABB(cache, transform, 0);
            aabb.combine(cache);
        }
    }

    public static void computeAABB(AABB aabb, Body body, AABB cache) {
        computeAABB(aabb, body, IDENTITY_TRANSFORM, cache);
    }
}