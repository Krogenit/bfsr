package net.bfsr.damage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import net.bfsr.physics.CollisionMatrixType;
import org.jbox2d.dynamics.Fixture;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class DamageableRigidBody extends RigidBody {
    @Getter
    private final DamageMask mask;
    @Setter
    @Getter
    private Polygon polygon;
    private final List<Fixture> fixturesToAdd = new ArrayList<>();
    private final List<Fixture> fixturesToRemove = new ArrayList<>();
    @Getter
    private final List<ConnectedObject<?>> connectedObjects = new ArrayList<>();

    protected DamageableRigidBody(float sizeX, float sizeY, GameObjectConfigData configData, DamageMask mask, Polygon polygon) {
        super(0, 0, 0, 1, sizeX, sizeY, configData);
        this.mask = mask;
        this.polygon = polygon;
    }

    protected DamageableRigidBody(float x, float y, float sin, float cos, float sizeX, float sizeY, GameObjectConfigData configData,
                                  DamageMask mask, Polygon polygon) {
        super(x, y, sin, cos, sizeX, sizeY, configData);
        this.mask = mask;
        this.polygon = polygon;
    }

    @Override
    public void update() {
        super.update();
        updateFixtures();
        updateConnectedObjects();
    }

    protected void updateFixtures() {
        if (fixturesToAdd.size() > 0) {
            body.setFixtures(fixturesToAdd);
            addConnectedObjectFixturesToBody();
            fixturesToAdd.clear();
            fixturesToRemove.clear();
        } else if (fixturesToRemove.size() > 0) {
            for (int i = 0; i < fixturesToRemove.size(); i++) {
                Fixture fixture = fixturesToRemove.get(i);
                if (fixture.body == null) {
                    fixturesToRemove.remove(i--);
                }
            }

            body.removeFixtures(fixturesToRemove);
            fixturesToRemove.clear();
        }
    }

    protected void updateConnectedObjects() {
        for (int i = 0; i < connectedObjects.size(); i++) {
            connectedObjects.get(i).update();
        }
    }

    @Override
    public void postPhysicsUpdate() {
        super.postPhysicsUpdate();
        for (int i = 0; i < connectedObjects.size(); i++) {
            connectedObjects.get(i).postPhysicsUpdate(this);
        }
    }

    public void onContourReconstructed(Polygon polygon) {}

    public void setFixtures(List<Fixture> fixtures) {
        for (int i = 0; i < fixtures.size(); i++) {
            setupFixture(fixtures.get(i));
        }

        body.setFixtures(fixtures);
        addConnectedObjectFixturesToBody();
    }

    public void addConnectedObjectFixturesToBody() {
        for (int i = 0; i < connectedObjects.size(); i++) {
            connectedObjects.get(i).addFixtures(body);
        }
    }

    public void addConnectedObject(ConnectedObject<?> connectedObject) {
        connectedObjects.add(connectedObject);
    }

    public void removeConnectedObject(ConnectedObject<?> connectedObject) {
        connectedObjects.remove(connectedObject);
    }

    public void initConnectedObject(ConnectedObject<?> connectedObject) {
        connectedObject.init(this);
    }

    public void addFixtureToAdd(Fixture fixture) {
        fixturesToAdd.add(fixture);
    }

    public void addFixtureToRemove(Fixture fixture) {
        fixturesToRemove.add(fixture);
    }

    @Override
    public int getCollisionMatrixType() {
        return CollisionMatrixType.DAMAGEABLE_RIGID_BODY.ordinal();
    }

    void clearFixturesToAdd() {
        fixturesToAdd.clear();
    }
}