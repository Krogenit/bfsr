package net.bfsr.physics.filter;

import net.bfsr.entity.ship.Ship;

public class BeamFilter extends CollisionFilter {
    public BeamFilter(Ship userData) {
        super(userData, Categories.BEAM_CATEGORY, Categories.SHIP_CATEGORY);
    }
}