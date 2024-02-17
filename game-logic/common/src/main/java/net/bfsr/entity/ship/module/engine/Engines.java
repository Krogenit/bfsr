package net.bfsr.entity.ship.module.engine;

import lombok.Getter;
import net.bfsr.config.component.engine.EnginesData;
import net.bfsr.config.entity.ship.EngineData;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.math.Direction;
import org.dyn4j.dynamics.Body;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Getter
public class Engines extends DamageableModule {
    private final EnginesData enginesData;
    private final float forwardAcceleration, backwardAcceleration, sideAcceleration;
    private final float maxForwardVelocity;
    private final float maneuverability;
    private final float angularVelocity;
    private final List<Engine> engines = new ArrayList<>();
    private final EnumMap<Direction, List<Engine>> enginesByDirection = new EnumMap<>(Direction.class);

    public Engines(EnginesData enginesData, Ship ship) {
        this.enginesData = enginesData;
        this.forwardAcceleration = enginesData.getForwardAcceleration();
        this.backwardAcceleration = enginesData.getBackwardAcceleration();
        this.sideAcceleration = enginesData.getSideAcceleration();
        this.maxForwardVelocity = enginesData.getMaxForwardVelocity();
        this.maneuverability = enginesData.getManeuverability();
        this.angularVelocity = enginesData.getAngularVelocity();

        ship.getConfigData().getEngines().forEachEntry((direction, enginesData1) -> {
            List<EngineData> engines1 = enginesData1.engines();
            List<Engine> engineList = new ArrayList<>(engines1.size());
            for (int i = 0; i < engines1.size(); i++) {
                EngineData engineData = engines1.get(i);
                Engine engine = new Engine(engineData);
                engineList.add(engine);
                engines.add(engine);
            }

            enginesByDirection.put(direction, engineList);
            return true;
        });
    }

    @Override
    public void init(Ship ship) {
        super.init(ship);

        for (int i = 0; i < engines.size(); i++) {
            engines.get(i).init(ship, i);
        }
    }

    @Override
    protected void createFixture(RigidBody<?> rigidBody) {}

    @Override
    public void addToList(List<Module> modules) {
        for (int i = 0; i < engines.size(); i++) {
            modules.add(engines.get(i));
        }
    }

    @Override
    public void addFixtureToBody(Body body) {
        for (int i = 0; i < engines.size(); i++) {
            engines.get(i).addFixtureToBody(body);
        }
    }

    public boolean isEngineAlive(Direction direction) {
        if (direction == Direction.STOP) return true;

        List<Engine> engines = enginesByDirection.get(direction);

        for (int i = 0; i < engines.size(); i++) {
            if (!engines.get(i).isDead()) {
                return true;
            }
        }

        return false;
    }

    public boolean isSomeEngineAlive() {
        for (int i = 0; i < engines.size(); i++) {
            if (!engines.get(i).isDead()) {
                return true;
            }
        }

        return false;
    }

    public List<Engine> getEngines(Direction direction) {
        return enginesByDirection.get(direction);
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ENGINE;
    }
}