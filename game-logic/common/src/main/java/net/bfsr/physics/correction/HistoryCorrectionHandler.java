package net.bfsr.physics.correction;

import net.bfsr.entity.TransformData;
import net.bfsr.network.packet.common.entity.PacketWorldSnapshot;
import org.joml.Vector2f;

public class HistoryCorrectionHandler extends CorrectionHandler {
    @Override
    public void updateTransform(double timestamp) {
        TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(), timestamp);
        if (transformData != null) {
            Vector2f serverPosition = transformData.getPosition();
            rigidBody.setPosition(serverPosition.x, serverPosition.y);
            rigidBody.setRotation(transformData.getSin(), transformData.getCos());
        }
    }

    @Override
    public void updateData(double timestamp) {
        PacketWorldSnapshot.EntityData entityData = dataHistoryManager.getData(rigidBody.getId(), timestamp);
        if (entityData != null) {
            Vector2f serverVelocity = entityData.getVelocity();
            rigidBody.setVelocity(serverVelocity.x, serverVelocity.y);
            rigidBody.setAngularVelocity(entityData.getAngularVelocity());
        }
    }
}
