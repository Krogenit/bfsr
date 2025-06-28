package net.bfsr.engine.physics.correction;

import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.world.entity.TransformData;
import org.joml.Vector2f;

public class HistoryCorrectionHandler extends CorrectionHandler {
    @Override
    public void updateTransform(double time, int frame) {
        TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(), frame);
        if (transformData != null) {
            Vector2f serverPosition = transformData.getPosition();
            rigidBody.setTransform(serverPosition.x, serverPosition.y, transformData.getSin(), transformData.getCos());
        }
    }

    @Override
    public void updateData(double time, int frame) {
        PacketWorldSnapshot.EntityData entityData = dataHistoryManager.getData(rigidBody.getId(), frame);
        if (entityData != null) {
            Vector2f serverVelocity = entityData.getVelocity();
            rigidBody.setVelocity(serverVelocity.x, serverVelocity.y);
            rigidBody.setAngularVelocity(entityData.getAngularVelocity());
        }
    }
}
