package net.bfsr.engine.math;

import org.jbox2d.collision.AABB;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.joml.Math;

import java.util.List;

public final class MathUtils {
    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = (float) (Math.PI * 2.0);
    public static final float THREE_PI = (float) (Math.PI * 3.0);
    public static final float HALF_PI = (float) (Math.PI / 2.0);
    public static final float DEGREES_TO_RADIANS = 0.017453292519943295f;

    public static float lerpAngle(float start, float end) {
        return (((end - start) % TWO_PI) + THREE_PI) % TWO_PI - PI;
    }

    public static void computeAABB(AABB aabb, Body body, float x, float y, float sin, float cos, AABB cache) {
        List<Fixture> fixtures = body.fixtures;
        if (fixtures.isEmpty()) {
            return;
        }

        fixtures.get(0).getShape().computeAABB(aabb, x, y, sin, cos, 0);
        for (int i = 1; i < fixtures.size(); i++) {
            fixtures.get(i).getShape().computeAABB(cache, x, y, sin, cos, 0);
            aabb.combine(cache);
        }
    }

    public static void computeAABB(AABB aabb, Body body, AABB cache) {
        computeAABB(aabb, body, 0, 0, 0, 1, cache);
    }
}