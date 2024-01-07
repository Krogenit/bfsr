package net.bfsr.entity;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;

@Getter
public class TransformData extends ChronologicalEntityData {
    private final Vector2f position = new Vector2f();
    @Setter
    private float sin, cos;

    TransformData getInterpol(TransformData other, double time) {
        float interpolation = (float) ((this.time - time) / (this.time - other.time));
        TransformData epd = new TransformData();
        epd.position.set(position.x + (other.position.x - position.x) * interpolation,
                position.y + (other.position.y - position.y) * interpolation);
        epd.setSin(sin + (other.sin - sin) * interpolation);
        epd.setCos(cos + (other.cos - cos) * interpolation);
        epd.time = time;

        return epd;
    }

    public void setPosition(Vector2f pos) {
        position.set(pos);
    }
}