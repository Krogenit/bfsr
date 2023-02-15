package net.bfsr.component.hull;

import lombok.Getter;
import lombok.Setter;
import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.util.TimeUtils;

public class Hull {
    @Getter
    @Setter
    private float hull;
    @Getter
    private final float maxHull;
    private final float regenHull;
    private final ShipCommon ship;

    public Hull(float maxHull, float regenHull, ShipCommon ship) {
        this.hull = maxHull;
        this.maxHull = maxHull;
        this.regenHull = regenHull;
        this.ship = ship;
    }

    public void update() {
        regenHull();
    }

    private void regenHull() {
        if (hull < 0) {
            hull = 0;
        }

        if (hull < maxHull) {
            hull += (regenHull + ship.getCrew().getCrewRegen()) * TimeUtils.UPDATE_DELTA_TIME;
        } else if (hull > maxHull) {
            hull = maxHull;
        }
    }

    public void damage(float reducedHullDamage) {
        this.hull -= reducedHullDamage;
    }
}
