package net.bfsr.entity.wreck;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bfsr.config.Vector2fConfigurable;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

@AllArgsConstructor
@Getter
public class RegisteredShipWreck {
    private final String texture;
    private final String fireTexture;
    private final String sparkleTexture;
    private final Polygon polygon;

    public RegisteredShipWreck(String texture, String fireTexture, String sparkleTexture, Vector2fConfigurable[] vectors) {
        this.texture = texture;
        this.fireTexture = fireTexture;
        this.sparkleTexture = sparkleTexture;

        Vector2[] vertices = new Vector2[vectors.length];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = new Vector2(vectors[i].getX(), vectors[i].getY());
        }

        polygon = new Polygon(vertices);
    }
}
