package net.bfsr.entity.ship.module.engine;

import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Polygon;

public class Engine extends DamageableModule {
    private final Ship ship;
    private final Polygon polygon;

    public Engine(Ship ship, Polygon polygon) {
        super(5.0f);
        this.ship = ship;
        this.polygon = polygon;
    }

    public void init(Ship ship, int id) {
        init(ship);
        this.id = id;
    }

    @Override
    protected void createFixture() {
        fixture = new BodyFixture(polygon);
        fixture.setUserData(this);
        fixture.setFilter(new ShipFilter(ship));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        ship.getBody().addFixture(fixture);
    }

    @Override
    protected void destroy() {
        super.destroy();
        ship.getFixturesToRemove().add(fixture);
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ENGINE;
    }
}