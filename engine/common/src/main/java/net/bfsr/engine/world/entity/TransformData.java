package net.bfsr.engine.world.entity;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;

@Getter
public class TransformData extends ChronologicalEntityData {
    private final Vector2f position = new Vector2f();
    @Setter
    private float sin, cos;

    void getInterpol(TransformData other, double time, TransformData destination) {
        float interpolation = (float) ((this.time - time) / (this.time - other.time));
        destination.position.set(position.x + (other.position.x - position.x) * interpolation,
                position.y + (other.position.y - position.y) * interpolation);
        destination.setSin(sin + (other.sin - sin) * interpolation);
        destination.setCos(cos + (other.cos - cos) * interpolation);
        destination.time = time;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }
}