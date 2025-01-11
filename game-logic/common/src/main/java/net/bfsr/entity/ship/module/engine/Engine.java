package net.bfsr.entity.ship.module.engine;

import lombok.Getter;
import net.bfsr.config.entity.ship.EngineData;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.Filters;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.dynamics.Fixture;

public class Engine extends DamageableModule {
    @Getter
    private final EngineData engineData;
    private final Polygon polygon;

    public Engine(EngineData engineData) {
        super(5.0f);
        this.engineData = engineData;
        this.polygon = engineData.polygons().get(0).clone();
    }

    public void init(Ship ship, int id) {
        init(ship);
        this.id = id;
    }

    @Override
    protected void createFixture(RigidBody rigidBody) {
        rigidBody.addFixture(fixture = new Fixture(polygon, Filters.SHIP_FILTER, this, PhysicsUtils.DEFAULT_FIXTURE_DENSITY));
    }

    @Override
    protected void destroy() {
        super.destroy();
        if (ship.getWorld().isServer()) {
            ship.removeFixture(fixture);
        }

        if (!ship.getModules().getEngines().isSomeEngineAlive()) {
            ship.setDead();
        }
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ENGINE;
    }
}