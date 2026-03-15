package net.bfsr.engine.world.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.network.sync.ChronologicalData;
import org.joml.Vector2f;

@Getter
public class VelocityData extends ChronologicalData<VelocityData> {
    private final Vector2f velocity = new Vector2f();
    @Setter
    private float angularVelocity;

    @Override
    public void getInterpolated(VelocityData other, int frame, float interpolation, VelocityData destination) {
        destination.velocity.set(velocity.x + (other.velocity.x - velocity.x) * interpolation,
                velocity.y + (other.velocity.y - velocity.y) * interpolation);
        destination.angularVelocity = (angularVelocity + (other.angularVelocity - angularVelocity) * interpolation);
        destination.frame = frame;
    }

    public void correction(float dx, float dy, float angularVelocityDelta) {
        velocity.add(dx, dy);
        angularVelocity += angularVelocityDelta;
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }
}