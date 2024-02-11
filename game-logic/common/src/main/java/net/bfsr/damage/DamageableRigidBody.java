package net.bfsr.damage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import net.bfsr.physics.CollisionMatrixType;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class DamageableRigidBody<CONFIG_DATA extends GameObjectConfigData> extends RigidBody<CONFIG_DATA> {
    private final DamageMask mask;
    @Setter
    private Polygon polygon;
    protected final List<BodyFixture> fixturesToAdd = new ArrayList<>();
    protected final List<BodyFixture> fixturesToRemove = new ArrayList<>();
    private final List<ConnectedObject<?>> connectedObjects = new ArrayList<>();

    protected DamageableRigidBody(float sizeX, float sizeY, CONFIG_DATA configData, int registryId, DamageMask mask,
                                  Polygon polygon) {
        super(0, 0, 0, 1, sizeX, sizeY, configData, registryId);
        this.mask = mask;
        this.polygon = polygon;
    }

    protected DamageableRigidBody(float x, float y, float sin, float cos, float sizeX, float sizeY, CONFIG_DATA configData,
                                  int registryId, DamageMask mask, Polygon polygon) {
        super(x, y, sin, cos, sizeX, sizeY, configData, registryId);
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
        boolean updated = false;

        if (fixturesToAdd.size() > 0) {
            body.removeAllFixtures();
            for (int i = 0; i < fixturesToAdd.size(); i++) {
                body.addFixture(fixturesToAdd.get(i));
            }
            addConnectedObjectFixturesToBody();
            fixturesToAdd.clear();
            fixturesToRemove.clear();
            updated = true;
        }

        if (fixturesToRemove.size() > 0) {
            for (int i = 0; i < fixturesToRemove.size(); i++) {
                body.removeFixture(fixturesToRemove.get(i));
            }
            fixturesToRemove.clear();
            updated = true;
        }

        if (updated) {
            body.updateMass();
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

    public void setFixtures(List<BodyFixture> fixtures) {
        Body body = getBody();
        List<BodyFixture> bodyFixtures = body.getFixtures();
        while (bodyFixtures.size() > 0) {
            body.removeFixture(0);
        }

        for (int i = 0; i < fixtures.size(); i++) {
            body.addFixture(setupFixture(fixtures.get(i)));
        }

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

    @Override
    public int getCollisionMatrixType() {
        return CollisionMatrixType.DAMAGEABLE_RIGID_BODY.ordinal();
    }
}