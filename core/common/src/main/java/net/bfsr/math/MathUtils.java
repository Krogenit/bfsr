package net.bfsr.math;

import net.bfsr.entity.RigidBody;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Transform;
import org.joml.Math;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public final class MathUtils {
    public static final float PI = (float) Math.PI;
    public static final float MINUS_PI = -PI;
    public static final float TWO_PI = (float) (Math.PI * 2.0);
    public static final float THREE_PI = (float) (Math.PI * 3.0);
    public static final float HALF_PI = (float) (Math.PI / 2.0);
    public static final Vector2f ROTATE_TO_VECTOR = new Vector2f();
    public static final Vector2f ANGLE_TO_VELOCITY = new Vector2f();
    private static final Transform IDENTITY_TRANSFORM = new Transform();
    private static final AABB CACHED_AABB_1 = new AABB(0, 0, 0, 0);
    private static final List<Direction> DIRECTIONS = new ArrayList<>();

    public static float lerpAngle(float start, float end) {
        return (((end - start) % TWO_PI) + THREE_PI) % TWO_PI - PI;
    }

    public static void computeAABB(Body body, AABB aabb) {
        List<BodyFixture> fixtures = body.getFixtures();
        int size = fixtures.size();
        fixtures.get(0).getShape().computeAABB(IDENTITY_TRANSFORM, aabb);
        for (int i = 1; i < size; i++) {
            fixtures.get(i).getShape().computeAABB(IDENTITY_TRANSFORM, CACHED_AABB_1);
            aabb.union(CACHED_AABB_1);
        }
    }

    public static float getRotationDifference(RigidBody gameObject, Vector2f vector) {
        Vector2f position = gameObject.getPosition();
        ROTATE_TO_VECTOR.set(vector.x - position.x, vector.y - position.y);
        Vector2f angleToVelocity = RotationHelper.angleToVelocity(gameObject.getSin(), gameObject.getCos(), 1.0f);
        return angleToVelocity.angle(ROTATE_TO_VECTOR);
    }

    public static void rotateToVector(RigidBody gameObject, Vector2f vector, float rotationSpeed) {
        Body body = gameObject.getBody();

        float diffRad = getRotationDifference(gameObject, vector);
        float diffAbs = Math.abs(diffRad);
        float addRot = Math.min(diffAbs, rotationSpeed);

        float sin = gameObject.getSin();
        float cos = gameObject.getCos();
        if (addRot >= diffAbs) {
            float sin1 = Math.sin(diffRad);
            float cos1 = Math.cos(diffRad);
            gameObject.setRotation(sin * cos1 + cos * sin1, cos * cos1 - sin * sin1);
        } else {
            addRot = diffRad > 0 ? addRot : -addRot;
            float sin1 = Math.sin(addRot);
            float cos1 = Math.cos(addRot);
            gameObject.setRotation(sin * cos1 + cos * sin1, cos * cos1 - sin * sin1);
            body.setAngularVelocity(body.getAngularVelocity() * 0.99f);
        }
    }

    public static List<Direction> calculateDirectionsToOtherObject(RigidBody gameObject, float x, float y) {
        Vector2f pos = gameObject.getPosition();
        ROTATE_TO_VECTOR.x = x - pos.x;
        ROTATE_TO_VECTOR.y = y - pos.y;
        Vector2f angleToVelocity = RotationHelper.angleToVelocity(gameObject.getSin(), gameObject.getCos(), 1.0f);
        double diff = Math.toDegrees(angleToVelocity.angle(ROTATE_TO_VECTOR));
        double diffAbs = Math.abs(diff);

        DIRECTIONS.clear();

        if (diffAbs > 112.5f) {
            DIRECTIONS.add(Direction.BACKWARD);
        } else if (diffAbs <= 67.5f) {
            DIRECTIONS.add(Direction.FORWARD);
        }

        if (diff < -22.5f && diff >= -157.5f) {
            DIRECTIONS.add(Direction.LEFT);
        } else if (diff > 22.5f && diff <= 157.5f) {
            DIRECTIONS.add(Direction.RIGHT);
        }

        return DIRECTIONS;
    }

    public static Direction calculateDirectionToOtherObject(RigidBody gameObject, float x, float y) {
        Vector2f pos = gameObject.getPosition();
        ROTATE_TO_VECTOR.x = x - pos.x;
        ROTATE_TO_VECTOR.y = y - pos.y;
        RotationHelper.angleToVelocity(gameObject.getSin(), gameObject.getCos(), 1.0f, ANGLE_TO_VELOCITY);
        double diff = Math.toDegrees(ANGLE_TO_VELOCITY.angle(ROTATE_TO_VECTOR));
        double diffAbs = Math.abs(diff);
        if (diffAbs > 135) {
            return Direction.BACKWARD;
        } else if (diff < -45 && diff >= -135) {
            return Direction.LEFT;
        } else if (diffAbs <= 45) {
            return Direction.FORWARD;
        } else if (diff > 45 && diff <= 135) {
            return Direction.RIGHT;
        }

        return null;
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