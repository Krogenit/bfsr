package net.bfsr.engine.math;

import net.bfsr.engine.world.entity.RigidBody;
import org.jbox2d.dynamics.Body;
import org.joml.Math;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public final class RigidBodyUtils {
    public final Vector2f rotateToVector = new Vector2f();
    public final Vector2f angleToVelocity = new Vector2f();
    private final List<Direction> directions = new ArrayList<>();

    public float getRotationDifference(RigidBody gameObject, Vector2f vector) {
        return getRotationDifference(gameObject, vector.x, vector.y);
    }

    public float getRotationDifference(RigidBody gameObject, float x, float y) {
        return angleToVelocity.set(gameObject.getCos(), gameObject.getSin()).angle(rotateToVector.set(x - gameObject.getX(),
                y - gameObject.getY()));
    }

    public void rotateToVector(RigidBody gameObject, Vector2f vector, float rotationSpeed) {
        rotateToVector(gameObject, vector.x, vector.y, rotationSpeed);
    }

    public void rotateToVector(RigidBody gameObject, float x, float y, float rotationSpeed) {
        Body body = gameObject.getBody();

        float diffRad = getRotationDifference(gameObject, x, y);
        float diffAbs = Math.abs(diffRad);
        float addRot = Math.min(diffAbs, rotationSpeed);

        float sin = gameObject.getSin();
        float cos = gameObject.getCos();
        if (addRot >= diffAbs) {
            if (diffAbs > 0.001f) {
                float sin1 = Math.sin(diffRad);
                float cos1 = Math.cos(diffRad);
                gameObject.setRotation(sin * cos1 + cos * sin1, cos * cos1 - sin * sin1);
            }
        } else {
            addRot = diffRad > 0 ? addRot : -addRot;
            float sin1 = Math.sin(addRot);
            float cos1 = Math.cos(addRot);
            gameObject.setRotation(sin * cos1 + cos * sin1, cos * cos1 - sin * sin1);
        }

        body.setAngularVelocity(body.getAngularVelocity() * 0.98f);
    }

    public List<Direction> calculateDirectionsToPoint(RigidBody gameObject, Vector2f point) {
        float diff = getRotationDifference(gameObject, point);
        float diffAbs = Math.abs(diff);
        directions.clear();

        if (diffAbs > 112.5f * MathUtils.DEGREES_TO_RADIANS) {
            directions.add(Direction.BACKWARD);
        } else if (diffAbs <= 67.5f * MathUtils.DEGREES_TO_RADIANS) {
            directions.add(Direction.FORWARD);
        }

        if (diff < -22.5f * MathUtils.DEGREES_TO_RADIANS && diff >= -157.5f * MathUtils.DEGREES_TO_RADIANS) {
            directions.add(Direction.LEFT);
        } else if (diff > 22.5f * MathUtils.DEGREES_TO_RADIANS && diff <= 157.5f * MathUtils.DEGREES_TO_RADIANS) {
            directions.add(Direction.RIGHT);
        }

        return directions;
    }

    public Direction calculateDirectionToPoint(RigidBody gameObject, Vector2f point) {
        return calculateDirectionToPoint(gameObject, point.x, point.y);
    }

    public Direction calculateDirectionToPoint(RigidBody gameObject, float x, float y) {
        float diff = getRotationDifference(gameObject, x, y);
        float diffAbs = Math.abs(diff);

        if (diffAbs > 135 * MathUtils.DEGREES_TO_RADIANS) {
            return Direction.BACKWARD;
        } else if (diff < -45 * MathUtils.DEGREES_TO_RADIANS && diff >= -135 * MathUtils.DEGREES_TO_RADIANS) {
            return Direction.LEFT;
        } else if (diffAbs <= 45 * MathUtils.DEGREES_TO_RADIANS) {
            return Direction.FORWARD;
        } else {
            return Direction.RIGHT;
        }
    }
}