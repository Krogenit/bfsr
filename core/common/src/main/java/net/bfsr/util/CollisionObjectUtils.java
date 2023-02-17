package net.bfsr.util;

import net.bfsr.entity.GameObject;
import net.bfsr.math.Direction;
import net.bfsr.math.MathUtils;
import net.bfsr.math.RotationHelper;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Transform;
import org.joml.Vector2f;

import java.util.List;

public final class CollisionObjectUtils {
    private static final Transform IDENTITY_TRANSFORM = new Transform();
    private static final AABB CACHED_AABB_0 = new AABB(0, 0, 0, 0);
    private static final AABB CACHED_AABB_1 = new AABB(0, 0, 0, 0);

    public static final Vector2f ROTATE_TO_VECTOR = new Vector2f();
    public static final Vector2f ANGLE_TO_VELOCITY = new Vector2f();
    private static final Direction[] DIRECTIONS = new Direction[2];

    public static AABB computeAABB(Body body) {
        List<BodyFixture> fixtures = body.getFixtures();
        int size = fixtures.size();
        fixtures.get(0).getShape().computeAABB(IDENTITY_TRANSFORM, CACHED_AABB_0);
        for (int i = 1; i < size; i++) {
            fixtures.get(i).getShape().computeAABB(IDENTITY_TRANSFORM, CACHED_AABB_1);
            CACHED_AABB_0.union(CACHED_AABB_1);
        }

        return CACHED_AABB_0;
    }

    public static float getRotationDifference(GameObject gameObject, Vector2f vector) {
        Transform transform = gameObject.getBody().getTransform();
        ROTATE_TO_VECTOR.x = (float) (vector.x - transform.getTranslationX());
        ROTATE_TO_VECTOR.y = (float) (vector.y - transform.getTranslationY());

        RotationHelper.angleToVelocity(gameObject.getRotation(), 1.0f, ANGLE_TO_VELOCITY);
        return ANGLE_TO_VELOCITY.angle(ROTATE_TO_VECTOR);
    }

    public static void rotateToVector(Body body, Vector2f vector, float rotationSpeed) {
        Transform transform = body.getTransform();
        float rot = (float) transform.getRotationAngle();
        ROTATE_TO_VECTOR.x = (float) (vector.x - transform.getTranslationX());
        ROTATE_TO_VECTOR.y = (float) (vector.y - transform.getTranslationY());

        RotationHelper.angleToVelocity(rot, 1.0f, ANGLE_TO_VELOCITY);
        float diffRad = ANGLE_TO_VELOCITY.angle(ROTATE_TO_VECTOR);
        double diff = Math.toDegrees(diffRad);
        double diffAbs = Math.abs(diff);
        double addRot = 0;

        if (diff > 0) {
            if (diffAbs <= 5) {
                if (diffAbs <= 1) {
                    body.setAngularVelocity(0);
                } else addRot = rotationSpeed / 4.0 * TimeUtils.UPDATE_DELTA_TIME;
            } else addRot = rotationSpeed * TimeUtils.UPDATE_DELTA_TIME;

            if (addRot >= diffRad) {
                transform.setRotation(Math.atan2(ROTATE_TO_VECTOR.x, -ROTATE_TO_VECTOR.y) - Math.PI / 2.0);
                body.setAngularVelocity(0);
            } else {
                transform.setRotation(rot + addRot);
                body.setAngularVelocity(body.getAngularVelocity() * 0.99f);
            }
        } else {
            if (diffAbs <= 5) {
                if (diffAbs <= 1) {
                    body.setAngularVelocity(0);
                } else addRot = -rotationSpeed / 4.0 * TimeUtils.UPDATE_DELTA_TIME;
            } else addRot = -rotationSpeed * TimeUtils.UPDATE_DELTA_TIME;

            if (addRot <= diffRad) {
                transform.setRotation(Math.atan2(ROTATE_TO_VECTOR.x, -ROTATE_TO_VECTOR.y) - Math.PI / 2.0);
                body.setAngularVelocity(0);
            } else {
                transform.setRotation(rot + addRot);
                body.setAngularVelocity(body.getAngularVelocity() * 0.99f);
            }
        }
    }

    public static Direction[] calculateDirectionsToOtherObject(GameObject gameObject, float x, float y) {
        DIRECTIONS[0] = DIRECTIONS[1] = null;
        Vector2f pos = gameObject.getPosition();
        ROTATE_TO_VECTOR.x = x - pos.x;
        ROTATE_TO_VECTOR.y = y - pos.y;
        RotationHelper.angleToVelocity(gameObject.getRotation(), 1.0f, ANGLE_TO_VELOCITY);
        double diff = Math.toDegrees(ANGLE_TO_VELOCITY.angle(ROTATE_TO_VECTOR));
        double diffAbs = Math.abs(diff);
        if (diffAbs > 112.5f) {
            DIRECTIONS[0] = Direction.BACKWARD;
        } else if (diffAbs <= 67.5f) {
            DIRECTIONS[0] = Direction.FORWARD;
        }


        if (diff < -22.5f && diff >= -157.5f) {
            DIRECTIONS[1] = Direction.LEFT;
        } else if (diff > 22.5f && diff <= 157.5f) {
            DIRECTIONS[1] = Direction.RIGHT;
        }

        return DIRECTIONS;
    }

    public static Direction calculateDirectionToOtherObject(GameObject gameObject, float x, float y) {
        Vector2f pos = gameObject.getPosition();
        ROTATE_TO_VECTOR.x = x - pos.x;
        ROTATE_TO_VECTOR.y = y - pos.y;
        RotationHelper.angleToVelocity(gameObject.getRotation(), 1.0f, ANGLE_TO_VELOCITY);
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

    public static void updatePos(GameObject gameObject, Vector2f newPos) {
        Vector2f pos = gameObject.getPosition();

        float dist = pos.distance(newPos);

        float alpha = dist > 10 ? 1.0f : 0.05f;
        double x = pos.x + alpha * (newPos.x - pos.x);
        double y = pos.y + alpha * (newPos.y - pos.y);

        gameObject.getBody().getTransform().setTranslation(x, y);
    }

    public static void updateRot(Body body, float newRotation) {
        float currentRotation = (float) body.getTransform().getRotationAngle();

        float diff = newRotation - currentRotation;
        if (diff < -MathUtils.PI) diff += MathUtils.TWO_PI;
        if (diff > MathUtils.PI) diff -= MathUtils.TWO_PI;

        float alpha = Math.max(Math.abs(diff) / 10.0f, 0.1f);
        body.getTransform().setRotation(currentRotation + diff * alpha);
    }
}
