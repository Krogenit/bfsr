package net.bfsr.physics.correction;

import lombok.NoArgsConstructor;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.entity.EntityDataHistoryManager;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.TransformData;
import net.bfsr.network.packet.common.entity.PacketWorldSnapshot;
import org.joml.Vector2f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NoArgsConstructor
public class CorrectionHandler {
    protected RigidBody rigidBody;
    EntityDataHistoryManager dataHistoryManager;
    private Vector2f position, velocity;
    float correctionAmount = 1.0f;

    CorrectionHandler(float correctionAmount) {
        this.correctionAmount = correctionAmount;
    }

    public void update(double timestamp) {
        updateTransform(timestamp);
        updateData(timestamp);
    }

    public void updateTransform(double timestamp) {
        TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(), timestamp);
        if (transformData != null) {
            Vector2f serverPosition = transformData.getPosition();
            correction(serverPosition, position, (dx, dy) -> rigidBody.setPosition(position.x + dx, position.y + dy));
            angleCorrection(transformData.getCos(), transformData.getSin(), rigidBody.getCos(), rigidBody.getSin());
        }
    }

    public void updateData(double timestamp) {
        PacketWorldSnapshot.EntityData entityData = dataHistoryManager.getData(rigidBody.getId(), timestamp);
        if (entityData != null) {
            Vector2f serverVelocity = entityData.getVelocity();
            correction(serverVelocity, velocity, (dx, dy) -> rigidBody.setVelocity(velocity.x + dx, velocity.y + dy));
            correction(entityData.getAngularVelocity(), rigidBody.getAngularVelocity(), rigidBody::setAngularVelocity);
        }
    }

    void correction(Vector2f serverVector, Vector2f localVector, BiConsumer<Float, Float> correctionConsumer) {
        correctionConsumer.accept((serverVector.x - localVector.x) * correctionAmount, (serverVector.y - localVector.y) * correctionAmount);
    }

    private void correction(float serverValue, float localValue, Consumer<Float> correctionConsumer) {
        correctionConsumer.accept(localValue + (serverValue - localValue) * correctionAmount);
    }

    void angleCorrection(float serverCos, float serverSin, float localCos, float localSin) {
        float serverAngle = (float) ((serverSin >= 0) ? Math.acos(serverCos) : -Math.acos(serverCos));
        float localAngle = (float) ((localSin >= 0) ? Math.acos(localCos) : -Math.acos(localCos));
        float angle = localAngle + MathUtils.lerpAngle(localAngle, serverAngle) * correctionAmount;
        rigidBody.setRotation(LUT.sin(angle), LUT.cos(angle));
    }

    public CorrectionHandler setRigidBody(RigidBody rigidBody) {
        this.rigidBody = rigidBody;
        this.position = rigidBody.getPosition();
        this.velocity = rigidBody.getVelocity();
        this.dataHistoryManager = rigidBody.getWorld().getEntityManager().getDataHistoryManager();
        return this;
    }
}
