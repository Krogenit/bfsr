package net.bfsr.entity.ship.module.engine;

import lombok.extern.log4j.Log4j2;
import net.bfsr.config.component.engine.EnginesData;
import net.bfsr.config.entity.ship.EngineData;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.Filters;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Fixture;

import java.util.List;

@Log4j2
public class Engine extends DamageableModule {
    private final Polygon polygon;

    public Engine(EnginesData enginesData, EngineData engineData) {
        super(enginesData, 5.0f);
        List<Polygon> polygons = engineData.polygons();
        if (polygons.isEmpty()) {
            this.polygon = new Polygon(new Vector2[]{new Vector2(-0.5f, -0.5f), new Vector2(0.5f, -0.5f), new Vector2(0.5f, 0.5f),
                    new Vector2(-0.5f, 0.5f)});
            log.error("Can't find polygon for ship engine in engine data {}", engineData);
        } else {
            this.polygon = polygons.get(0).clone();
        }
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