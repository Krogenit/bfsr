package net.bfsr.physics.filter;

import net.bfsr.entity.ship.Ship;

public class BeamFilter extends CollisionFilter<Ship> {
    public BeamFilter(Ship userData) {
        super(userData, Categories.BEAM_CATEGORY, Categories.SHIP_CATEGORY | Categories.WRECK_CATEGORY);
    }
}