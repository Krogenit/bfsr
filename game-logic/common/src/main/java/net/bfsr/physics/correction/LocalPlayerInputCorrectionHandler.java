package net.bfsr.physics.correction;

import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.PositionHistory;
import net.bfsr.entity.TransformData;
import org.joml.Vector2f;

public class LocalPlayerInputCorrectionHandler extends CorrectionHandler {
    private static final float MIN_VALUE_TO_CORRECTION = 0.1f;
    private static final float MIN_ANGLE_VALUE_TO_CORRECTION = 0.2f;

    private final PositionHistory positionHistory;
    private final double clientRenderDelayInNanos;

    public LocalPlayerInputCorrectionHandler(PositionHistory positionHistory, double clientRenderDelayInNanos) {
        this.positionHistory = positionHistory;
        this.clientRenderDelayInNanos = clientRenderDelayInNanos;
    }

    @Override
    public void updateTransform(double timestamp) {
        double time = timestamp + clientRenderDelayInNanos;
        positionHistory.addPositionData(rigidBody.getX(), rigidBody.getY(), rigidBody.getSin(), rigidBody.getCos(), time);

        TransformData serverTransformData = dataHistoryManager.getTransformData(rigidBody.getId(), timestamp);

        if (serverTransformData == null) {
            return;
        }

        TransformData localTransformData = positionHistory.get(timestamp);
        if (localTransformData != null) {
            Vector2f serverPosition = serverTransformData.getPosition();
            Vector2f localPosition = localTransformData.getPosition();

            boolean needHistoryCorrection = false;
            float correctionX = 0.0f;
            float correctionY = 0.0f;
            float angleCorrectionAmount = 0.0f;
            float dx = serverPosition.x - localPosition.x;
            float dy = serverPosition.y - localPosition.y;
            float dxAbs = Math.abs(dx);

            if (dxAbs > MIN_VALUE_TO_CORRECTION) {
                float xCorrectionAmount = (dxAbs - MIN_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                correctionX = dx * xCorrectionAmount;
                rigidBody.setPosition(rigidBody.getX() + correctionX, rigidBody.getY());
                needHistoryCorrection = true;
            }

            float dyAbs = Math.abs(dy);
            if (dyAbs > MIN_VALUE_TO_CORRECTION) {
                float yCorrectionAmount = (dyAbs - MIN_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                correctionY = dy * yCorrectionAmount;
                rigidBody.setPosition(rigidBody.getX(), rigidBody.getY() + correctionY);
                needHistoryCorrection = true;
            }

            float serverCos = serverTransformData.getCos();
            float serverSin = serverTransformData.getSin();
            float localCos = rigidBody.getCos();
            float localSin = rigidBody.getSin();
            float serverAngle = (float) ((serverSin >= 0) ? Math.acos(serverCos) : -Math.acos(serverCos));
            float localAngle = (float) ((localSin >= 0) ? Math.acos(localCos) : -Math.acos(localCos));
            float angleDiff = MathUtils.lerpAngle(localAngle, serverAngle);

            if (angleDiff > MIN_ANGLE_VALUE_TO_CORRECTION) {
                float angleCorrection = angleDiff - MIN_ANGLE_VALUE_TO_CORRECTION;
                angleCorrectionAmount = angleCorrection * correctionAmount;
                float newAngle = localAngle + angleCorrectionAmount;
                rigidBody.setRotation(LUT.sin(newAngle), LUT.cos(newAngle));
                needHistoryCorrection = true;
            }

            if (needHistoryCorrection) {
                positionHistory.correction(correctionX, correctionY, angleCorrectionAmount);
            }
        }
    }

    @Override
    public void updateData(double timestamp) {}
}
