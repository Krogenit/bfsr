package net.bfsr.engine.world.entity;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.engine.math.LUT;
import net.bfsr.engine.network.sync.ChronologicalData;
import org.joml.Vector2f;

@Getter
public class TransformData extends ChronologicalData<TransformData> {
    private final Vector2f position = new Vector2f();
    @Setter
    private float sin, cos;

    @Override
    public void getInterpolated(TransformData other, int frame, float interpolation, TransformData destination) {
        destination.position.set(position.x + (other.position.x - position.x) * interpolation,
                position.y + (other.position.y - position.y) * interpolation);
        destination.setSin(sin + (other.sin - sin) * interpolation);
        destination.setCos(cos + (other.cos - cos) * interpolation);
        destination.frame = frame;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void correction(float dx, float dy, float angleCorrection) {
        position.add(dx, dy);

        if (angleCorrection != 0.0f) {
            float localAngle = (float) ((sin >= 0) ? Math.acos(cos) : -Math.acos(cos));
            float newAngle = localAngle + angleCorrection;
            sin = LUT.sin(newAngle);
            cos = LUT.cos(newAngle);
        }
    }
}