package net.bfsr.physics.correction;

import net.bfsr.entity.PositionHistory;
import net.bfsr.entity.TransformData;
import org.joml.Vector2f;

public class LocalPlayerInputCorrectionHandler extends CorrectionHandler {
    private final PositionHistory positionHistory;
    private final double clientRenderDelayInNanos;

    public LocalPlayerInputCorrectionHandler(PositionHistory positionHistory, double clientRenderDelayInNanos) {
        this.positionHistory = positionHistory;
        this.clientRenderDelayInNanos = clientRenderDelayInNanos;
    }

    @Override
    public void updateTransform(double timestamp) {
        Vector2f position = rigidBody.getPosition();
        double time = timestamp + clientRenderDelayInNanos;
        positionHistory.addPositionData(position, rigidBody.getSin(), rigidBody.getCos(), time);

        TransformData serverTransformData = dataHistoryManager.getTransformData(rigidBody.getId(), timestamp);

        if (serverTransformData == null) {
            return;
        }

        TransformData localTransformData = positionHistory.get(timestamp);
        if (localTransformData != null) {
            Vector2f serverPosition = serverTransformData.getPosition();
            Vector2f localPosition = localTransformData.getPosition();

            correction(serverPosition, localPosition, (dx, dy) -> {
                rigidBody.setPosition(position.x + dx, position.y + dy);
                positionHistory.correction(dx, dy);
            });

            angleCorrection(serverTransformData.getCos(), serverTransformData.getSin(), rigidBody.getCos(), rigidBody.getSin());
        }
    }

    @Override
    public void updateData(double timestamp) {}
}
