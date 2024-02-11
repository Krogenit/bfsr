package net.bfsr.entity.ship.module.engine;

import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;

public class Engine extends DamageableModule {
    private final Polygon polygon;

    public Engine(Polygon polygon) {
        super(5.0f);
        this.polygon = polygon;
    }

    public void init(Ship ship, int id) {
        init(ship);
        this.id = id;
    }

    @Override
    protected void createFixture(RigidBody<?> rigidBody) {
        fixture = new BodyFixture(polygon);
        fixture.setUserData(this);
        fixture.setFilter(new ShipFilter(rigidBody));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        rigidBody.getBody().addFixture(fixture);
    }

    @Override
    protected void destroy() {
        super.destroy();
        ship.getFixturesToRemove().add(fixture);
        if (!ship.getModules().getEngines().isSomeEngineAlive()) {
            ship.setDead();
        }
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ENGINE;
    }
}