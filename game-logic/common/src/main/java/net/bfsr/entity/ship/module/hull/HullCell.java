package net.bfsr.entity.ship.module.hull;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.ship.module.ModuleCell;

@Setter
@Getter
public class HullCell extends ModuleCell {
    protected float value;
    protected float maxValue;
    private int id;
    private int repairTimer;

    protected HullCell(int column, int row) {
        super(column, row);
    }

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