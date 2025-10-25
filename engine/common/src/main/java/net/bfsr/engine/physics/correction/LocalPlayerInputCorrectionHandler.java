package net.bfsr.engine.physics.correction;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.network.NetworkHandler;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.network.sync.DataHistory;
import net.bfsr.engine.world.entity.EntityPositionHistory;
import net.bfsr.engine.world.entity.TransformData;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

@RequiredArgsConstructor
public class LocalPlayerInputCorrectionHandler extends CorrectionHandler {
    private static final float MIN_VALUE_TO_CORRECTION = 0.0f;
    private static final float MIN_ANGLE_VALUE_TO_CORRECTION = 0.0f;
    private static final float SMALL_CORRECTION_FACTOR = 0.1f;
    private static final float FORCE_CORRECTION_TO_SERVER_THRESHOLD = 5.0f;

    private final EntityPositionHistory positionHistory = new EntityPositionHistory(NetworkHandler.GLOBAL_HISTORY_LENGTH_MILLIS);
    private final DataHistory<PacketWorldSnapshot.EntityData> dataHistory = new DataHistory<>(
            NetworkHandler.GLOBAL_HISTORY_LENGTH_MILLIS, new PacketWorldSnapshot.EntityData(0, 0, 0, 0, 0, new Vector2f()));
    @Setter
    private int renderDelayInFrames;

    private void updateTransform() {
        TransformData serverTransformData = dataHistoryManager.getAndRemoveFirstTransformData(rigidBody.getId());
        if (serverTransformData == null) {
            return;
        }

        TransformData localTransformData = positionHistory.get(serverTransformData.getFrame());
        if (localTransformData == null) {
            return;
        }

        int deltaFrame = serverTransformData.getFrame() - localTransformData.getFrame();
        if (deltaFrame != 0) {
            return;
        }

        Vector2f serverPosition = serverTransformData.getPosition();
        Vector2f localPosition = localTransformData.getPosition();

        float dx = serverPosition.x - localPosition.x;
        float dy = serverPosition.y - localPosition.y;
        float dxAbs = Math.abs(dx);
        float correctionX;

        if (dxAbs > MIN_VALUE_TO_CORRECTION) {
            if (dxAbs > FORCE_CORRECTION_TO_SERVER_THRESHOLD) {
                correctionX = dx;
            } else {
                correctionX = dx * SMALL_CORRECTION_FACTOR * correctionAmount;
            }

            rigidBody.setPosition(rigidBody.getX() + correctionX, rigidBody.getY());
        } else {
            correctionX = 0.0f;
        }

        float dyAbs = Math.abs(dy);
        float correctionY;
        if (dyAbs > MIN_VALUE_TO_CORRECTION) {
            if (dyAbs > FORCE_CORRECTION_TO_SERVER_THRESHOLD) {
                correctionY = dy;
            } else {
                correctionY = dy * SMALL_CORRECTION_FACTOR * correctionAmount;
            }

            rigidBody.setPosition(rigidBody.getX(), rigidBody.getY() + correctionY);
        } else {
            correctionY = 0.0f;
        }

        float serverCos = serverTransformData.getCos();
        float serverSin = serverTransformData.getSin();
        float localCos = rigidBody.getCos();
        float localSin = rigidBody.getSin();
        float serverAngle = (float) ((serverSin >= 0.0f) ? Math.acos(serverCos) : -Math.acos(serverCos));
        float localAngle = (float) ((localSin >= 0.0f) ? Math.acos(localCos) : -Math.acos(localCos));
        float angleDiff = MathUtils.lerpAngle(localAngle, serverAngle);
        float angleCorrection;

        if (angleDiff > MIN_ANGLE_VALUE_TO_CORRECTION) {
            angleCorrection = angleDiff * SMALL_CORRECTION_FACTOR * correctionAmount;
            float newAngle = localAngle + angleCorrection;
            rigidBody.setRotation(LUT.sin(newAngle), LUT.cos(newAngle));
        } else {
            angleCorrection = 0.0f;
        }

        if (correctionX != 0.0f || correctionY != 0.0f || angleCorrection != 0.0f) {
            positionHistory.forEach(transformData -> {
                transformData.correction(correctionX, correctionY, angleCorrection);
            });
        }
    }

    private void updateVelocity() {
        PacketWorldSnapshot.EntityData serverData = dataHistoryManager.getAndRemoveFirstData(rigidBody.getId());
        if (serverData == null) {
            return;
        }

        PacketWorldSnapshot.EntityData localData = dataHistory.getInterpolated(serverData.getFrame());
        if (localData == null) {
            return;
        }

        int deltaFrame = serverData.getFrame() - localData.getFrame();
        if (deltaFrame != 0) {
            return;
        }

        Vector2f serverVelocity = serverData.getVelocity();
        Vector2f localVelocity = localData.getVelocity();
        Vector2 linearVelocity = rigidBody.getLinearVelocity();

        float correctionX;
        float correctionY;
        float angularVelocityCorrectionAmount;
        float dx = serverVelocity.x - localVelocity.x;
        float dy = serverVelocity.y - localVelocity.y;
        float dxAbs = Math.abs(dx);

        if (dxAbs > MIN_VALUE_TO_CORRECTION) {
            correctionX = dx * SMALL_CORRECTION_FACTOR * correctionAmount;
            rigidBody.setVelocity(linearVelocity.x + correctionX, linearVelocity.y);
        } else {
            correctionX = 0.0f;
        }

        float dyAbs = Math.abs(dy);
        if (dyAbs > MIN_VALUE_TO_CORRECTION) {
            correctionY = dy * SMALL_CORRECTION_FACTOR * correctionAmount;
            rigidBody.setVelocity(linearVelocity.x, linearVelocity.y + correctionY);
        } else {
            correctionY = 0.0f;
        }

        float serverAngularVelocity = serverData.getAngularVelocity();
        float localAngularVelocity = localData.getAngularVelocity();
        float velocityDiff = serverAngularVelocity - localAngularVelocity;

        if (velocityDiff > MIN_ANGLE_VALUE_TO_CORRECTION) {
            angularVelocityCorrectionAmount = velocityDiff * SMALL_CORRECTION_FACTOR * correctionAmount;
            rigidBody.setAngularVelocity(localAngularVelocity + angularVelocityCorrectionAmount);
        } else {
            angularVelocityCorrectionAmount = 0.0f;
        }

        if (correctionX != 0.0f || correctionY != 0.0f || angularVelocityCorrectionAmount != 0.0f) {
            dataHistory.forEach(entityData -> entityData.correction(correctionX, correctionY, angularVelocityCorrectionAmount));
        }
    }

    @Override
    public void updateTransform(int frame) {
        int frameWithoutDelay = frame + renderDelayInFrames;
        positionHistory.addPositionData(rigidBody.getX(), rigidBody.getY(), rigidBody.getSin(), rigidBody.getCos(), frameWithoutDelay);
        dataHistory.addData(new PacketWorldSnapshot.EntityData(rigidBody, frameWithoutDelay));

        updateTransform();
        updateVelocity();
    }

    @Override
    public void updateData(int frame) {}

    public void clear() {
        positionHistory.clear();
        dataHistory.clear();
    }
}
