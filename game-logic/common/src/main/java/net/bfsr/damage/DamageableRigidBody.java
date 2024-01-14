package net.bfsr.damage;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class DamageableRigidBody<CONFIG_DATA extends GameObjectConfigData> extends RigidBody<CONFIG_DATA> {
    private final DamageMask mask;
    @Setter
    private PathsD contours;
    protected final List<BodyFixture> fixturesToAdd = new ArrayList<>();
    protected final List<BodyFixture> fixturesToRemove = new ArrayList<>();
    private final List<ConnectedObject<?>> connectedObjects = new ArrayList<>();

    protected DamageableRigidBody(float sizeX, float sizeY, CONFIG_DATA configData, int registryId, DamageMask mask,
                                  PathD contour) {
        super(0, 0, 0, 1, sizeX, sizeY, configData, registryId);
        this.mask = mask;
        this.contours = new PathsD();
        this.contours.add(contour);
    }

    protected DamageableRigidBody(float x, float y, float sin, float cos, float sizeX, float sizeY, CONFIG_DATA configData,
                                  int registryId, DamageMask mask, PathsD contours) {
        super(x, y, sin, cos, sizeX, sizeY, configData, registryId);
        this.mask = mask;
        this.contours = contours;
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

    public void onContourReconstructed(PathD contour) {}

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
}