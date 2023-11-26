package net.bfsr.math;

import net.bfsr.entity.RigidBody;
import org.dyn4j.dynamics.Body;
import org.joml.Math;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public final class RigidBodyUtils {
    public static final Vector2f ROTATE_TO_VECTOR = new Vector2f();
    public static final Vector2f ANGLE_TO_VELOCITY = new Vector2f();
    private static final List<Direction> DIRECTIONS = new ArrayList<>();

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
        }

        body.setAngularVelocity(body.getAngularVelocity() * 0.99f);
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
}