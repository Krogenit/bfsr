package net.bfsr.physics.correction;

import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.PositionHistory;
import net.bfsr.entity.TransformData;
import org.joml.Vector2f;

public class LocalPlayerInputCorrectionHandler extends CorrectionHandler {
    private static final float MIN_VALUE_TO_CORRECTION = 0.0f;
    private static final float MIN_ANGLE_VALUE_TO_CORRECTION = 0.0f;

    private final PositionHistory positionHistory;
    private final double clientRenderDelayInNanos;

    public LocalPlayerInputCorrectionHandler(PositionHistory positionHistory, double clientRenderDelayInNanos) {
        this.positionHistory = positionHistory;
        this.clientRenderDelayInNanos = clientRenderDelayInNanos;
    }

    @Override
    public void updateTransform(double timestamp) {
        positionHistory.addPositionData(rigidBody.getX(), rigidBody.getY(), rigidBody.getSin(), rigidBody.getCos(),
                timestamp + clientRenderDelayInNanos);

        TransformData serverTransformData = dataHistoryManager.getFirstTransformData(rigidBody.getId());
        if (serverTransformData == null) {
            return;
        }

        TransformData localTransformData = positionHistory.getNonInterpolated(serverTransformData.getTime());
        if (localTransformData != null) {
            Vector2f serverPosition = serverTransformData.getPosition();
            Vector2f localPosition = localTransformData.getPosition();

            float correctionX;
            float correctionY;
            float angleCorrectionAmount;
            float dx = serverPosition.x - localPosition.x;
            float dy = serverPosition.y - localPosition.y;
            float dxAbs = Math.abs(dx);

            if (dxAbs > MIN_VALUE_TO_CORRECTION && dxAbs < 10) {
                float xCorrectionAmount = (dxAbs - MIN_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                correctionX = dx * xCorrectionAmount;
                rigidBody.setPosition(rigidBody.getX() + correctionX, rigidBody.getY());
            }

            float dyAbs = Math.abs(dy);
            if (dyAbs > MIN_VALUE_TO_CORRECTION && dyAbs < 10) {
                float yCorrectionAmount = (dyAbs - MIN_VALUE_TO_CORRECTION) * 0.1f * correctionAmount;
                correctionY = dy * yCorrectionAmount;
                rigidBody.setPosition(rigidBody.getX(), rigidBody.getY() + correctionY);
            }

            float serverCos = serverTransformData.getCos();
            float serverSin = serverTransformData.getSin();
            float localCos = rigidBody.getCos();
            float localSin = rigidBody.getSin();
            float serverAngle = (float) ((serverSin >= 0) ? Math.acos(serverCos) : -Math.acos(serverCos));
            float localAngle = (float) ((localSin >= 0) ? Math.acos(localCos) : -Math.acos(localCos));
            float angleDiff = MathUtils.lerpAngle(localAngle, serverAngle);

            if (angleDiff > MIN_ANGLE_VALUE_TO_CORRECTION) {
                float angleCorrection = (angleDiff - MIN_ANGLE_VALUE_TO_CORRECTION) * 0.1f;
                angleCorrectionAmount = angleCorrection * correctionAmount;
                float newAngle = localAngle + angleCorrectionAmount;
                rigidBody.setRotation(LUT.sin(newAngle), LUT.cos(newAngle));
            }
        }
    }

    @Override
    public void updateData(double timestamp) {}
}
