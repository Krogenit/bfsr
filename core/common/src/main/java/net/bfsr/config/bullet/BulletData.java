package net.bfsr.config.bullet;

import lombok.Getter;
import net.bfsr.config.Vector2fConfigurable;
import net.bfsr.util.PathHelper;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.joml.Vector4f;

import java.nio.file.Path;

@Getter
public class BulletData {
    private final float bulletSpeed;
    private final float sizeX, sizeY;
    private final Path texturePath;
    private final Vector4f color;
    private final float lifeTime;
    private final DamageConfigurable bulletDamage;
    private final Polygon polygon;
    private final int dataIndex;

    public BulletData(BulletConfig bulletConfig, int dataIndex) {
        this.dataIndex = dataIndex;
        this.bulletSpeed = bulletConfig.speed();
        this.sizeX = bulletConfig.size().x();
        this.sizeY = bulletConfig.size().y();
        this.texturePath = PathHelper.CLIENT_CONTENT.resolve(bulletConfig.texture());
        this.color = new Vector4f(bulletConfig.color().r(), bulletConfig.color().g(), bulletConfig.color().b(), bulletConfig.color().a());
        this.lifeTime = bulletConfig.lifeTime();
        this.bulletDamage = bulletConfig.damage();

        Vector2fConfigurable[] configurableVertices = bulletConfig.vertices();
        Vector2[] vertices = new Vector2[configurableVertices.length];
        for (int i = 0; i < vertices.length; i++) {
            Vector2fConfigurable configurableVertex = configurableVertices[i];
            vertices[i] = new Vector2(configurableVertex.x(), configurableVertex.y());
        }

        polygon = new Polygon(vertices);
    }
}