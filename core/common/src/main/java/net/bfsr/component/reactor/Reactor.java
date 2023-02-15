package net.bfsr.component.reactor;

import lombok.Getter;
import net.bfsr.util.TimeUtils;

public class Reactor {
    @Getter
    private float energy;
    @Getter
    private final float maxEnergy;
    private final float regenEnergy;

    public Reactor(float maxEnergy, float regenEnergy) {
        this.energy = maxEnergy;
        this.maxEnergy = maxEnergy;
        this.regenEnergy = regenEnergy;
    }

    public void update() {
        regenEnergy();
    }

    private void regenEnergy() {
        if (energy < maxEnergy) {
            energy += regenEnergy * TimeUtils.UPDATE_DELTA_TIME;

            if (energy > maxEnergy) {
                energy = maxEnergy;
            }
        }
    }

    public void setEnergy(float energy) {
        this.energy = energy;

        if (this.energy < 0) {
            this.energy = 0;
        }
    }

    public void consume(float energy) {
        this.energy -= energy;

        if (this.energy < 0) {
            this.energy = 0;
        }
    }
}
