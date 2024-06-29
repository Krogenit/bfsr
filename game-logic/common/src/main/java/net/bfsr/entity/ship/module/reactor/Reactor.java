package net.bfsr.entity.ship.module.reactor;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.reactor.ReactorData;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.ShipFilter;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;

public class Reactor extends DamageableModule {
    @Getter
    @Setter
    private float energy;
    @Getter
    private final float maxEnergy;
    private final float regenEnergy;
    private final Convex reactorConvex;
    @Getter
    private final ReactorData reactorData;

    public Reactor(ReactorData reactorData, Convex reactorConvex) {
        super(reactorData.getHp());
        this.reactorData = reactorData;
        this.energy = reactorData.getMaxEnergyCapacity();
        this.maxEnergy = reactorData.getMaxEnergyCapacity();
        this.regenEnergy = reactorData.getRegenAmount();
        this.reactorConvex = reactorConvex;
    }

    @Override
    protected void createFixture(RigidBody rigidBody) {
        fixture = new BodyFixture(reactorConvex);
        fixture.setUserData(this);
        fixture.setFilter(new ShipFilter(rigidBody));
        fixture.setDensity(PhysicsUtils.DEFAULT_FIXTURE_DENSITY);
        rigidBody.getBody().addFixture(fixture);
    }

    @Override
    public void update() {
        regenEnergy();
    }

    private void regenEnergy() {
        if (energy < maxEnergy) {
            energy += regenEnergy;

            if (energy > maxEnergy) {
                energy = maxEnergy;
            }
        }
    }

    public void consume(float amount) {
        energy -= amount;

        if (energy < 0) {
            energy = 0;
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        ship.getFixturesToRemove().add(fixture);
        ship.setDestroying();
    }

    @Override
    public ModuleType getType() {
        return ModuleType.REACTOR;
    }
}