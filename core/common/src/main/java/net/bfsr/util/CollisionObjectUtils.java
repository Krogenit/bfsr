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
        Vector2f position = gameObject.getPosition();
        ROTATE_TO_VECTOR.x = vector.x - position.x;
        ROTATE_TO_VECTOR.y = vector.y - position.y;

        RotationHelper.angleToVelocity(gameObject.getSin(), gameObject.getCos(), 1.0f, ANGLE_TO_VELOCITY);
        return ANGLE_TO_VELOCITY.angle(ROTATE_TO_VECTOR);
    }

    public static void rotateToVector(GameObject gameObject, Vector2f vector, float rotationSpeed) {
        Body body = gameObject.getBody();

        float diffRad = getRotationDifference(gameObject, vector);
        float diffAbs = Math.abs(diffRad);
        float addRot = Math.min(diffAbs, rotationSpeed * TimeUtils.UPDATE_DELTA_TIME);

        if (addRot >= diffAbs) {
            gameObject.setRotation(gameObject.getRotation() + diffRad);
        } else {
            gameObject.setRotation(gameObject.getRotation() + (diffRad > 0 ? addRot : -addRot));
            body.setAngularVelocity(body.getAngularVelocity() * 0.99f);
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

        float interpolationAmount = 1.0f;
        if (dist >= 20) {
            float x = pos.x + (newPos.x - pos.x) * interpolationAmount;
            float y = pos.y + (newPos.y - pos.y) * interpolationAmount;
            gameObject.setPosition(x, y);
        } else {
            float alpha = Math.max(dist / 20, 0.0f) * interpolationAmount;
            float x = pos.x + (newPos.x - pos.x) * alpha;
            float y = pos.y + (newPos.y - pos.y) * alpha;

            gameObject.setPosition(x, y);
        }
    }

    public static void updateRot(GameObject gameObject, float angle) {
        float currentRotation = gameObject.getRotation();
        float diff = angle - currentRotation;
        if (diff < MathUtils.MINUS_PI) diff += MathUtils.TWO_PI;
        if (diff > MathUtils.PI) diff -= MathUtils.TWO_PI;
        float diffAbs = Math.abs(diff);
        if (diffAbs > 0.06f) {
            float interpolationAmount = 0.5f;
            gameObject.setRotation(currentRotation + diff * interpolationAmount);
        } else {
            float interpolationAmount = 0.25f;
            float alpha = Math.max(diffAbs / 0.06f, 0.0f) * interpolationAmount;
            gameObject.setRotation(currentRotation + diff * alpha);
        }
    }
}