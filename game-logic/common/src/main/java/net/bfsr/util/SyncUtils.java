package net.bfsr.util;

import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.RigidBody;
import org.joml.Math;
import org.joml.Vector2f;

public final class SyncUtils {
    public static void updatePos(GameObject gameObject, Vector2f newPos) {
        Vector2f pos = gameObject.getPosition();

        float dist = pos.distanceSquared(newPos);

        if (dist >= 400) {
            float interpolationAmount = 1.0f;
            float x = pos.x + (newPos.x - pos.x) * interpolationAmount;
            float y = pos.y + (newPos.y - pos.y) * interpolationAmount;
            gameObject.setPosition(x, y);
        } else {
            float alpha = Math.max(dist / 400, 0.0f);
            float x = pos.x + (newPos.x - pos.x) * alpha;
            float y = pos.y + (newPos.y - pos.y) * alpha;

            gameObject.setPosition(x, y);
        }
    }


    public static void updateRot(RigidBody gameObject, float sin, float cos) {
        float currSin = gameObject.getSin();
        float currCos = gameObject.getCos();
        float sinDiff = sin - currSin;
        float cosDiff = cos - currCos;
        float remoteAngle = MathUtils.fastAtan2(sin, cos);
        float localAngle = MathUtils.fastAtan2(currSin, currCos);
        float diff = remoteAngle - localAngle;
        if (diff < MathUtils.MINUS_PI) diff += MathUtils.TWO_PI;
        if (diff > MathUtils.PI) diff -= MathUtils.TWO_PI;
        float diffAbs = Math.abs(diff);
        float interpolationAmount;
        if (diffAbs > 0.06f) {
            interpolationAmount = 1.0f;
        } else {
            interpolationAmount = Math.max(diffAbs / 0.06f, 0.0f);
        }

        gameObject.setRotation(currSin + sinDiff * interpolationAmount, currCos + cosDiff * interpolationAmount);
    }
}