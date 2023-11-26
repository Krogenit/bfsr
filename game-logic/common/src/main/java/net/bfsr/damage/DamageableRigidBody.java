package net.bfsr.damage;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bfsr.config.GameObjectConfigData;
import net.bfsr.entity.RigidBody;
import org.dyn4j.dynamics.BodyFixture;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class DamageableRigidBody<CONFIG_DATA extends GameObjectConfigData> extends RigidBody<CONFIG_DATA>
        implements Damageable<CONFIG_DATA> {
    private final DamageMask mask;
    @Setter
    private PathsD contours;
    protected final List<BodyFixture> fixturesToAdd = new ArrayList<>();
    protected final List<BodyFixture> fixturesToRemove = new ArrayList<>();
    private final List<ConnectedObject> connectedObjects = new ArrayList<>();

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

    @Override
    public void onContourReconstructed(PathD contour) {}

    @Override
    public void setFixtures(List<BodyFixture> fixtures) {
        Damageable.super.setFixtures(fixtures);
        addConnectedObjectFixturesToBody();
    }

    public void addConnectedObjectFixturesToBody() {
        for (int i = 0; i < connectedObjects.size(); i++) {
            connectedObjects.get(i).addFixtures(body);
        }
    }

    @Override
    public void addConnectedObject(ConnectedObject connectedObject) {
        connectedObjects.add(connectedObject);
    }

    @Override
    public void removeConnectedObject(ConnectedObject connectedObject) {
        connectedObjects.remove(connectedObject);
    }
}