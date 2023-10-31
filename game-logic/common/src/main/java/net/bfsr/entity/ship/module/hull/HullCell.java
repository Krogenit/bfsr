package net.bfsr.entity.ship.module.hull;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.hull.HullData;

public class HullCell {
    @Getter
    @Setter
    protected float value;
    @Getter
    protected final float repairSpeed;
    @Getter
    protected final float maxValue;
    @Setter
    @Getter
    private int id;

    public HullCell(HullData hullData) {
        this.value = hullData.getMaxHullValue();
        this.maxValue = value;
        this.repairSpeed = hullData.getRegenAmount();
    }

    public void update() {
        if (value < maxValue) {
            value += repairSpeed;

            if (value > maxValue) {
                value = maxValue;
            }
        }
    }
}