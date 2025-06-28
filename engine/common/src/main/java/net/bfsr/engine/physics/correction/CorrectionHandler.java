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
import org.joml.Vector4f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NoArgsConstructor
public class CorrectionHandler {
    private final Vector4f transform = new Vector4f();

    protected RigidBody rigidBody;
    EntityDataHistoryManager dataHistoryManager;
    float correctionAmount = 1.0f;

    CorrectionHandler(float correctionAmount) {
        this.correctionAmount = correctionAmount;
    }

    public void update(double time, int frame) {
        updateTransform(time, frame);
        updateData(time, frame);
    }

    public void updateTransform(double time, int frame) {
        TransformData transformData = dataHistoryManager.getTransformData(rigidBody.getId(), frame);
        if (transformData == null) {
            return;
        }

        Vector2f serverPosition = transformData.getPosition();
        correction(serverPosition, rigidBody.getX(), rigidBody.getY(), (dx, dy) -> {
            transform.x = rigidBody.getX() + dx;
            transform.y = rigidBody.getY() + dy;
        });
        angleCorrection(transformData.getCos(), transformData.getSin(), rigidBody.getCos(), rigidBody.getSin(), (sin, cos) -> {
            transform.z = sin;
            transform.w = cos;
        });

        rigidBody.setTransform(transform.x, transform.y, transform.z, transform.w);
    }

    public void updateData(double time, int frame) {
        PacketWorldSnapshot.EntityData entityData = dataHistoryManager.getData(rigidBody.getId(), frame);
        if (entityData == null) {
            return;
        }

        Vector2f serverVelocity = entityData.getVelocity();
        Vector2 linearVelocity = rigidBody.getLinearVelocity();
        correction(serverVelocity, linearVelocity.x, linearVelocity.y,
                (dx, dy) -> rigidBody.setVelocity(linearVelocity.x + dx, linearVelocity.y + dy));
        correction(entityData.getAngularVelocity(), rigidBody.getAngularVelocity(), rigidBody::setAngularVelocity);
    }

    private void correction(Vector2f serverVector, float localX, float localY, BiConsumer<Float, Float> correctionConsumer) {
        correctionConsumer.accept((serverVector.x - localX) * correctionAmount, (serverVector.y - localY) * correctionAmount);
    }

    private void correction(float serverValue, float localValue, Consumer<Float> correctionConsumer) {
        correctionConsumer.accept(localValue + (serverValue - localValue) * correctionAmount);
    }

    private void angleCorrection(float serverCos, float serverSin, float localCos, float localSin,
                                 BiConsumer<Float, Float> correctionConsumer) {
        float serverAngle = (float) ((serverSin >= 0) ? Math.acos(serverCos) : -Math.acos(serverCos));
        float localAngle = (float) ((localSin >= 0) ? Math.acos(localCos) : -Math.acos(localCos));
        angleCorrection(localAngle, MathUtils.lerpAngle(localAngle, serverAngle), correctionConsumer);
    }

    private void angleCorrection(float localAngle, float angleDiff, BiConsumer<Float, Float> correctionConsumer) {
        float angle = localAngle + angleDiff * correctionAmount;
        correctionConsumer.accept(LUT.sin(angle), LUT.cos(angle));
    }

    public CorrectionHandler setRigidBody(RigidBody rigidBody) {
        this.rigidBody = rigidBody;
        this.dataHistoryManager = rigidBody.getWorld().getEntityManager().getDataHistoryManager();
        return this;
    }
}
