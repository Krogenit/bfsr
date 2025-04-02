package net.bfsr.engine.physics.correction;

import lombok.NoArgsConstructor;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.math.MathUtils;
import net.bfsr.engine.network.packet.common.world.PacketWorldSnapshot;
import net.bfsr.engine.world.entity.EntityDataHistoryManager;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.engine.world.entity.TransformData;
import org.jbox2d.common.Vector2;
import org.joml.Vector2f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NoArgsConstructor
public class CorrectionHandler {
    protected RigidBody rigidBody;
    EntityDataHistoryManager dataHistoryManager;
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
            correction(serverPosition, rigidBody.getX(), rigidBody.getY(),
                    (dx, dy) -> rigidBody.setPosition(rigidBody.getX() + dx, rigidBody.getY() + dy));
            angleCorrection(transformData.getCos(), transformData.getSin(), rigidBody.getCos(), rigidBody.getSin());
        }
    }

    public void updateData(double timestamp) {
        PacketWorldSnapshot.EntityData entityData = dataHistoryManager.getData(rigidBody.getId(), timestamp);
        if (entityData != null) {
            Vector2f serverVelocity = entityData.getVelocity();
            Vector2 linearVelocity = rigidBody.getLinearVelocity();
            correction(serverVelocity, linearVelocity.x, linearVelocity.y,
                    (dx, dy) -> rigidBody.setVelocity(linearVelocity.x + dx, linearVelocity.y + dy));
            correction(entityData.getAngularVelocity(), rigidBody.getAngularVelocity(), rigidBody::setAngularVelocity);
        }
    }

    void correction(Vector2f serverVector, float localX, float localY, BiConsumer<Float, Float> correctionConsumer) {
        correctionConsumer.accept((serverVector.x - localX) * correctionAmount, (serverVector.y - localY) * correctionAmount);
    }

    private void correction(float serverValue, float localValue, Consumer<Float> correctionConsumer) {
        correctionConsumer.accept(localValue + (serverValue - localValue) * correctionAmount);
    }

    void angleCorrection(float serverCos, float serverSin, float localCos, float localSin) {
        float serverAngle = (float) ((serverSin >= 0) ? Math.acos(serverCos) : -Math.acos(serverCos));
        float localAngle = (float) ((localSin >= 0) ? Math.acos(localCos) : -Math.acos(localCos));
        angleCorrection(localAngle, MathUtils.lerpAngle(localAngle, serverAngle));
    }

    void angleCorrection(float localAngle, float angleDiff) {
        float angle = localAngle + angleDiff * correctionAmount;
        rigidBody.setRotation(LUT.sin(angle), LUT.cos(angle));
    }

    public CorrectionHandler setRigidBody(RigidBody rigidBody) {
        this.rigidBody = rigidBody;
        this.dataHistoryManager = rigidBody.getWorld().getEntityManager().getDataHistoryManager();
        return this;
    }
}
