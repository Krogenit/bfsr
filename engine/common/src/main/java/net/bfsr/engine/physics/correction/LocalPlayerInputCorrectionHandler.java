package net.bfsr.engine.physics.correction;

import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.world.entity.EntityDataHistory;
import net.bfsr.engine.world.entity.PositionHistory;
import net.bfsr.engine.world.entity.TransformData;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

public class LocalPlayerInputCorrectionHandler extends CorrectionHandler {
    private static final float MIN_VALUE_TO_CORRECTION = 0.0f;
    private static final float MIN_ANGLE_VALUE_TO_CORRECTION = 0.0f;

    private final PositionHistory positionHistory = new PositionHistory(500);
    private final EntityDataHistory<PacketWorldSnapshot.EntityData> dataHistory = new EntityDataHistory<>(500);
    private final double clientRenderDelayInNanos;

    public LocalPlayerInputCorrectionHandler(double clientRenderDelayInNanos) {
        this.clientRenderDelayInNanos = clientRenderDelayInNanos;
    }

    @Override
    public void updateTransform(double timestamp) {
        double time = timestamp + clientRenderDelayInNanos;
        positionHistory.addPositionData(rigidBody.getX(), rigidBody.getY(), rigidBody.getSin(), rigidBody.getCos(), time);
        dataHistory.addData(new PacketWorldSnapshot.EntityData(rigidBody, time));

        TransformData serverTransformData = dataHistoryManager.getFirstTransformData(rigidBody.getId());
        if (serverTransformData == null) {
            return;
        }

        TransformData localTransformData = positionHistory.getNonInterpolated(serverTransformData.getTime());
        if (localTransformData != null) {
            Vector2f serverPosition = serverTransformData.getPosition();
            Vector2f localPosition = localTransformData.getPosition();

            float dx = serverPosition.x - localPosition.x;
            float dy = serverPosition.y - localPosition.y;
            float dxAbs = Math.abs(dx);

            if (dxAbs > MIN_VALUE_TO_CORRECTION) {
                if (dxAbs > 10) {
                    rigidBody.setPosition(serverPosition.x, rigidBody.getY());
                } else {
                    float xCorrection = dx * (dxAbs - MIN_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                    rigidBody.setPosition(rigidBody.getX() + xCorrection, rigidBody.getY());
                }
            }

            float dyAbs = Math.abs(dy);
            if (dyAbs > MIN_VALUE_TO_CORRECTION) {
                if (dyAbs > 10) {
                    rigidBody.setPosition(rigidBody.getX(), serverPosition.y);
                } else {
                    float yCorrection = dy * (dyAbs - MIN_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                    rigidBody.setPosition(rigidBody.getX(), rigidBody.getY() + yCorrection);
                }
            }

            float serverCos = serverTransformData.getCos();
            float serverSin = serverTransformData.getSin();
            float localCos = rigidBody.getCos();
            float localSin = rigidBody.getSin();
            float serverAngle = (float) ((serverSin >= 0) ? Math.acos(serverCos) : -Math.acos(serverCos));
            float localAngle = (float) ((localSin >= 0) ? Math.acos(localCos) : -Math.acos(localCos));
            float angleDiff = MathUtils.lerpAngle(localAngle, serverAngle);

            if (angleDiff > MIN_ANGLE_VALUE_TO_CORRECTION) {
                float newAngle = localAngle + (angleDiff - MIN_ANGLE_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                rigidBody.setRotation(LUT.sin(newAngle), LUT.cos(newAngle));
            }
        }

        PacketWorldSnapshot.EntityData serverData = dataHistoryManager.getFirstData(rigidBody.getId());
        if (serverData == null) {
            return;
        }

        PacketWorldSnapshot.EntityData localData = dataHistory.get(serverTransformData.getTime());
        if (localData != null) {
            Vector2f serverVelocity = serverData.getVelocity();
            Vector2f localVelocity = localData.getVelocity();
            Vector2 linearVelocity = rigidBody.getLinearVelocity();

            float correctionX;
            float correctionY;
            float angleCorrectionAmount;
            float dx = serverVelocity.x - localVelocity.x;
            float dy = serverVelocity.y - localVelocity.y;
            float dxAbs = Math.abs(dx);

            if (dxAbs > MIN_VALUE_TO_CORRECTION) {
                float xCorrectionAmount = (dxAbs - MIN_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                correctionX = dx * xCorrectionAmount;
                rigidBody.setVelocity(linearVelocity.x + correctionX, linearVelocity.y);
            }

            float dyAbs = Math.abs(dy);
            if (dyAbs > MIN_VALUE_TO_CORRECTION) {
                float yCorrectionAmount = (dyAbs - MIN_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                correctionY = dy * yCorrectionAmount;
                rigidBody.setVelocity(linearVelocity.x, linearVelocity.y + correctionY);
            }

            float serverAngularVelocity = serverData.getAngularVelocity();
            float localAngularVelocity = localData.getAngularVelocity();
            float velocityDiff = serverAngularVelocity - localAngularVelocity;

            if (velocityDiff > MIN_ANGLE_VALUE_TO_CORRECTION) {
                angleCorrectionAmount = (velocityDiff - MIN_ANGLE_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                rigidBody.setAngularVelocity(localAngularVelocity + angleCorrectionAmount);
            }
        }
    }

    @Override
    public void updateData(double timestamp) {}

    public void clear() {
        positionHistory.clear();
        dataHistory.clear();
    }
}
