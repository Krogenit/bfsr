package net.bfsr.component.hull;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.util.TimeUtils;

public class Hull {
    @Getter
    @Setter
    private float hull;
    @Getter
    private final float maxHull;
    private final float regenHull;

    public Hull(float maxHull, float regenHull) {
        this.hull = maxHull;
        this.maxHull = maxHull;
        this.regenHull = regenHull;
    }

    public void regenHull(float crewRegen) {
        if (hull < maxHull) {
            hull += (regenHull + crewRegen) * TimeUtils.UPDATE_DELTA_TIME;

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
