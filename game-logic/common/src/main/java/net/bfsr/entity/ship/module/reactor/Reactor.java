package net.bfsr.entity.ship.module.reactor;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.reactor.ReactorData;
import net.bfsr.entity.ship.module.Module;
import net.bfsr.entity.ship.module.ModuleType;

public class Reactor extends Module {
    @Getter
    @Setter
    private float energy;
    @Getter
    private final float maxEnergy;
    private final float regenEnergy;

    public Reactor(ReactorData reactorData) {
        this.energy = reactorData.getMaxEnergyCapacity();
        this.maxEnergy = reactorData.getMaxEnergyCapacity();
        this.regenEnergy = reactorData.getRegenAmount();
    }

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

    public void consume(float energy) {
        this.energy -= energy;

        if (this.energy < 0) {
            this.energy = 0;
        }
    }

    @Override
    public ModuleType getType() {
        return ModuleType.REACTOR;
    }
}