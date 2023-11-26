package net.bfsr.entity.ship.module.hull;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HullCell {
    protected float value;
    protected float maxValue;
    private int id;
    private int repairTimer;

    public void damage(float amount) {
        value -= amount;
        repairTimer = 300;

        if (value < 0) {
            value = 0;
        }
    }

    public void update() {
        if (repairTimer > 0) {
            repairTimer--;
        }
    }
}