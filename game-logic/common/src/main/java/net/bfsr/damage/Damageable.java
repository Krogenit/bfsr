package net.bfsr.damage;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.world.World;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;

import java.util.List;

public interface Damageable<CONFIG_DATA extends GameObjectConfigData> {
    DamageMask getMask();
    BodyFixture setupFixture(BodyFixture bodyFixture);
    void setContours(PathsD contours);
    default void setFixtures(List<BodyFixture> fixtures) {
        Body body = getBody();
        List<BodyFixture> bodyFixtures = body.getFixtures();
        while (bodyFixtures.size() > 0) {
            body.removeFixture(0);
        }

        for (int i = 0; i < fixtures.size(); i++) {
            body.addFixture(setupFixture(fixtures.get(i)));
        }
    }
    int getId();
    PathsD getContours();
    Body getBody();
    World getWorld();
    Vector2f getSize();
    List<BodyFixture> getFixturesToAdd();
    void setDead();
    boolean isDead();
    float getX();
    float getY();
    float getSin();
    float getCos();
    CONFIG_DATA getConfigData();
    List<ConnectedObject> getConnectedObjects();
    void removeConnectedObject(ConnectedObject connectedObject);
    void addConnectedObject(ConnectedObject connectedObject);
    void onContourReconstructed(PathD contour);
}