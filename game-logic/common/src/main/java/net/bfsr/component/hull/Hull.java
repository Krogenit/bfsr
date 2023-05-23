package net.bfsr.component.hull;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.config.component.hull.HullData;

public class Hull {
    @Getter
    @Setter
    private float hull;
    @Getter
    private final float maxHull;
    private final float regenAmount;

    public Hull(HullData hullData) {
        this.hull = hullData.getMaxHullValue();
        this.maxHull = hullData.getMaxHullValue();
        this.regenAmount = hullData.getRegenAmount();
    }

    public void regenHull(float crewRegen) {
        if (hull < maxHull) {
            hull += regenAmount + crewRegen;

            if (hull > maxHull) {
                hull = maxHull;
            }
        }
    }

    public void damage(float damage) {
        this.hull -= damage;

        if (hull < 0) {
            hull = 0;
        }
    }
}