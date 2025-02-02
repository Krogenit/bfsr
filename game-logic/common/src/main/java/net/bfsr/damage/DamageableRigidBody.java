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

@Getter
@RequiredArgsConstructor
public class DamageableRigidBody extends RigidBody {
    private final DamageMask mask;
    @Setter
    private Polygon polygon;
    private final List<ConnectedObject<?>> connectedObjects = new ArrayList<>();
    private final float localOffsetX, localOffsetY;

    protected DamageableRigidBody(float sizeX, float sizeY, GameObjectConfigData configData, DamageMask mask, Polygon polygon) {
        this(0, 0, 0, 1, sizeX, sizeY, configData, mask, polygon, 0.0f, 0.0f);
    }

    protected DamageableRigidBody(float x, float y, float sin, float cos, float sizeX, float sizeY, GameObjectConfigData configData,
                                  DamageMask mask, Polygon polygon, float localOffsetX, float localOffsetY) {
        super(x, y, sin, cos, sizeX, sizeY, configData);
        this.mask = mask;
        this.polygon = polygon;
        this.localOffsetX = localOffsetX;
        this.localOffsetY = localOffsetY;
    }

    @Override
    public void update() {
        super.update();
        updateConnectedObjects();
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
            connectedObjects.get(i).addFixtures(this);
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