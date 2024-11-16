package net.bfsr.entity.ship.module.reactor;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.reactor.ReactorData;
import net.bfsr.entity.RigidBody;
import net.bfsr.entity.ship.module.DamageableModule;
import net.bfsr.entity.ship.module.ModuleType;
import net.bfsr.physics.PhysicsUtils;
import net.bfsr.physics.filter.Filters;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Fixture;

public class Reactor extends DamageableModule {
    @Getter
    @Setter
    private float energy;
    @Getter
    private final float maxEnergy;
    private final float regenEnergy;
    private final Shape shape;
    @Getter
    private final ReactorData reactorData;

    public Reactor(ReactorData reactorData, Shape shape) {
        super(reactorData.getHp());
        this.reactorData = reactorData;
        this.energy = reactorData.getMaxEnergyCapacity();
        this.maxEnergy = reactorData.getMaxEnergyCapacity();
        this.regenEnergy = reactorData.getRegenAmount();
        this.shape = shape;
    }

    @Override
    protected void createFixture(RigidBody rigidBody) {
        rigidBody.getBody().addFixture(fixture = new Fixture(shape, Filters.SHIP_FILTER, this, PhysicsUtils.DEFAULT_FIXTURE_DENSITY));
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
        ship.addFixtureToRemove(fixture);
        ship.setDestroying();
    }

    @Override
    public ModuleType getType() {
        return ModuleType.REACTOR;
    }
}