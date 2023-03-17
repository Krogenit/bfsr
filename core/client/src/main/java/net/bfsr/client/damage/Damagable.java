package net.bfsr.client.damage;

import clipper2.core.PathsD;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.damage.DamagableCommon;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;

import java.util.List;

public interface Damagable extends DamagableCommon {
    PathsD getContours();
    DamageMaskTexture getMaskTexture();
    Body getBody();
    Vector2f getScale();
    List<BodyFixture> getFixturesToAdd();
    boolean isDead();
    void destroy();
    void setupFixture(BodyFixture bodyFixture);
    void setContours(PathsD contours);
    default void setFixtures(List<BodyFixture> fixtures) {
        Body body = getBody();
        List<BodyFixture> bodyFixtures = body.getFixtures();
        while (bodyFixtures.size() > 0) {
            body.removeFixture(0);
        }

        for (int i = 0; i < fixtures.size(); i++) {
            BodyFixture bodyFixture = fixtures.get(i);
            setupFixture(bodyFixture);
            body.addFixture(bodyFixture);
        }
    }
}